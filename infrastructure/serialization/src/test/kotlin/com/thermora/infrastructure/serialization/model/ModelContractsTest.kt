package com.thermora.infrastructure.serialization.model

import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.application.ports.record.*
import com.thermora.application.core.codec.SnapshotCodecRegistry
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.infrastructure.persistence.memory.InMemoryCalculationRecordRepository
import com.thermora.infrastructure.serialization.registry.StandardSnapshotCodecs
import com.thermora.infrastructure.serialization.sealedhousing.legacy.*
import com.thermora.infrastructure.serialization.internalmixing.legacy.*
import com.thermora.infrastructure.serialization.externalcooling.legacy.*
import com.thermora.infrastructure.serialization.finnedhousing.legacy.*
import com.thermora.models.sealedhousing.legacy.output.*
import com.thermora.models.internalmixing.legacy.output.*
import com.thermora.models.externalcooling.legacy.output.*
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.concurrent.*
import kotlin.test.*

class ModelContractsTest {
    private val codecs=StandardSnapshotCodecs.all()
    private val roots=listOf("SealedHousingLegacy","InternalMixingLegacy","ExternalCoolingLegacy","FinnedHousingLegacy")
    private fun text(index:Int,suffix:String="Full"):String = checkNotNull(javaClass.getResource("/model-v1/${roots[index/2]}${if(index%2==0) "Input" else "Output"}$suffix.json")).readText()
    private fun decode(index:Int,content:String)=codecs[index].decode(SnapshotPayload(codecs[index].schema,content))

    @Test fun `all structural error cases preserve paths`() {
        val stream=checkNotNull(javaClass.getResourceAsStream("/model-v1/negative-cases.tsv.gz"))
        var count=0
        GZIPInputStream(stream).bufferedReader().useLines { lines -> lines.forEach { line ->
            val parts=line.split('\t');val index=parts[0].toInt()
            val result=assertIs<SnapshotCodecResult.Failure>(decode(index,String(Base64.getDecoder().decode(parts[3]),Charsets.UTF_8)))
            assertEquals(SnapshotCodecError.valueOf(parts[1]),result.error,line.take(100))
            assertTrue(result.message.startsWith(parts[2]+":"),"${parts[2]} != ${result.message}")
            count++
        } }
        assertEquals(2252,count)
    }
    @Test fun `all codecs accept independently reordered fields`() {
        for(index in codecs.indices) assertEquals(decode(index,text(index)),decode(index,text(index,"Reversed")))
    }
    @Test fun `registry contains exactly eight immutable registrations`() {
        assertEquals(8,codecs.size)
        val registry=SnapshotCodecRegistry(codecs)
        assertEquals(8,registry.availableSchemas().size)
        for(c in codecs) {
            assertSame(c,registry.find(c.schema))
            assertSame(c,registry.find(c.schema,c.valueType))
            assertNull(registry.find(c.schema,String::class))
            assertNull(registry.find(c.schema.copy(version=2)))
        }
        assertFailsWith<UnsupportedOperationException> { (codecs as MutableList).clear() }
        assertFailsWith<IllegalArgumentException> { SnapshotCodecRegistry(codecs+codecs.first()) }
    }
    @Test fun `all four model snapshots persist success and failure independently of models`() {
        val repository=InMemoryCalculationRecordRepository()
        val now=Instant.parse("2026-11-26T00:00:00Z")
        for(model in 0..3) for(success in listOf(false,true)) {
            val i=model*2
            val input=SnapshotPayload(codecs[i].schema,text(i))
            val output=if(success) SnapshotPayload(codecs[i+1].schema,text(i+1)) else null
            val diagnostic=SnapshotDiagnostic("SERIALIZATION_TEST","Independent synthetic fixture",DiagnosticSeverity.WARNING,null,emptyMap())
            val snapshot=CalculationSnapshot(CalculationModelId(listOf("sealed-housing-legacy","internal-mixing-legacy","external-cooling-legacy","finned-housing-legacy")[model]),"1.0.0",input,output,if(success) CalculationStatus.SUCCESS else CalculationStatus.FAILURE,now,now,listOf(diagnostic))
            val id=CalculationRecordId("$model-$success")
            val record=CalculationRecord(id,1,snapshot,now)
            assertIs<RepositoryResult.Success<Unit>>(repository.insert(record))
            val restored=assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(id)).value!!
            assertEquals(record,restored)
            assertEquals(listOf(diagnostic),restored.snapshot.diagnostics)
            assertIs<SnapshotCodecResult.Success<*>>(codecs[i].decode(restored.snapshot.input))
            if(success) assertIs<SnapshotCodecResult.Success<*>>(codecs[i+1].decode(restored.snapshot.output!!)) else assertNull(restored.snapshot.output)
        }
    }
    @Test fun `all nullable output combinations and absent heat transfer survive`() {
        val sealed=IndependentModelFixtures.SealedHousingLegacyOutputFull()
        repeat(8) { mask ->
            val value=sealed.copy(geometryResults=if(mask and 1!=0) sealed.geometryResults else null,externalHeatTransfer=if(mask and 2!=0) sealed.externalHeatTransfer else null,internalHeatTransfer=if(mask and 4!=0) sealed.internalHeatTransfer else null)
            val c=SealedHousingLegacyOutputCodec();val payload=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value
            assertEquals(value,assertIs<SnapshotCodecResult.Success<*>>(c.decode(payload)).value)
        }
        val external=IndependentModelFixtures.ExternalCoolingLegacyOutputFull()
        repeat(8) { mask ->
            val value=external.copy(geometryResults=if(mask and 1!=0) external.geometryResults else null,externalHeatTransfer=if(mask and 2!=0) external.externalHeatTransfer else null,internalHeatTransfer=if(mask and 4!=0) external.internalHeatTransfer else null)
            val c=ExternalCoolingLegacyOutputCodec();assertEquals(value,assertIs<SnapshotCodecResult.Success<*>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value)).value)
        }
        val mixing=IndependentModelFixtures.InternalMixingLegacyOutputFull()
        repeat(4) { mask ->
            val value=mixing.copy(geometryResults=if(mask and 1!=0) mixing.geometryResults else null,heatTransferResults=if(mask and 2!=0) mixing.heatTransferResults else null)
            val c=InternalMixingLegacyOutputCodec();assertEquals(value,assertIs<SnapshotCodecResult.Success<*>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value)).value)
        }
    }
    @Test fun `restored collections cannot be mutated and preserve repeats`() {
        val c=SealedHousingLegacyInputCodec();val initial=IndependentModelFixtures.SealedHousingLegacyInputFull()
        val wall=checkNotNull(initial.heatTransfer).wallGaps.first()
        val walls=listOf(wall.copy(wallIndex=2),wall.copy(wallIndex=1),wall.copy(wallIndex=2))
        val input=initial.copy(heatTransfer=checkNotNull(initial.heatTransfer).copy(wallGaps=walls))
        val restored=assertIs<SnapshotCodecResult.Success<com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(input)).value)).value
        assertEquals(walls,restored.heatTransfer!!.wallGaps)
        assertFailsWith<UnsupportedOperationException> { (checkNotNull(restored.heatTransfer).wallGaps as MutableList).clear() }
    }
    @Test fun `model invalid states and inconsistent boundary are not repaired`() {
        val c=SealedHousingLegacyInputCodec();val initial=IndependentModelFixtures.SealedHousingLegacyInputFull()
        val value=initial.copy(source=initial.source.copy(power=Quantity(0.0,initial.source.power.unit)),elementWithRadiatorArea=Quantity(-1.0,initial.source.power.unit))
        assertEquals(value,assertIs<SnapshotCodecResult.Success<*>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value)).value)
        val mix=IndependentModelFixtures.InternalMixingLegacyInputFull()
        val inconsistent=mix.copy(sealedHousing=mix.sealedHousing.copy(enclosureTemperature=Temperature.fromKelvin(1.0)))
        val mc=InternalMixingLegacyInputCodec();assertEquals(inconsistent,assertIs<SnapshotCodecResult.Success<*>>(mc.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(mc.encode(inconsistent)).value)).value)
    }
    @Test fun `signed zero subnormal and custom units survive model mapping`() {
        val c=InternalMixingLegacyOutputCodec();val initial=IndependentModelFixtures.InternalMixingLegacyOutputFull()
        for(bits in listOf(0L,Long.MIN_VALUE,1L,Long.MIN_VALUE+1)) {
            val value=initial.copy(heatedZoneRise=TemperatureDelta.fromKelvin(Double.fromBits(bits)))
            val restored=assertIs<SnapshotCodecResult.Success<InternalMixingLegacyOutput>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value)).value
            assertEquals(bits,restored.heatedZoneRise.kelvin.toRawBits())
        }
    }
    @Test fun `encode and decode enforce arrays and total payload limits`() {
        val c=SealedHousingLegacyInputCodec();val initial=IndependentModelFixtures.SealedHousingLegacyInputFull()
        val value=initial.copy(heatTransfer=initial.heatTransfer!!.copy(wallGaps=List(257){checkNotNull(initial.heatTransfer).wallGaps.first()}))
        assertEquals(SnapshotCodecError.INVALID_VALUE,assertIs<SnapshotCodecResult.Failure>(c.encode(value)).error)
        assertEquals(SnapshotCodecError.INVALID_PAYLOAD,assertIs<SnapshotCodecResult.Failure>(c.decode(SnapshotPayload(c.schema,"{}" + " ".repeat(1048575)))).error)
        val nan=initial.copy(heatTransfer=checkNotNull(initial.heatTransfer).copy(enclosureEmissivity=Double.NaN))
        assertEquals(SnapshotCodecError.INVALID_VALUE,assertIs<SnapshotCodecResult.Failure>(c.encode(nan)).error)
    }
    @Test fun `independent concurrent codec operations are deterministic`() {
        val executor=Executors.newFixedThreadPool(8);val start=CountDownLatch(1)
        try {
            val jobs=(0 until 64).map { index -> executor.submit(Callable {
                assertTrue(start.await(10,TimeUnit.SECONDS));val i=index%8
                concurrentRoundTrip(codecs[i],text(i))
            }) }
            start.countDown();jobs.forEach { it.get(15,TimeUnit.SECONDS) }
        } finally { start.countDown();executor.shutdownNow();assertTrue(executor.awaitTermination(10,TimeUnit.SECONDS)) }
    }
    @Test fun `quoted numeric fields are rejected across all eight full contracts`() {
        val input=checkNotNull(javaClass.getResourceAsStream("/model-v1/quoted-numbers.tsv.gz"))
        var count=0
        GZIPInputStream(input).bufferedReader().useLines { lines -> lines.forEach { line ->
            val parts=line.split('\t');val index=parts[0].toInt()
            val result=assertIs<SnapshotCodecResult.Failure>(decode(index,String(Base64.getDecoder().decode(parts[2]),Charsets.UTF_8)))
            assertEquals(SnapshotCodecError.INVALID_PAYLOAD,result.error)
            assertTrue(result.message.startsWith(parts[1]+":"),result.message)
            count++
        } }
        assertEquals(235,count)
    }
    @Test fun `custom descriptor model values preserve raw fields without physical correction`() {
        val c=SealedHousingLegacyInputCodec()
        val unit=MeasurementUnit("custom-power",PhysicalDimension.POWER,Double.MIN_VALUE,-0.0)
        val value=IndependentModelFixtures.SealedHousingLegacyInputFull().copy(elementWithRadiatorArea=Quantity(-Double.MIN_VALUE,unit))
        val payload=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value
        val restored=assertIs<SnapshotCodecResult.Success<com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput>>(c.decode(payload)).value
        assertEquals(value.elementWithRadiatorArea.value.toRawBits(),restored.elementWithRadiatorArea.value.toRawBits())
        assertEquals(unit.scaleToBase.toRawBits(),restored.elementWithRadiatorArea.unit.scaleToBase.toRawBits())
        assertEquals(unit.offsetToBase.toRawBits(),restored.elementWithRadiatorArea.unit.offsetToBase.toRawBits())
    }

    private fun <T:Any> concurrentRoundTrip(codec:SnapshotCodec<T>, text:String) {
        val value=assertIs<SnapshotCodecResult.Success<T>>(codec.decode(SnapshotPayload(codec.schema,text))).value
        val payload=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(value)).value
        assertEquals(text,payload.content)
        assertEquals(value,assertIs<SnapshotCodecResult.Success<T>>(codec.decode(payload)).value)
    }

}

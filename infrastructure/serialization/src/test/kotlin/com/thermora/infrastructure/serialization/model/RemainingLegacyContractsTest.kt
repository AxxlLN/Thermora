package com.thermora.infrastructure.serialization.model

import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.application.core.codec.SnapshotCodecRegistry
import com.thermora.infrastructure.serialization.registry.StandardSnapshotCodecs
import com.thermora.infrastructure.serialization.perforatedhousing.legacy.*
import com.thermora.infrastructure.serialization.forcedcooling.legacy.*
import com.thermora.infrastructure.serialization.perforatedhousing.legacy.v1.*
import com.thermora.infrastructure.serialization.forcedcooling.legacy.v1.*
import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.models.perforatedhousing.legacy.output.PerforatedHousingLegacyOutput
import com.thermora.models.forcedcooling.legacy.output.ForcedCoolingLegacyOutput
import java.util.concurrent.*
import kotlin.test.*

class RemainingLegacyContractsTest {
    @Test fun `all six models and twelve codecs register without conflicts`() {
        assertEquals(6,ThermoraCoreFactory.createDefault().availableModels().size)
        val all=StandardSnapshotCodecs.all()
        assertEquals(12,all.size);assertEquals(12,all.map { it.schema }.toSet().size)
        assertEquals(all.map { it.schema },StandardSnapshotCodecs.all().map { it.schema })
        val registry=SnapshotCodecRegistry(all)
        for(c in all) {
            assertSame(c,registry.find(c.schema,c.valueType))
            assertNull(registry.find(c.schema,String::class))
            assertNull(registry.find(c.schema.copy(version=2)))
        }
        assertFailsWith<UnsupportedOperationException> { (all as MutableList).clear() }
    }
    @Test fun `perforated output preserves all 128 nullable combinations`() {
        val c=PerforatedHousingLegacyOutputCodec();val v=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyOutputFull()
        repeat(128) { mask ->
            val o=v.copy(convergence=v.convergence.copy(enclosurePercent=if(mask and 1!=0)v.convergence.enclosurePercent else null,totalPowerPercent=if(mask and 2!=0)v.convergence.totalPowerPercent else null,airPowerPercent=if(mask and 4!=0)v.convergence.airPowerPercent else null,
                    withinThreePercent = if (mask and 32 != 0) v.convergence.withinThreePercent else null,
                    withinOnePercent = if (mask and 64 != 0) v.convergence.withinOnePercent else null),
                nextStep=v.nextStep.copy(enclosureTemperature=if(mask and 8!=0)v.nextStep.enclosureTemperature else null,zoneTemperature=if(mask and 16!=0)v.nextStep.zoneTemperature else null))
            val p=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(o)).value
            assertEquals(o,assertIs<SnapshotCodecResult.Success<PerforatedHousingLegacyOutput>>(c.decode(p)).value)
        }
    }
    @Test fun `forced output preserves all 128 nullable combinations`() {
        val c=ForcedCoolingLegacyOutputCodec();val v=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyOutputFull()
        repeat(128) { mask ->
            val o=v.copy(convergence=v.convergence.copy(enclosurePercent=if(mask and 1!=0)v.convergence.enclosurePercent else null,zonePercent=if(mask and 2!=0)v.convergence.zonePercent else null,airPowerPercent=if(mask and 4!=0)v.convergence.airPowerPercent else null,
                    withinThreePercent = if (mask and 32 != 0) v.convergence.withinThreePercent else null,
                    withinOnePercent = if (mask and 64 != 0) v.convergence.withinOnePercent else null),
                nextStep=v.nextStep.copy(enclosureTemperature=if(mask and 8!=0)v.nextStep.enclosureTemperature else null,zoneTemperature=if(mask and 16!=0)v.nextStep.zoneTemperature else null))
            assertEquals(o,assertIs<SnapshotCodecResult.Success<*>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(o)).value)).value)
        }
    }
    @Test fun `free scalar raw bits and physically inconsistent states are restored literally`() {
        val p=PerforatedHousingLegacyOutputCodec();val pv=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyOutputFull()
        val f=ForcedCoolingLegacyOutputCodec();val fv=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyOutputFull()
        for(bits in listOf(0L,Long.MIN_VALUE,1L,Long.MIN_VALUE+1,0x7fefffffffffffffL,0xffefffffffffffffUL.toLong())) {
            val x=Double.fromBits(bits)
            val po=pv.copy(quadratic=pv.quadratic.copy(legacyD=x))
            val pr=assertIs<SnapshotCodecResult.Success<PerforatedHousingLegacyOutput>>(p.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(p.encode(po)).value)).value
            assertEquals(bits,pr.quadratic.legacyD.toRawBits())
            val fo=fv.copy(airBalance=fv.airBalance.copy(airRise=TemperatureDelta.fromKelvin(x)))
            val fr=assertIs<SnapshotCodecResult.Success<ForcedCoolingLegacyOutput>>(f.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(f.encode(fo)).value)).value
            assertEquals(bits,fr.airBalance.airRise.kelvin.toRawBits())
        }
    }
    @Test fun `custom units preserve value scale offset and supplementary symbols`() {
        val c=ForcedCoolingLegacyInputCodec();val v=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull()
        val unit=MeasurementUnit("😀".repeat(128),PhysicalDimension.POWER,Double.MIN_VALUE,-0.0)
        val value=v.copy(elementWithRadiatorArea=Quantity(-Double.MAX_VALUE,unit))
        val r=assertIs<SnapshotCodecResult.Success<com.thermora.models.forcedcooling.legacy.input.ForcedCoolingLegacyInput>>(c.decode(assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value)).value
        assertEquals(value.elementWithRadiatorArea,r.elementWithRadiatorArea)
        assertEquals(Long.MIN_VALUE,r.elementWithRadiatorArea.unit.offsetToBase.toRawBits())
        assertEquals(1L,r.elementWithRadiatorArea.unit.scaleToBase.toRawBits())
        val invalid=v.copy(elementWithRadiatorArea=Quantity(1.0,unit.copy(symbol="😀".repeat(129))))
        assertEquals(SnapshotCodecError.INVALID_VALUE,assertIs<SnapshotCodecResult.Failure>(c.encode(invalid)).error)
    }
    @Test fun `nonfinite model values fail encode without partial success`() {
        val c=PerforatedHousingLegacyOutputCodec();val v=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyOutputFull()
        for(x in listOf(Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY)) {
            assertEquals(SnapshotCodecError.INVALID_VALUE,assertIs<SnapshotCodecResult.Failure>(c.encode(v.copy(quadratic=v.quadratic.copy(legacyD=x)))).error)
        }
        assertIs<SnapshotCodecResult.Success<*>>(c.encode(v))
    }
    @Test fun `numeric range type and lexical limits preserve nested paths`() {
        val c=PerforatedHousingLegacyOutputCodec();val full=NewModelContractSupport.resource("PerforatedHousingLegacyOutputFull.json")
        val pattern=Regex("\"legacyD\":[^,}]+")
        assertEquals(1,pattern.findAll(full).count())
        for((token,kind) in listOf("1e999" to SnapshotCodecError.INVALID_VALUE,"1e-999" to SnapshotCodecError.INVALID_VALUE,"true" to SnapshotCodecError.INVALID_PAYLOAD,"\"123\"" to SnapshotCodecError.INVALID_PAYLOAD,("0."+"1".repeat(63)) to SnapshotCodecError.INVALID_PAYLOAD)) {
            val error=assertIs<SnapshotCodecResult.Failure>(c.decode(SnapshotPayload(c.schema,full.replace(pattern,"\"legacyD\":"+token))))
            assertEquals(kind,error.error);assertTrue(error.message.startsWith("$.quadratic.legacyD:"),error.message)
        }
        assertIs<SnapshotCodecResult.Success<*>>(c.decode(SnapshotPayload(c.schema,full.replace(pattern,"\"legacyD\":0."+"1".repeat(62)))))
    }
    @Test fun `perforation constructor invariants map to nested VALUE`() {
        val c=PerforatedHousingLegacyInputCodec();val full=NewModelContractSupport.resource("PerforatedHousingLegacyInputFull.json")
        val bad=full.replace("\"value\":0.00365","\"value\":-0.00365")
        assertNotEquals(full,bad)
        val f=assertIs<SnapshotCodecResult.Failure>(c.decode(SnapshotPayload(c.schema,bad)))
        assertEquals(SnapshotCodecError.INVALID_VALUE,f.error);assertTrue(f.message.startsWith("$.perforation:"))
    }
    @Test fun `new nested mappers consume exactly one value`() {
        val reads:List<(StrictJsonReader,String)->Any> = listOf(
            { r,p -> PerforatedHousingLegacyInputJsonMapper.read(r,p) },{ r,p -> PerforatedHousingLegacyOutputJsonMapper.read(r,p) },
            { r,p -> ForcedCoolingLegacyInputJsonMapper.read(r,p) },{ r,p -> ForcedCoolingLegacyOutputJsonMapper.read(r,p) })
        val roots=listOf("PerforatedHousingLegacyInput","PerforatedHousingLegacyOutput","ForcedCoolingLegacyInput","ForcedCoolingLegacyOutput")
        for(index in roots.indices) {
            val text=NewModelContractSupport.resource(roots[index]+"Full.json")
            StrictJsonReader.read("{\"state\":$text,\"tail\":42}") { r ->
                r.nextToken();JsonObjectFields(r,"$").required("state") { child,path -> reads[index](child,path) }.required("tail") { child,_ -> assertEquals(42,child.readInt()) }.read()
            }
            StrictJsonReader.read("[$text,$text]") { r ->
                r.nextToken();repeat(2) { i -> r.nextToken();reads[index](r,"$[$i]") };assertEquals(com.fasterxml.jackson.core.JsonToken.END_ARRAY,r.nextToken())
            }
        }
    }
    @Test fun `independent concurrent operations share only stateless codecs`() {
        val all=StandardSnapshotCodecs.all().drop(8)
        val roots=listOf("PerforatedHousingLegacyInput","PerforatedHousingLegacyOutput","ForcedCoolingLegacyInput","ForcedCoolingLegacyOutput")
        val pool=Executors.newFixedThreadPool(8);val start=CountDownLatch(1)
        try {
            val jobs=(0 until 32).map { n -> pool.submit(Callable { assertTrue(start.await(10,TimeUnit.SECONDS));roundTrip(all[n%4],NewModelContractSupport.resource(roots[n%4]+"Full.json")) }) }
            start.countDown();jobs.forEach { it.get(15,TimeUnit.SECONDS) }
        } finally { start.countDown();pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS)) }
    }
    @Test fun `shared Core runs both manual models concurrently without state leakage`() {
        val core=ThermoraCoreFactory.createDefault()
        val pi=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputFull()
        val fi=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull()
        fun calculate(index:Int):Any = if(index%2==0) core.calculate(com.thermora.calculation.api.metadata.CalculationModelId("perforated-housing-legacy"),pi,PerforatedHousingLegacyOutput::class)
            else core.calculate(com.thermora.calculation.api.metadata.CalculationModelId("forced-cooling-legacy"),fi,ForcedCoolingLegacyOutput::class)
        val expected=listOf(calculate(0),calculate(1))
        val executor=Executors.newFixedThreadPool(8);val gate=CountDownLatch(1)
        try {
            val futures=(0 until 32).map { index -> executor.submit(Callable { assertTrue(gate.await(10,TimeUnit.SECONDS));assertEquals(expected[index%2],calculate(index)) }) }
            gate.countDown();futures.forEach { it.get(15,TimeUnit.SECONDS) }
        } finally { gate.countDown();executor.shutdownNow();assertTrue(executor.awaitTermination(10,TimeUnit.SECONDS)) }
        assertEquals(pi,PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputFull())
        assertEquals(fi,ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull())
    }

    private fun <T:Any> roundTrip(c:SnapshotCodec<T>,text:String) {
        val value=assertIs<SnapshotCodecResult.Success<T>>(c.decode(SnapshotPayload(c.schema,text))).value
        val first=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value
        assertEquals(first,assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(value)).value)
        assertEquals(value,assertIs<SnapshotCodecResult.Success<T>>(c.decode(first)).value)
    }
}

package com.thermora.infrastructure.serialization.model

import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.application.ports.record.*
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.*
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.infrastructure.persistence.memory.InMemoryCalculationRecordRepository
import com.thermora.infrastructure.serialization.sealedhousing.legacy.*
import com.thermora.infrastructure.serialization.internalmixing.legacy.*
import com.thermora.infrastructure.serialization.externalcooling.legacy.*
import com.thermora.infrastructure.serialization.finnedhousing.legacy.*
import com.thermora.models.sealedhousing.legacy.input.*
import com.thermora.models.sealedhousing.legacy.output.*
import com.thermora.models.internalmixing.legacy.input.*
import com.thermora.models.internalmixing.legacy.output.*
import com.thermora.models.externalcooling.legacy.input.*
import com.thermora.models.externalcooling.legacy.output.*
import com.thermora.models.finnedhousing.legacy.output.*
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.test.*

class ActualCalculationHistoryTest {
    private val core=ThermoraCoreFactory.createDefault()
    private val reference=CalculationIntegrationFixtures.excelInput()
    private fun <I:CalculationInput,O:CalculationOutput> exercise(id:String, input:I, invalid:I, ic:SnapshotCodec<I>, oc:SnapshotCodec<O>, outputType:KClass<O>) {
        val repository=InMemoryCalculationRecordRepository()
        val now=Instant.parse("2026-11-26T00:00:00Z")
        for((index,request) in listOf(input,invalid).withIndex()) {
            val result=core.calculate(CalculationModelId(id),request,outputType)
            if(index==0) assertIs<CalculationResult.Success<O>>(result) else assertIs<CalculationResult.Failure>(result)
            val output=if(result is CalculationResult.Success) assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(oc.encode(result.value)).value else null
            val diagnostics=when(result) { is CalculationResult.Success -> result.diagnostics;is CalculationResult.Failure -> result.diagnostics }
            val snapshot=CalculationSnapshot(CalculationModelId(id),"1.0.0",assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(ic.encode(request)).value,output,if(index==0) CalculationStatus.SUCCESS else CalculationStatus.FAILURE,now,now,diagnostics.map(SnapshotDiagnostic::from))
            val record=CalculationRecord(CalculationRecordId("$id-$index"),1,snapshot,now)
            assertIs<RepositoryResult.Success<Unit>>(repository.insert(record))
            val restored=assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(record.id)).value!!
            assertEquals(request,assertIs<SnapshotCodecResult.Success<I>>(ic.decode(restored.snapshot.input)).value)
            assertEquals(snapshot.diagnostics,restored.snapshot.diagnostics)
            if(result is CalculationResult.Success) assertEquals(result.value,assertIs<SnapshotCodecResult.Success<O>>(oc.decode(restored.snapshot.output!!)).value)
            else assertNull(restored.snapshot.output)
        }
    }
    private fun sealed()=SealedHousingLegacyInput(reference.ambient,reference.source,reference.heatedZone,reference.criticalElement,reference.elementWithRadiatorArea)
    @Test fun `sealed actual success and failure persist`() {
        val input=sealed()
        exercise("sealed-housing-legacy",input,input.copy(source=input.source.copy(power=Quantity(0.0,StandardUnits.WATT))),SealedHousingLegacyInputCodec(),SealedHousingLegacyOutputCodec(),SealedHousingLegacyOutput::class)
    }
    @Test fun `mixing actual success and failure persist`() {
        val boundary=assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(core.calculate(CalculationModelId("sealed-housing-legacy"),sealed(),SealedHousingLegacyOutput::class)).value
        val input=InternalMixingLegacyInput(SealedHousingBoundarySnapshot(reference.ambient,reference.source,boundary.enclosureRise,boundary.enclosureTemperature),reference.heatedZone,reference.criticalElement,reference.elementWithRadiatorArea)
        val invalid=input.copy(sealedHousing=input.sealedHousing.copy(source=reference.source.copy(power=Quantity(0.0,StandardUnits.WATT))))
        exercise("internal-mixing-legacy",input,invalid,InternalMixingLegacyInputCodec(),InternalMixingLegacyOutputCodec(),InternalMixingLegacyOutput::class)
    }
    @Test fun `external actual success and failure persist`() {
        val input=ExternalCoolingLegacyInput(reference.ambient,reference.source,reference.heatedZone,reference.criticalElement,reference.elementWithRadiatorArea)
        exercise("external-cooling-legacy",input,input.copy(source=input.source.copy(power=Quantity(0.0,StandardUnits.WATT))),ExternalCoolingLegacyInputCodec(),ExternalCoolingLegacyOutputCodec(),ExternalCoolingLegacyOutput::class)
    }
    @Test fun `finned actual success and failure persist`() {
        exercise("finned-housing-legacy",reference,reference.copy(source=reference.source.copy(power=Quantity(0.0,StandardUnits.WATT))),FinnedHousingLegacyInputCodec(),FinnedHousingLegacyOutputCodec(),FinnedHousingLegacyOutput::class)
    }
}

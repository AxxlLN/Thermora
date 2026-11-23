package com.thermora.infrastructure.persistence.memory

import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.application.ports.record.*
import com.thermora.application.ports.snapshot.*
import com.thermora.application.ports.testing.RecordFixtures
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import kotlin.test.*

class CoreHistoryIntegrationTest {
    private fun input(power: Double) = SealedHousingLegacyInput(
        AmbientConditions(Temperature.fromKelvin(313.0)), ThermalSource(Quantity(power, StandardUnits.WATT)),
        HeatSourceGeometry(Quantity(0.112, StandardUnits.METER), Quantity(0.069, StandardUnits.METER),
            Quantity(0.0055, StandardUnits.METER), 0.5),
        CriticalElementThermalProperties(Quantity(1.25, StandardUnits.WATT),
            ThermalResistance(Quantity(5.0, StandardUnits.KELVIN_PER_WATT)),
            ThermalResistance(Quantity(0.5, StandardUnits.KELVIN_PER_WATT))),
        Quantity(0.00423, StandardUnits.SQUARE_METER)
    )

    @Test fun `real Core success retains Foundation values diagnostics and independently versioned payloads`() {
        val core = ThermoraCoreFactory.createDefault()
        val id = CalculationModelId("sealed-housing-legacy")
        val descriptor = assertNotNull(core.findModel(id))
        val result = assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(core.calculate(id, input(4.3), SealedHousingLegacyOutput::class))
        assertEquals(323.8726142430732, result.value.enclosureTemperature.kelvin, 1e-9)
        val snapshot = CalculationSnapshot(id, descriptor.version,
            SnapshotPayload(SnapshotSchema("test.text.input", 2), "power=4.3 W;ambient=313 K"),
            SnapshotPayload(SnapshotSchema("test.text.output", 7), "caseKelvin=${result.value.enclosureTemperature.kelvin}"),
            CalculationStatus.SUCCESS, RecordFixtures.time, RecordFixtures.time,
            result.diagnostics.map(SnapshotDiagnostic::from))
        val record = CalculationRecord(CalculationRecordId("core-success"), 3, snapshot, RecordFixtures.time, "test-build")
        val repository: CalculationRecordRepository = InMemoryCalculationRecordRepository()
        assertIs<RepositoryResult.Success<Unit>>(repository.insert(record))
        val stored = assertNotNull(assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(record.id)).value)
        assertEquals(result.diagnostics, stored.snapshot.diagnostics.map { it.toDiagnostic() })
        assertEquals("caseKelvin=${result.value.enclosureTemperature.kelvin}", stored.snapshot.output!!.content)
        assertEquals(snapshot.input, stored.snapshot.input)
    }

    @Test fun `real Core validation failure can be stored without inventing an output or installed model dependency`() {
        val core = ThermoraCoreFactory.createDefault()
        val id = CalculationModelId("sealed-housing-legacy")
        val result = assertIs<CalculationResult.Failure>(core.calculate(id, input(0.0), SealedHousingLegacyOutput::class))
        val snapshot = CalculationSnapshot(id, assertNotNull(core.findModel(id)).version,
            SnapshotPayload(SnapshotSchema("test.text.input", 1), "power=0 W"), null,
            CalculationStatus.FAILURE, RecordFixtures.time, RecordFixtures.time,
            result.diagnostics.map(SnapshotDiagnostic::from))
        val repository = InMemoryCalculationRecordRepository()
        val record = CalculationRecord(CalculationRecordId("core-failure"), 1, snapshot, RecordFixtures.time)
        assertIs<RepositoryResult.Success<Unit>>(repository.insert(record))
        val restored = assertNotNull(assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(record.id)).value)
        assertEquals(result.diagnostics, restored.snapshot.diagnostics.map { it.toDiagnostic() })
        assertNull(restored.snapshot.output)
        assertNull(ThermoraCoreFactory.create(emptyList()).findModel(restored.snapshot.modelId))
        assertEquals(record, restored)
    }
}

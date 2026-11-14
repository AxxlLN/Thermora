
package com.thermora.calculation.engine.integration

import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.engine.execution.CalculationEngine
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SealedHousingLegacyIntegrationTest {

    private val modelId = CalculationModelId("sealed-housing-legacy")

    private fun referenceInput() = SealedHousingLegacyInput(
        ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        ),
        source = ThermalSource(
            Quantity(4.3, StandardUnits.WATT)
        ),
        heatedZone = HeatSourceGeometry(
            length = Quantity(0.112, StandardUnits.METER),
            width = Quantity(0.069, StandardUnits.METER),
            height = Quantity(0.0055, StandardUnits.METER),
            volumeFillCoefficient = 0.5
        ),
        criticalElement = CriticalElementThermalProperties(
            power = Quantity(1.25, StandardUnits.WATT),
            transitionToCaseResistance = ThermalResistance(
                Quantity(5.0, StandardUnits.KELVIN_PER_WATT)
            ),
            caseToRadiatorResistance = ThermalResistance(
                Quantity(0.5, StandardUnits.KELVIN_PER_WATT)
            )
        ),
        elementWithRadiatorArea = Quantity(
            0.00423,
            StandardUnits.SQUARE_METER
        )
    )

    @Test
    fun `registers and executes sealed housing model`() {
        val registry = CalculationModelRegistry()
        val model = SealedHousingLegacyModel()

        val registration = registry.register(model)
        assertIs<CalculationResult.Success<*>>(registration)

        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = modelId,
            input = referenceInput(),
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        val success =
            assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)

        assertEquals(
            323.8726142430732,
            success.value.enclosureTemperature.kelvin,
            1e-9
        )

        assertEquals(
            351.09906309240495,
            success.value.junctionTemperature.kelvin,
            1e-9
        )

        assertTrue(
            success.diagnostics.any {
                it.code == "SEALED_LEGACY_APPLICABILITY_UNKNOWN"
            }
        )
    }

    @Test
    fun `returns failure when model is not registered`() {
        val registry = CalculationModelRegistry()
        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = modelId,
            input = referenceInput(),
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertTrue(
            failure.diagnostics.any {
                it.code == "CALCULATION_MODEL_NOT_FOUND"
            }
        )
    }

    @Test
    fun `rejects duplicate model registration`() {
        val registry = CalculationModelRegistry()
        val model = SealedHousingLegacyModel()

        assertIs<CalculationResult.Success<*>>(
            registry.register(model)
        )

        val secondRegistration = registry.register(model)

        val failure =
            assertIs<CalculationResult.Failure>(secondRegistration)

        assertTrue(
            failure.diagnostics.any {
                it.code == "CALCULATION_MODEL_ALREADY_REGISTERED"
            }
        )
    }
}

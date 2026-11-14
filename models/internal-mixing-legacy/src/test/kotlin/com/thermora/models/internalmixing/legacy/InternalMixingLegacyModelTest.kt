package com.thermora.models.internalmixing.legacy

import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import com.thermora.models.internalmixing.legacy.input.SealedHousingBoundarySnapshot
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class InternalMixingLegacyModelTest {

    private val model = InternalMixingLegacyModel()

    private fun referenceInput(
        powerWatts: Double = 4.3,
        elementPowerWatts: Double = 1.25
    ): InternalMixingLegacyInput {

        val ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        )

        val enclosureRise =
            TemperatureDelta.fromKelvin(10.8726142430732)

        return InternalMixingLegacyInput(
            sealedHousing = SealedHousingBoundarySnapshot(
                ambient = ambient,
                source = ThermalSource(
                    Quantity(powerWatts, StandardUnits.WATT)
                ),
                enclosureRise = enclosureRise,
                enclosureTemperature = Temperature.fromKelvin(
                    ambient.temperature.kelvin + enclosureRise.kelvin
                )
            ),
            heatedZone = HeatSourceGeometry(
                length = Quantity(0.112, StandardUnits.METER),
                width = Quantity(0.069, StandardUnits.METER),
                height = Quantity(0.0055, StandardUnits.METER),
                volumeFillCoefficient = 0.5
            ),
            criticalElement = CriticalElementThermalProperties(
                power = Quantity(elementPowerWatts, StandardUnits.WATT),
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
    }

    @Test
    fun `reproduces all seven Excel reference outputs`() {
        val result = model.calculate(referenceInput())

        val success =
            assertIs<CalculationResult.Success<*>>(result)

        val output =
            assertIs<com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput>(
                success.value
            )

        assertEquals(
            24.282434969840896,
            output.heatedZoneRise.kelvin,
            1e-9
        )

        assertEquals(
            5.6470778999629996,
            output.heatedZoneToAmbientResistance.value.baseValue,
            1e-9
        )

        assertEquals(
            323.8726142430732,
            output.enclosureTemperature.kelvin,
            1e-9
        )

        assertEquals(
            337.2824349698409,
            output.heatedZoneTemperature.kelvin,
            1e-9
        )

        assertEquals(
            330.577524606457,
            output.internalAirTemperature.kelvin,
            1e-9
        )

        assertEquals(
            338.07522029292744,
            output.elementTemperature.kelvin,
            1e-9
        )

        assertEquals(
            344.1574349698409,
            output.junctionTemperature.kelvin,
            1e-9
        )
    }

    @Test
    fun `returns both legacy warnings exactly once`() {
        val result = model.calculate(referenceInput())
        val success = assertIs<CalculationResult.Success<*>>(result)

        assertEquals(
            setOf(
                "INTERNAL_MIXING_LEGACY_APPLICABILITY_UNKNOWN",
                "INTERNAL_MIXING_LEGACY_JUNCTION_USES_ZONE"
            ),
            success.diagnostics.map { it.code }.toSet()
        )

        assertEquals(2, success.diagnostics.size)

        assertTrue(
            success.diagnostics.all {
                it.severity == DiagnosticSeverity.WARNING
            }
        )
    }

    @Test
    fun `invalid input returns failure without calculation`() {
        val result = model.calculate(
            referenceInput(powerWatts = 0.0)
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertEquals(
            "INVALID_TOTAL_POWER",
            failure.diagnostics.first().code
        )
    }

    @Test
    fun `accepts element power exceeding total power`() {
        val result = model.calculate(
            referenceInput(elementPowerWatts = 5.0)
        )

        assertIs<CalculationResult.Success<*>>(result)
    }

    @Test
    fun `model exposes correct metadata`() {
        assertEquals(
            "internal-mixing-legacy",
            model.descriptor.id.value
        )

        assertEquals("1.0.0", model.descriptor.version)

        assertEquals(
            InternalMixingLegacyInput::class,
            model.inputType
        )
    }
    @Test
    fun `C07 changing element area changes element temperature but preserves junction temperature`() {
        val smallerAreaInput = referenceInput().copy(
            elementWithRadiatorArea = Quantity(0.002, StandardUnits.SQUARE_METER)
        )
        val largerAreaInput = smallerAreaInput.copy(
            elementWithRadiatorArea = Quantity(0.008, StandardUnits.SQUARE_METER)
        )
        listOf(smallerAreaInput, largerAreaInput).forEach { input ->
            val validation = model.validate(input)
            assertTrue(validation.isValid)
            assertTrue(validation.diagnostics.isEmpty())
        }

        val smallerArea = assertIs<CalculationResult.Success<InternalMixingLegacyOutput>>(model.calculate(smallerAreaInput))
        val largerArea = assertIs<CalculationResult.Success<InternalMixingLegacyOutput>>(model.calculate(largerAreaInput))

        assertNotEquals(smallerArea.value.elementTemperature, largerArea.value.elementTemperature)
        assertTrue(smallerArea.value.elementTemperature.kelvin > largerArea.value.elementTemperature.kelvin)
        assertEquals(smallerArea.value.junctionTemperature, largerArea.value.junctionTemperature)
        assertEquals(
            smallerArea.value.heatedZoneTemperature.kelvin + 5.5 * 1.25,
            smallerArea.value.junctionTemperature.kelvin,
            1e-9
        )
        assertEquals(smallerArea.diagnostics, largerArea.diagnostics)
        assertTrue(smallerArea.diagnostics.none { it.severity == DiagnosticSeverity.ERROR })
    }

}

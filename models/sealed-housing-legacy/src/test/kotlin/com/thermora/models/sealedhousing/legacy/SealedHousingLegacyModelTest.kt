package com.thermora.models.sealedhousing.legacy

import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureUnit
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SealedHousingLegacyModelTest {

    private val model = SealedHousingLegacyModel()

    private fun referenceInput(
        powerWatts: Double = 4.3,
        elementPowerWatts: Double = 1.25
    ) = SealedHousingLegacyInput(
        ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        ),
        source = ThermalSource(
            Quantity(powerWatts, StandardUnits.WATT)
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

    @Test
    fun `matches Excel reference temperatures`() {
        val result = assertIs<CalculationResult.Success<*>>(
            model.calculate(referenceInput())
        )

        val output =
            assertIs<com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput>(
                result.value
            )

        assertEquals(
            323.8726142430732,
            output.enclosureTemperature.kelvin,
            1e-9
        )
        assertEquals(
            343.2368742000398,
            output.heatedZoneTemperature.kelvin,
            1e-9
        )
        assertEquals(
            333.55474422155646,
            output.internalAirTemperature.kelvin,
            1e-9
        )
        assertEquals(
            344.22406309240495,
            output.elementTemperature.kelvin,
            1e-9
        )
        assertEquals(
            351.09906309240495,
            output.junctionTemperature.kelvin,
            1e-9
        )
    }

    @Test
    fun `matches Excel reference rises and resistances`() {
        val result = assertIs<CalculationResult.Success<*>>(
            model.calculate(referenceInput())
        )

        val output =
            assertIs<com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput>(
                result.value
            )

        assertEquals(
            10.872614243073194,
            output.enclosureRise.kelvin,
            1e-9
        )
        assertEquals(
            30.236874200039768,
            output.heatedZoneRise.kelvin,
            1e-9
        )
        assertEquals(
            2.52851494024958,
            output.enclosureToAmbientResistance.value.baseValue,
            1e-9
        )
        assertEquals(
            7.031831209311575,
            output.heatedZoneToAmbientResistance.value.baseValue,
            1e-9
        )
    }

    @Test
    fun `converts Kelvin to Celsius using 273 point 15`() {
        val result = assertIs<CalculationResult.Success<*>>(
            model.calculate(referenceInput())
        )

        val output =
            assertIs<com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput>(
                result.value
            )

        assertEquals(
            50.7226142430732,
            output.enclosureTemperature.inUnit(TemperatureUnit.CELSIUS),
            1e-9
        )
    }

    @Test
    fun `rejects zero total power`() {
        val result = model.calculate(
            referenceInput(powerWatts = 0.0)
        )

        assertIs<CalculationResult.Failure>(result)
    }

    @Test
    fun `allows zero critical element power`() {
        val result = model.calculate(
            referenceInput(elementPowerWatts = 0.0)
        )

        assertIs<CalculationResult.Success<*>>(result)
    }

    @Test
    fun `reports unknown applicability of empirical correlations`() {
        val result = assertIs<CalculationResult.Success<*>>(
            model.calculate(referenceInput())
        )

        assertTrue(
            result.diagnostics.any {
                it.code == "SEALED_LEGACY_APPLICABILITY_UNKNOWN"
            }
        )
    }

    @Test
    fun `supports geometry dimensions in millimeters`() {
        val original = referenceInput()

        val millimeterInput = original.copy(
            heatedZone = HeatSourceGeometry(
                length = Quantity(112.0, StandardUnits.MILLIMETER),
                width = Quantity(69.0, StandardUnits.MILLIMETER),
                height = Quantity(5.5, StandardUnits.MILLIMETER),
                volumeFillCoefficient = 0.5
            )
        )

        val first = assertIs<CalculationResult.Success<*>>(
            model.calculate(original)
        )
        val second = assertIs<CalculationResult.Success<*>>(
            model.calculate(millimeterInput)
        )

        val firstOutput =
            assertIs<com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput>(
                first.value
            )
        val secondOutput =
            assertIs<com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput>(
                second.value
            )

        assertEquals(
            firstOutput.elementTemperature.kelvin,
            secondOutput.elementTemperature.kelvin,
            1e-9
        )
    }


    @Test
    fun `handles extremely large finite power`() {
        val original = referenceInput()

        val input = original.copy(
            source = ThermalSource(
                Quantity(1e300, StandardUnits.WATT)
            )
        )

        val result = SealedHousingLegacyModel().calculate(input)

        val success =
            assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)

        val output = success.value

        assertTrue(output.enclosureTemperature.kelvin.isFinite())
        assertTrue(output.heatedZoneTemperature.kelvin.isFinite())
        assertTrue(output.internalAirTemperature.kelvin.isFinite())
        assertTrue(output.elementTemperature.kelvin.isFinite())
        assertTrue(output.junctionTemperature.kelvin.isFinite())

        assertTrue(
            success.diagnostics.any {
                it.code == "SEALED_LEGACY_APPLICABILITY_UNKNOWN"
            }
        )
    }

    @Test
    fun `rejects numerical overflow in element calculation`() {
        val original = referenceInput()

        val input = original.copy(
            criticalElement = original.criticalElement.copy(
                power = Quantity(1e308, StandardUnits.WATT)
            ),
            heatedZone = original.heatedZone.copy(
                length = Quantity(1e308, StandardUnits.METER)
            )
        )

        val result = SealedHousingLegacyModel().calculate(input)

        val failure = assertIs<CalculationResult.Failure>(result)

        assertTrue(failure.diagnostics.any {
            it.severity == DiagnosticSeverity.ERROR
        })
    }

    @Test
    fun `handles extremely small positive power`() {
        val original = referenceInput()

        val input = original.copy(
            source = ThermalSource(
                Quantity(1e-300, StandardUnits.WATT)
            )
        )

        val result = SealedHousingLegacyModel().calculate(input)

        assertTrue(
            result is CalculationResult.Success ||
                    result is CalculationResult.Failure
        )
    }

    @Test
    fun `does not produce invalid results for extremely small power`() {
        val original = referenceInput()

        val input = original.copy(
            source = ThermalSource(
                Quantity(1e-300, StandardUnits.WATT)
            )
        )

        val result = SealedHousingLegacyModel().calculate(input)

        when (result) {
            is CalculationResult.Success -> {
                assertTrue(result.value.enclosureTemperature.kelvin.isFinite())
                assertTrue(result.value.heatedZoneTemperature.kelvin.isFinite())
                assertTrue(result.value.elementTemperature.kelvin.isFinite())
                assertTrue(result.value.junctionTemperature.kelvin.isFinite())
                assertTrue(result.value.enclosureToAmbientResistance.value.baseValue.isFinite())
                assertTrue(result.value.heatedZoneToAmbientResistance.value.baseValue.isFinite())
            }

            is CalculationResult.Failure -> {
                assertTrue(result.diagnostics.isNotEmpty())
            }
        }
    }
}

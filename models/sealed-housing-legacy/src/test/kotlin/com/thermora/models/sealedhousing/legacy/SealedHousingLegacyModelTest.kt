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
import com.thermora.models.sealedhousing.legacy.internal.LegacyTemperatureArithmetic
import java.math.BigDecimal
import kotlin.test.assertFailsWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.math.pow

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

    private fun output(input: SealedHousingLegacyInput): SealedHousingLegacyOutput =
        assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(model.calculate(input)).value

    private fun assertRelative(expected: Double, actual: Double) {
        assertEquals(expected, actual, kotlin.math.abs(expected) * 5e-14)
    }

    @Test
    fun `preserves small zone rise in element temperature`() {
        val input = referenceInput(1e-18, 2.5e-19).copy(
            elementWithRadiatorArea = Quantity(1e-18, StandardUnits.SQUARE_METER)
        )

        val result = output(input)

        assertEquals(313.0, result.heatedZoneTemperature.kelvin)
        assertEquals(316.11820680196945, result.elementTemperature.kelvin, 1e-12)
    }

    @Test
    fun `averages large finite absolute temperatures without overflow`() {
        val input = referenceInput(elementPowerWatts = 0.0).copy(
            ambient = AmbientConditions(Temperature.fromKelvin(1e308))
        )

        val result = output(input)

        assertEquals(1e308, result.internalAirTemperature.kelvin)
        assertEquals(1e308, result.elementTemperature.kelvin)
    }

    @Test
    fun `zero element power adds no heat even when resistance sum overflows`() {
        val original = referenceInput(elementPowerWatts = 0.0)
        val input = original.copy(
            criticalElement = original.criticalElement.copy(
                transitionToCaseResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT)),
                caseToRadiatorResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT))
            )
        )

        val result = output(input)

        assertEquals(335.67765565002985, result.elementTemperature.kelvin, 1e-9)
        assertEquals(result.elementTemperature.kelvin, result.junctionTemperature.kelvin)
    }

    @Test
    fun `small element power scales resistance sum before final rounding`() {
        val original = referenceInput(elementPowerWatts = 1e-308)
        val input = original.copy(
            criticalElement = original.criticalElement.copy(
                transitionToCaseResistance = ThermalResistance(Quantity(1e308, StandardUnits.KELVIN_PER_WATT)),
                caseToRadiatorResistance = ThermalResistance(Quantity(1e308, StandardUnits.KELVIN_PER_WATT))
            )
        )

        val result = output(input)

        assertEquals(337.67765565002985, result.junctionTemperature.kelvin, 1e-9)
    }

    @Test
    fun `small denominator product does not destroy finite power ratio result`() {
        val input = referenceInput(1e-200, 2.5e-201).copy(
            ambient = AmbientConditions(Temperature.fromKelvin(0.0)),
            elementWithRadiatorArea = Quantity(1e-200, StandardUnits.SQUARE_METER)
        )

        assertRelative(1.873758401297329e26, output(input).elementTemperature.kelvin)
    }

    @Test
    fun `subnormal powers and area preserve ratio beyond Double range`() {
        val input = referenceInput(Double.MIN_VALUE, Double.MIN_VALUE).copy(
            elementWithRadiatorArea = Quantity(Double.MIN_VALUE, StandardUnits.SQUARE_METER)
        )

        assertRelative(2.3641975726979445e44, output(input).elementTemperature.kelvin)
    }

    @Test
    fun `subnormal numerator and denominator products retain significant heating`() {
        val input = referenceInput(1e-300, Double.MIN_VALUE).copy(
            elementWithRadiatorArea = Quantity(Double.MIN_VALUE, StandardUnits.SQUARE_METER)
        )

        assertRelative(1.1607584547107664e41, output(input).elementTemperature.kelvin)
    }

    @Test
    fun `small numerator product is not silently rounded to zero`() {
        val input = referenceInput(1e-300, Double.MIN_VALUE).copy(
            ambient = AmbientConditions(Temperature.fromKelvin(0.0)),
            elementWithRadiatorArea = Quantity(1e-18, StandardUnits.SQUARE_METER)
        )

        assertRelative(2.1166917657843664e-257, output(input).elementTemperature.kelvin)
    }

    @Test
    fun `zone area below Double range can contribute to representable result`() {
        val original = referenceInput(1e-100, 1e100)
        val input = original.copy(
            heatedZone = original.heatedZone.copy(
                length = Quantity(1e-200, StandardUnits.METER),
                width = Quantity(1e-200, StandardUnits.METER),
                height = Quantity(1e-200, StandardUnits.METER)
            ),
            elementWithRadiatorArea = Quantity(Double.MIN_VALUE, StandardUnits.SQUARE_METER)
        )

        val result = output(input)

        assertRelative(2.4746881171686004e38, result.elementTemperature.kelvin)
        assertRelative(5.5e100, result.junctionTemperature.kelvin)
    }

    @Test
    fun `zone area above Double range can contribute to representable result`() {
        val original = referenceInput(elementPowerWatts = 1e-300)
        val input = original.copy(
            heatedZone = original.heatedZone.copy(
                length = Quantity(1e308, StandardUnits.METER),
                width = Quantity(1e308, StandardUnits.METER)
            ),
            elementWithRadiatorArea = Quantity(1e308, StandardUnits.SQUARE_METER)
        )

        assertRelative(351591896.1432344, output(input).elementTemperature.kelvin)
    }

    @Test
    fun `unrepresentable junction temperature returns diagnostic failure`() {
        val original = referenceInput()
        val input = original.copy(
            criticalElement = original.criticalElement.copy(
                transitionToCaseResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT)),
                caseToRadiatorResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT))
            )
        )

        val result = assertIs<CalculationResult.Failure>(model.calculate(input))

        assertEquals("SEALED_LEGACY_CALCULATION_ERROR", result.diagnostics.single().code)
        assertEquals(DiagnosticSeverity.ERROR, result.diagnostics.single().severity)
    }

    @Test
    fun `preserves temperatures for realistic parameter sets`() {
        data class Case(
            val power: Double, val elementPower: Double, val area: Double,
            val length: Double, val width: Double, val height: Double, val ambient: Double,
            val expected: List<Double>
        )
        val cases = listOf(
            Case(1.0, 0.1, 0.002, 0.1, 0.08, 0.01, 293.15,
                listOf(296.2509, 301.7922, 299.02155, 301.5545395, 302.1045395)),
            Case(20.0, 2.0, 0.01, 0.2, 0.15, 0.02, 298.15,
                listOf(338.9352999204335, 411.3180689709274, 375.1266844456805, 401.9817032808259, 412.9817032808259)),
            Case(80.0, 5.0, 0.03, 0.3, 0.2, 0.05, 323.15,
                listOf(457.53025437181157, 695.2593038303526, 576.3947791010821, 630.3339825891192, 657.8339825891192))
        )
        cases.forEach { case ->
            val original = referenceInput(case.power, case.elementPower)
            val input = original.copy(
                ambient = AmbientConditions(Temperature.fromKelvin(case.ambient)),
                heatedZone = original.heatedZone.copy(
                    length = Quantity(case.length, StandardUnits.METER),
                    width = Quantity(case.width, StandardUnits.METER),
                    height = Quantity(case.height, StandardUnits.METER)
                ),
                elementWithRadiatorArea = Quantity(case.area, StandardUnits.SQUARE_METER)
            )
            val result = output(input)
            val actual = listOf(result.enclosureTemperature.kelvin, result.heatedZoneTemperature.kelvin,
                result.internalAirTemperature.kelvin, result.elementTemperature.kelvin, result.junctionTemperature.kelvin)
            case.expected.zip(actual).forEach { (expected, value) -> assertEquals(expected, value, 1e-9) }
            val previousCaseTemperature = case.ambient + 3.1009 * case.power.pow(0.8601)
            val previousZoneTemperature = case.ambient +
                (5.5413 * case.power.pow(0.8578) + 3.1009 * case.power.pow(0.8601))
            val previousArea = 2.0 * (case.length * case.width + case.height * 0.5 * (case.length + case.width))
            val previousElementTemperature = case.ambient + (previousZoneTemperature - case.ambient) *
                (0.75 + 0.25 * (case.elementPower * previousArea / (case.area * case.power)))
            val previous = listOf(
                previousCaseTemperature, previousZoneTemperature,
                (previousCaseTemperature + previousZoneTemperature) / 2.0,
                previousElementTemperature, previousElementTemperature + (5.0 + 0.5) * case.elementPower
            )
            previous.zip(actual).forEach { (before, after) -> assertEquals(before, after, 1e-9) }
        }
    }

    @Test
    fun `mean handles both signs cancellation subnormals and signed zero`() {
        val cases = listOf(
            Double.MAX_VALUE to Double.MAX_VALUE,
            -Double.MAX_VALUE to -Double.MAX_VALUE,
            Double.MAX_VALUE to -Double.MAX_VALUE,
            Double.MAX_VALUE to -1e308,
            -Double.MAX_VALUE to 1e308,
            1e308 to 1.7e308,
            -1e308 to -1.7e308,
            Double.MIN_VALUE to Double.MIN_VALUE,
            Double.MIN_VALUE to 2.0 * Double.MIN_VALUE,
            -Double.MIN_VALUE to -2.0 * Double.MIN_VALUE,
            Double.MIN_VALUE to -Double.MIN_VALUE,
            0.0 to Double.MIN_VALUE
        )
        cases.forEach { (first, second) ->
            val expected = BigDecimal(first).add(BigDecimal(second)).divide(BigDecimal(2)).toDouble()
            assertEquals(expected, LegacyTemperatureArithmetic.mean(first, second))
            assertEquals(expected, LegacyTemperatureArithmetic.mean(second, first))
        }
        assertEquals((-0.0).toRawBits(), LegacyTemperatureArithmetic.mean(-0.0, -0.0).toRawBits())
        assertEquals(0.0.toRawBits(), LegacyTemperatureArithmetic.mean(-0.0, 0.0).toRawBits())
    }

    @Test
    fun `mean rejects non finite arguments`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { LegacyTemperatureArithmetic.mean(value, 1.0) }
            assertFailsWith<IllegalArgumentException> { LegacyTemperatureArithmetic.mean(1.0, value) }
        }
    }

    @Test
    fun `equivalent power area length and temperature units preserve all outputs`() {
        val original = referenceInput()
        val converted = original.copy(
            ambient = AmbientConditions(Temperature.of(39.85, TemperatureUnit.CELSIUS)),
            source = ThermalSource(Quantity(0.0043, StandardUnits.KILOWATT)),
            heatedZone = original.heatedZone.copy(
                length = Quantity(112.0, StandardUnits.MILLIMETER),
                width = Quantity(69.0, StandardUnits.MILLIMETER),
                height = Quantity(5.5, StandardUnits.MILLIMETER)
            ),
            criticalElement = original.criticalElement.copy(power = Quantity(0.00125, StandardUnits.KILOWATT)),
            elementWithRadiatorArea = Quantity(42.3, StandardUnits.SQUARE_CENTIMETER)
        )
        fun values(result: SealedHousingLegacyOutput) = listOf(
            result.enclosureTemperature.kelvin, result.heatedZoneTemperature.kelvin,
            result.internalAirTemperature.kelvin, result.elementTemperature.kelvin,
            result.junctionTemperature.kelvin, result.enclosureRise.kelvin, result.heatedZoneRise.kelvin,
            result.enclosureToAmbientResistance.value.baseValue, result.heatedZoneToAmbientResistance.value.baseValue
        )

        values(output(original)).zip(values(output(converted))).forEach { (expected, actual) ->
            assertEquals(expected, actual, 1e-9)
        }
    }

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

        val success = assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)
        assertRelative(3.430137277514085e43, success.value.elementTemperature.kelvin)
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

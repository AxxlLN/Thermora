package com.thermora.models.externalcooling.legacy

import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExternalCoolingBoundaryTest {
    private val model = ExternalCoolingLegacyModel()

    private fun input(
        power: Double = 4.3,
        elementPower: Double = 1.25,
        area: Double = 0.00423,
        ambient: Double = 313.0,
        length: Double = 0.112,
        width: Double = 0.069,
        height: Double = 0.0055,
        fill: Double = 0.5,
        resistance: Double = 5.0,
        radiatorResistance: Double = 0.5
    ) = ExternalCoolingLegacyInput(
        AmbientConditions(Temperature.fromKelvin(ambient)),
        ThermalSource(Quantity(power, StandardUnits.WATT)),
        HeatSourceGeometry(Quantity(length, StandardUnits.METER), Quantity(width, StandardUnits.METER),
            Quantity(height, StandardUnits.METER), fill),
        CriticalElementThermalProperties(Quantity(elementPower, StandardUnits.WATT),
            ThermalResistance(Quantity(resistance, StandardUnits.KELVIN_PER_WATT)),
            ThermalResistance(Quantity(radiatorResistance, StandardUnits.KELVIN_PER_WATT))),
        Quantity(area, StandardUnits.SQUARE_METER)
    )

    private fun output(input: ExternalCoolingLegacyInput): ExternalCoolingLegacyOutput =
        assertIs<CalculationResult.Success<ExternalCoolingLegacyOutput>>(model.calculate(input)).value

    private fun assertRelative(expected: Double, actual: Double) =
        assertEquals(expected, actual, abs(expected) * 1e-13)

    private fun assertCalculationFailure(input: ExternalCoolingLegacyInput) {
        val failure = assertIs<CalculationResult.Failure>(model.calculate(input))
        assertEquals("EXTERNAL_COOLING_CALCULATION_ERROR", failure.diagnostics.single().code)
        assertEquals(DiagnosticSeverity.ERROR, failure.diagnostics.single().severity)
        assertEquals("external-cooling-legacy", failure.diagnostics.single().source)
    }

    @Test
    fun `invalid area and zero power return the original validation diagnostics`() {
        val wrongDimension = input().copy(elementWithRadiatorArea = Quantity(1.0, StandardUnits.WATT))
        listOf(input(power = 0.0), input(area = 0.0), input(area = -1.0), wrongDimension).forEach { request ->
            val validation = model.validate(request)
            assertTrue(!validation.isValid)
            val failure = assertIs<CalculationResult.Failure>(model.calculate(request))
            assertEquals(validation.diagnostics, failure.diagnostics)
            assertEquals(DiagnosticSeverity.ERROR, failure.diagnostics.single().severity)
            assertEquals(if (request.source.power.baseValue == 0.0) "EXTERNAL_COOLING_INVALID_TOTAL_POWER"
                else "EXTERNAL_COOLING_INVALID_ELEMENT_AREA", failure.diagnostics.single().code)
        }
    }

    @Test
    fun `nonfinite values and negative powers fail during domain construction`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { input(power = value) }
            assertFailsWith<IllegalArgumentException> { input(elementPower = value) }
            assertFailsWith<IllegalArgumentException> { input(area = value) }
            assertFailsWith<IllegalArgumentException> { input(ambient = value) }
        }
        assertFailsWith<IllegalArgumentException> { input(power = -1.0) }
        assertFailsWith<IllegalArgumentException> { input(elementPower = -1.0) }
        assertFailsWith<IllegalArgumentException> { input(ambient = -1.0) }
    }

    @Test
    fun `invalid dimensions and fill coefficients fail during geometry construction`() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { input(length = value) }
            assertFailsWith<IllegalArgumentException> { input(width = value) }
            assertFailsWith<IllegalArgumentException> { input(height = value) }
            assertFailsWith<IllegalArgumentException> { input(fill = value) }
        }
        assertFailsWith<IllegalArgumentException> { input(fill = Math.nextUp(1.0)) }
        assertFailsWith<IllegalArgumentException> {
            HeatSourceGeometry(Quantity(1.0, StandardUnits.WATT), Quantity(1.0, StandardUnits.METER),
                Quantity(1.0, StandardUnits.METER), 0.5)
        }
    }

    @Test
    fun `invalid resistances fail during domain construction`() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { input(resistance = value) }
            assertFailsWith<IllegalArgumentException> { input(radiatorResistance = value) }
        }
    }

    @Test
    fun `both allowed fill boundaries remain calculable`() {
        listOf(Double.MIN_VALUE, 1.0).forEach { fill ->
            val request = input(fill = fill)
            assertTrue(model.validate(request).isValid)
            assertTrue(output(request).elementTemperature.kelvin.isFinite())
        }
    }

    @Test
    fun `zero element power retains the baseline element temperature`() {
        val result = output(input(elementPower = 0.0))
        assertEquals(332.76876544450477, result.elementTemperature.kelvin, 1e-12)
        assertEquals(result.elementTemperature, result.junctionTemperature)
    }

    @Test
    fun `large absolute temperatures have a representable mean`() {
        val result = output(input(ambient = Double.MAX_VALUE, elementPower = 0.0))
        assertEquals(Double.MAX_VALUE, result.internalAirTemperature.kelvin)
        assertEquals(Double.MAX_VALUE, result.elementTemperature.kelvin)
        assertEquals(Double.MAX_VALUE, result.junctionTemperature.kelvin)
    }

    @Test
    fun `zero element power avoids an overflowing resistance sum`() {
        val result = output(input(elementPower = 0.0, resistance = Double.MAX_VALUE,
            radiatorResistance = Double.MAX_VALUE))
        assertEquals(332.76876544450477, result.junctionTemperature.kelvin, 1e-12)
    }

    @Test
    fun `a small power retains heating from very large resistances`() {
        val result = output(input(elementPower = 1e-308, resistance = 1e308, radiatorResistance = 1e308))
        assertEquals(334.76876544450477, result.junctionTemperature.kelvin, 1e-12)
    }

    @Test
    fun `underflowing denominator retains a representable element temperature`() {
        assertRelative(2.2761420610640523e26,
            output(input(power = 1e-200, elementPower = 2.5e-201, area = 1e-200)).elementTemperature.kelvin)
    }

    @Test
    fun `overflowing correction retains a representable element temperature`() {
        assertRelative(9.235803804902314e235,
            output(input(power = 1e-100, elementPower = 1e-100, area = Double.MIN_VALUE)).elementTemperature.kelvin)
    }

    @Test
    fun `area below Double range retains its thermal contribution`() {
        val result = output(input(power = 1e-100, elementPower = 1e100, area = Double.MIN_VALUE,
            length = 1e-200, width = 1e-200, height = 1e-200))
        assertRelative(2.245583394803468e38, result.elementTemperature.kelvin)
        assertRelative(5.5e100, result.junctionTemperature.kelvin)
    }

    @Test
    fun `area above Double range can still produce a finite temperature`() {
        val result = output(input(elementPower = 1e-300, area = 1e308, length = 1e308, width = 1e308))
        assertEquals(306492820.28046685, result.elementTemperature.kelvin, 1e-6)
    }

    @Test
    fun `subnormal powers and areas preserve their ratios`() {
        assertRelative(3.908842301799933e44,
            output(input(power = Double.MIN_VALUE, elementPower = Double.MIN_VALUE,
                area = Double.MIN_VALUE)).elementTemperature.kelvin)
    }

    @Test
    fun `a small zone rise survives rounding of its absolute temperature`() {
        val result = output(input(power = 1e-24, elementPower = 2.5e-25, area = 1e-18))
        assertEquals(313.0, result.heatedZoneTemperature.kelvin)
        assertEquals(313.00001547180744, result.elementTemperature.kelvin, 1e-12)
    }

    @Test
    fun `unrepresentable temperatures return diagnostic failures`() {
        assertCalculationFailure(input(area = Double.MIN_VALUE))
        assertCalculationFailure(input(resistance = Double.MAX_VALUE, radiatorResistance = Double.MAX_VALUE))
        assertCalculationFailure(input(power = 1e308, ambient = Double.MAX_VALUE, elementPower = 0.0))
    }

    @Test
    fun `maximum finite power remains calculable when all results are representable`() {
        val result = output(input(power = Double.MAX_VALUE, elementPower = 0.0))
        assertRelative(4.6313210539503104e297, result.enclosureRise.kelvin)
        assertRelative(4.6313210539503104e297, result.heatedZoneRise.kelvin)
        assertRelative(2.5762578518747143e-11, result.enclosureToAmbientResistance.value.baseValue)
        assertRelative(2.5762578518747143e-11, result.heatedZoneToAmbientResistance.value.baseValue)
        assertRelative(3.473490790462733e297, result.junctionTemperature.kelvin)
    }

    @Test
    fun `legacy accepts element power above total power without inventing a new restriction`() {
        val request = input(elementPower = 5.0)
        assertTrue(model.validate(request).isValid)
        assertTrue(output(request).elementTemperature.kelvin > output(input()).elementTemperature.kelvin)
    }

    @Test
    fun `equivalent input units preserve all output values`() {
        val original = input()
        val converted = original.copy(
            source = ThermalSource(original.source.power.convertTo(StandardUnits.KILOWATT)),
            heatedZone = original.heatedZone.copy(
                length = original.heatedZone.length.convertTo(StandardUnits.MILLIMETER),
                width = original.heatedZone.width.convertTo(StandardUnits.MILLIMETER),
                height = original.heatedZone.height.convertTo(StandardUnits.MILLIMETER)),
            criticalElement = original.criticalElement.copy(
                power = original.criticalElement.power.convertTo(StandardUnits.KILOWATT)),
            elementWithRadiatorArea = original.elementWithRadiatorArea.convertTo(StandardUnits.SQUARE_CENTIMETER)
        )
        val first = output(original)
        val second = output(converted)
        listOf(first.enclosureTemperature.kelvin to second.enclosureTemperature.kelvin,
            first.heatedZoneTemperature.kelvin to second.heatedZoneTemperature.kelvin,
            first.internalAirTemperature.kelvin to second.internalAirTemperature.kelvin,
            first.elementTemperature.kelvin to second.elementTemperature.kelvin,
            first.junctionTemperature.kelvin to second.junctionTemperature.kelvin,
            first.enclosureRise.kelvin to second.enclosureRise.kelvin,
            first.heatedZoneRise.kelvin to second.heatedZoneRise.kelvin,
            first.enclosureToAmbientResistance.value.baseValue to second.enclosureToAmbientResistance.value.baseValue,
            first.heatedZoneToAmbientResistance.value.baseValue to second.heatedZoneToAmbientResistance.value.baseValue
        ).forEach { (expected, actual) -> assertEquals(expected, actual, 1e-9) }
    }

    @Test
    fun `successful results retain an applicability warning`() {
        val success = assertIs<CalculationResult.Success<ExternalCoolingLegacyOutput>>(model.calculate(input()))
        assertEquals("EXTERNAL_COOLING_LEGACY_APPLICABILITY_UNKNOWN", success.diagnostics.single().code)
        assertEquals(DiagnosticSeverity.WARNING, success.diagnostics.single().severity)
        assertEquals("external-cooling-legacy", success.diagnostics.single().source)
    }

    @Test
    fun `realistic parameter sets retain independently calculated temperatures`() {
        val cases = listOf(
            input(power = 1.0, elementPower = 0.1, area = 0.002, length = 0.1,
                width = 0.08, height = 0.01, ambient = 293.15) to
                listOf(294.8511, 300.41159999999996, 297.63135, 300.211906, 300.76190599999995),
            input(power = 20.0, elementPower = 2.0, area = 0.01, length = 0.2,
                width = 0.15, height = 0.02, ambient = 298.15) to
                listOf(328.7762404148179, 401.23594370952145, 365.00609206216967,
                    392.73135335348593, 403.73135335348593),
            input(power = 80.0, elementPower = 5.0, area = 0.03, length = 0.3,
                width = 0.2, height = 0.05, ambient = 323.15) to
                listOf(439.83670409224635, 677.5546472749185, 558.6956756835824,
                    615.7184197555968, 643.2184197555968)
        )
        cases.forEach { (request, expected) ->
            val result = output(request)
            val actual = listOf(result.enclosureTemperature.kelvin, result.heatedZoneTemperature.kelvin,
                result.internalAirTemperature.kelvin, result.elementTemperature.kelvin, result.junctionTemperature.kelvin)
            expected.zip(actual).forEach { (reference, value) -> assertEquals(reference, value, 1e-9) }
        }
    }
}

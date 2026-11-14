package com.thermora.models.internalmixing.legacy

import com.thermora.foundation.common.result.CalculationResult
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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InternalMixingNumericalRegressionTest {

    private val model = InternalMixingLegacyModel()

    private fun input(
        totalPower: Double = 4.3,
        elementPower: Double = 1.25,
        elementArea: Double = 0.00423,
        transitionResistance: Double = 5.0,
        radiatorResistance: Double = 0.5,
        length: Double = 0.112,
        width: Double = 0.069,
        height: Double = 0.0055
    ): InternalMixingLegacyInput {
        val enclosureRise = 3.1009 * totalPower.pow(0.8601)
        return InternalMixingLegacyInput(
            sealedHousing = SealedHousingBoundarySnapshot(
                ambient = AmbientConditions(Temperature.fromKelvin(313.0)),
                source = ThermalSource(Quantity(totalPower, StandardUnits.WATT)),
                enclosureRise = TemperatureDelta.fromKelvin(enclosureRise),
                enclosureTemperature = Temperature.fromKelvin(313.0 + enclosureRise)
            ),
            heatedZone = HeatSourceGeometry(
                Quantity(length, StandardUnits.METER),
                Quantity(width, StandardUnits.METER),
                Quantity(height, StandardUnits.METER),
                0.5
            ),
            criticalElement = CriticalElementThermalProperties(
                Quantity(elementPower, StandardUnits.WATT),
                ThermalResistance(Quantity(transitionResistance, StandardUnits.KELVIN_PER_WATT)),
                ThermalResistance(Quantity(radiatorResistance, StandardUnits.KELVIN_PER_WATT))
            ),
            elementWithRadiatorArea = Quantity(elementArea, StandardUnits.SQUARE_METER)
        )
    }

    private fun output(input: InternalMixingLegacyInput): InternalMixingLegacyOutput =
        assertIs<CalculationResult.Success<InternalMixingLegacyOutput>>(model.calculate(input)).value

    private fun assertRelative(expected: Double, actual: Double) {
        assertEquals(expected, actual, abs(expected) * 5e-14)
    }

    @Test
    fun `TH-02 retains zone rise below the absolute temperature resolution`() {
        val result = output(input(totalPower = 1e-24, elementPower = 2.5e-25, elementArea = 1e-18))

        assertEquals(313.0, result.heatedZoneTemperature.kelvin)
        assertEquals(314.3963673387772, result.elementTemperature.kelvin, 1e-12)
    }

    @Test
    fun `TH-03 zero element power avoids overflowing area ratio`() {
        val result = output(input(elementPower = 0.0, elementArea = Double.MIN_VALUE))

        assertEquals(331.2118262273807, result.elementTemperature.kelvin, 1e-12)
        assertEquals(result.heatedZoneTemperature, result.junctionTemperature)
    }

    @Test
    fun `TH-03 overflowing partial ratio retains representable element temperature`() {
        val result = output(input(totalPower = 1e300, elementArea = Double.MIN_VALUE))

        assertRelative(3.4574716490957476e279, result.elementTemperature.kelvin)
    }

    @Test
    fun `TH-03 area below Double range retains its contribution to temperature`() {
        val result = output(input(
            totalPower = 1e-100,
            elementPower = 1e100,
            elementArea = Double.MIN_VALUE,
            length = 1e-200,
            width = 1e-200,
            height = 1e-200
        ))

        assertRelative(1.2368790876163407e59, result.elementTemperature.kelvin)
        assertRelative(5.5e100, result.junctionTemperature.kelvin)
    }

    @Test
    fun `TH-04 zero element power adds no junction heat with maximum resistances`() {
        val result = output(input(
            elementPower = 0.0,
            transitionResistance = Double.MAX_VALUE,
            radiatorResistance = Double.MAX_VALUE
        ))

        assertEquals(337.2824349698409, result.junctionTemperature.kelvin, 1e-12)
        assertEquals(result.heatedZoneTemperature, result.junctionTemperature)
    }

    @Test
    fun `large resistance sum can produce a finite nonzero junction rise`() {
        val result = output(input(
            elementPower = 1e-308,
            transitionResistance = 1e308,
            radiatorResistance = 1e308
        ))

        assertEquals(339.2824349698409, result.junctionTemperature.kelvin, 1e-12)
    }

    @Test
    fun `unrepresentable element temperature returns diagnostic failure`() {
        val result = assertIs<CalculationResult.Failure>(model.calculate(input(elementArea = Double.MIN_VALUE)))

        assertEquals("INTERNAL_MIXING_CALCULATION_ERROR", result.diagnostics.single().code)
    }

    @Test
    fun `unrepresentable junction temperature returns diagnostic failure`() {
        val result = assertIs<CalculationResult.Failure>(model.calculate(input(
            transitionResistance = Double.MAX_VALUE,
            radiatorResistance = Double.MAX_VALUE
        )))

        assertEquals("INTERNAL_MIXING_CALCULATION_ERROR", result.diagnostics.single().code)
    }
}

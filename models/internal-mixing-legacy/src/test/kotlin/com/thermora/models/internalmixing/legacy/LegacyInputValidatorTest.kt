package com.thermora.models.internalmixing.legacy

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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegacyInputValidatorTest {

    private val model = InternalMixingLegacyModel()

    private fun referenceInput(
        powerWatts: Double = 4.3,
        elementPowerWatts: Double = 1.25
    ): InternalMixingLegacyInput {

        val ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        )

        val source = ThermalSource(
            Quantity(powerWatts, StandardUnits.WATT)
        )

        val enclosureRise = TemperatureDelta.fromKelvin(10.8726142430732)

        return InternalMixingLegacyInput(
            sealedHousing = SealedHousingBoundarySnapshot(
                ambient = ambient,
                source = source,
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

    private fun assertInvalid(
        input: InternalMixingLegacyInput,
        expectedCode: String
    ) {
        val report = model.validate(input)

        assertFalse(report.isValid)
        assertEquals(
            expectedCode,
            report.diagnostics.first().code
        )
    }

    @Test
    fun `accepts valid reference input`() {
        val report = model.validate(referenceInput())

        assertTrue(report.isValid)
        assertTrue(report.diagnostics.isEmpty())
    }

    @Test
    fun `rejects zero total power`() {
        assertInvalid(
            referenceInput(powerWatts = 0.0),
            "INVALID_TOTAL_POWER"
        )
    }

    @Test
    fun `rejects zero element area`() {
        val input = referenceInput().copy(
            elementWithRadiatorArea = Quantity(
                0.0,
                StandardUnits.SQUARE_METER
            )
        )

        assertInvalid(input, "INVALID_ELEMENT_AREA")
    }

    @Test
    fun `accepts element power exceeding total power for legacy compatibility`() {
        val input = referenceInput(
            powerWatts = 4.3,
            elementPowerWatts = 5.0
        )

        val report = model.validate(input)

        assertTrue(report.isValid)
        assertTrue(report.diagnostics.isEmpty())
    }

    @Test
    fun `rejects inconsistent enclosure temperature`() {
        val original = referenceInput()

        val input = original.copy(
            sealedHousing = original.sealedHousing.copy(
                enclosureTemperature = Temperature.fromKelvin(330.0)
            )
        )

        assertInvalid(
            input,
            "INCONSISTENT_ENCLOSURE_TEMPERATURE"
        )
    }

    @Test
    fun `accepts small floating point temperature deviation`() {
        val original = referenceInput()

        val expected =
            original.sealedHousing.enclosureTemperature.kelvin

        val input = original.copy(
            sealedHousing = original.sealedHousing.copy(
                enclosureTemperature = Temperature.fromKelvin(
                    expected + 1e-11
                )
            )
        )

        assertTrue(model.validate(input).isValid)
    }

    @Test
    fun `accepts zero critical element power`() {
        assertTrue(
            model.validate(
                referenceInput(elementPowerWatts = 0.0)
            ).isValid
        )
    }

    @Test
    fun `accepts critical element power equal to total power`() {
        assertTrue(
            model.validate(
                referenceInput(elementPowerWatts = 4.3)
            ).isValid
        )
    }
}

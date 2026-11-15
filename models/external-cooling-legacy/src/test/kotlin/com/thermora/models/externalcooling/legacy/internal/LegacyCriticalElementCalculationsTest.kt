package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCriticalElementCalculationsTest {

    @Test
    fun `heated zone area matches Excel`() {
        val result = LegacyHeatedZoneArea.calculate(
            lengthMeters = 0.112,
            widthMeters = 0.069,
            heightMeters = 0.0055,
            volumeFillCoefficient = 0.5
        )

        assertEquals(0.0164515, result.toDouble(), 1e-10)
    }

    @Test
    fun `element temperature matches Excel AA30`() {
        val result = LegacyElementTemperature.calculate(
            ambient = Temperature.fromKelvin(313.0),
            heatedZoneRise = TemperatureDelta.fromKelvin(
                26.35835392600632
            ),
            elementPowerWatts = 1.25,
            heatedZoneAreaSquareMeters = BigDecimal(0.0164515),
            elementAreaSquareMeters = 0.00423,
            totalPowerWatts = 4.3
        )

        assertEquals(
            340.21891491007619,
            result.kelvin,
            1e-9
        )
    }

    @Test
    fun `junction temperature matches Excel AA31`() {
        val elementTemperature = Temperature.fromKelvin(
            340.21891491007619
        )

        val result = LegacyJunctionTemperature.calculate(
            elementTemperature = elementTemperature,
            elementPowerWatts = 1.25,
            junctionToCaseResistance = 5.0,
            caseToRadiatorResistance = 0.5
        )

        assertEquals(
            347.09391491007619,
            result.kelvin,
            1e-9
        )
    }
}

package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTemperatureCalculatorTest {

    private val ambient = Temperature.fromKelvin(313.0)

    private val enclosureRise =
        TemperatureDelta.fromKelvin(6.9496599897105886)

    private val heatedZoneRise =
        TemperatureDelta.fromKelvin(26.35835392600632)

    @Test
    fun `enclosure temperature matches Excel AA27`() {
        val result = LegacyTemperatureCalculator.enclosureTemperature(
            ambient,
            enclosureRise
        )

        assertEquals(
            319.9496599897106,
            result.kelvin,
            1e-9
        )
    }

    @Test
    fun `heated zone temperature matches Excel AA28`() {
        val result = LegacyTemperatureCalculator.heatedZoneTemperature(
            ambient,
            heatedZoneRise
        )

        assertEquals(
            339.35835392600632,
            result.kelvin,
            1e-9
        )
    }

    @Test
    fun `internal air temperature matches Excel AA29`() {
        val enclosure =
            LegacyTemperatureCalculator.enclosureTemperature(
                ambient,
                enclosureRise
            )

        val heatedZone =
            LegacyTemperatureCalculator.heatedZoneTemperature(
                ambient,
                heatedZoneRise
            )

        val result = LegacyTemperatureCalculator.internalAirTemperature(
            enclosure,
            heatedZone
        )

        assertEquals(
            329.65400695785843,
            result.kelvin,
            1e-9
        )
    }
}

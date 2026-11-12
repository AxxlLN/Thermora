package com.thermora.foundation.domain.environment

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class AmbientConditionsTest {

    @Test
    fun `should accept ambient temperature`() {
        val conditions = AmbientConditions(
            temperature = Temperature.of(
                313.0,
                TemperatureUnit.KELVIN
            )
        )

        assertEquals(313.0, conditions.temperature.kelvin)
    }

    @Test
    fun `should accept temperature in Celsius`() {
        val conditions = AmbientConditions(
            temperature = Temperature.of(
                25.0,
                TemperatureUnit.CELSIUS
            )
        )

        assertEquals(
            298.15,
            conditions.temperature.kelvin,
            1e-10
        )
    }
}
package com.thermora.foundation.units.temperature

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemperatureOverflowRegressionTest {

    private fun assertRelative(expected: Double, actual: Double) {
        assertEquals(expected, actual, abs(expected) * 1e-15)
    }

    @Test
    fun `TH-06 large Fahrenheit temperature has representable Kelvin value`() {
        assertRelative(5.555555555555556e307, Temperature.of(1e308, TemperatureUnit.FAHRENHEIT).kelvin)
    }

    @Test
    fun `TH-06 large Kelvin temperature has representable Fahrenheit value`() {
        assertRelative(1.62e308, Temperature.fromKelvin(9e307).inUnit(TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun `TH-06 large Fahrenheit differences convert in both directions of heat flow`() {
        listOf(1.0, -1.0).forEach { sign ->
            assertRelative(sign * 5.555555555555556e307,
                TemperatureDelta.of(sign * 1e308, TemperatureUnit.FAHRENHEIT).kelvin)
        }
    }

    @Test
    fun `TH-06 large Kelvin differences have representable Fahrenheit values`() {
        listOf(1.0, -1.0).forEach { sign ->
            assertRelative(sign * 1.62e308,
                TemperatureDelta.fromKelvin(sign * 9e307).inUnit(TemperatureUnit.FAHRENHEIT))
        }
    }

    @Test
    fun `unrepresentable Fahrenheit results still fail`() {
        assertFailsWith<IllegalArgumentException> {
            Temperature.fromKelvin(Double.MAX_VALUE).inUnit(TemperatureUnit.FAHRENHEIT)
        }
        listOf(Double.MAX_VALUE, -Double.MAX_VALUE).forEach { value ->
            assertFailsWith<IllegalArgumentException> {
                TemperatureDelta.fromKelvin(value).inUnit(TemperatureUnit.FAHRENHEIT)
            }
        }
    }

    @Test
    fun `Fahrenheit differences retain subnormal values and signed zero`() {
        assertEquals(Double.MIN_VALUE, TemperatureDelta.of(Double.MIN_VALUE, TemperatureUnit.FAHRENHEIT).kelvin)
        assertEquals(2.0 * Double.MIN_VALUE,
            TemperatureDelta.fromKelvin(Double.MIN_VALUE).inUnit(TemperatureUnit.FAHRENHEIT))
        assertEquals((-0.0).toRawBits(), TemperatureDelta.of(-0.0, TemperatureUnit.FAHRENHEIT).kelvin.toRawBits())
    }
}

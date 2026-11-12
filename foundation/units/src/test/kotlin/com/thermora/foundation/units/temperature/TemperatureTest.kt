package com.thermora.foundation.units.temperature

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemperatureTest {

    @Test
    fun `should convert Celsius to Kelvin`() {
        val temperature = Temperature.of(25.0, TemperatureUnit.CELSIUS)

        assertEquals(298.15, temperature.kelvin, 1e-10)
    }

    @Test
    fun `should convert Fahrenheit to Celsius`() {
        val temperature = Temperature.of(77.0, TemperatureUnit.FAHRENHEIT)

        assertEquals(25.0, temperature.inUnit(TemperatureUnit.CELSIUS), 1e-10)
    }

    @Test
    fun `should reject temperature below absolute zero`() {
        assertFailsWith<IllegalArgumentException> {
            Temperature.of(-274.0, TemperatureUnit.CELSIUS)
        }
    }

    @Test
    fun `should add temperature delta`() {
        val temperature = Temperature.of(20.0, TemperatureUnit.CELSIUS)
        val delta = TemperatureDelta.of(10.0, TemperatureUnit.CELSIUS)

        val result = temperature + delta

        assertEquals(30.0, result.inUnit(TemperatureUnit.CELSIUS), 1e-10)
    }

    @Test
    fun `should subtract two absolute temperatures`() {
        val first = Temperature.of(30.0, TemperatureUnit.CELSIUS)
        val second = Temperature.of(20.0, TemperatureUnit.CELSIUS)

        val delta = first - second

        assertEquals(10.0, delta.kelvin, 1e-10)
    }

    @Test
    fun `should reject subtraction below absolute zero`() {
        val temperature = Temperature.of(10.0, TemperatureUnit.CELSIUS)
        val delta = TemperatureDelta.of(300.0, TemperatureUnit.CELSIUS)

        assertFailsWith<IllegalArgumentException> {
            temperature - delta
        }
    }

    @Test
    fun `should convert Fahrenheit temperature delta correctly`() {
        val delta = TemperatureDelta.of(18.0, TemperatureUnit.FAHRENHEIT)

        assertEquals(10.0, delta.kelvin, 1e-10)
    }

    @Test
    fun `should allow negative temperature delta`() {
        val delta = TemperatureDelta.of(-10.0, TemperatureUnit.CELSIUS)

        assertEquals(-10.0, delta.kelvin, 1e-10)
    }

    @Test
    fun `should reject NaN`() {
        assertFailsWith<IllegalArgumentException> {
            Temperature.of(Double.NaN, TemperatureUnit.KELVIN)
        }
    }

    @Test
    fun `should reject infinite temperature delta`() {
        assertFailsWith<IllegalArgumentException> {
            TemperatureDelta.of(Double.POSITIVE_INFINITY, TemperatureUnit.KELVIN)
        }
    }


    @Test
    fun `should convert Fahrenheit to Kelvin`() {
        val temperature = Temperature.of(32.0, TemperatureUnit.FAHRENHEIT)

        assertEquals(273.15, temperature.kelvin, 1e-10)
    }

    @Test
    fun `should convert Kelvin to Fahrenheit`() {
        val temperature = Temperature.fromKelvin(273.15)

        assertEquals(
            32.0,
            temperature.inUnit(TemperatureUnit.FAHRENHEIT),
            1e-10
        )
    }

    @Test
    fun `should add temperature deltas`() {
        val first = TemperatureDelta.of(10.0, TemperatureUnit.CELSIUS)
        val second = TemperatureDelta.of(5.0, TemperatureUnit.CELSIUS)

        assertEquals(15.0, (first + second).kelvin, 1e-10)
    }

    @Test
    fun `should subtract temperature deltas`() {
        val first = TemperatureDelta.of(10.0, TemperatureUnit.CELSIUS)
        val second = TemperatureDelta.of(15.0, TemperatureUnit.CELSIUS)

        assertEquals(-5.0, (first - second).kelvin, 1e-10)
    }

    @Test
    fun `should convert Fahrenheit delta to Celsius delta`() {
        val delta = TemperatureDelta.of(18.0, TemperatureUnit.FAHRENHEIT)

        assertEquals(
            10.0,
            delta.inUnit(TemperatureUnit.CELSIUS),
            1e-10
        )
    }

//    @Test
//    fun `should reject temperature delta division by zero`() {
//        val delta = TemperatureDelta.of(10.0, TemperatureUnit.KELVIN)
//
//        assertFailsWith<IllegalArgumentException> {
//            delta / 0.0
//        }
//    }

}
package com.thermora.foundation.units.quantity

import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QuantityTest {

    @Test
    fun `should convert millimeters to meters`() {
        val quantity = Quantity(
            1000.0,
            StandardUnits.MILLIMETER
        )

        val result = quantity.convertTo(StandardUnits.METER)

        assertEquals(1.0, result.value, 1e-10)
    }

    @Test
    fun `should convert Celsius to Kelvin`() {
        val quantity = Quantity(
            25.0,
            StandardUnits.CELSIUS
        )

        val result = quantity.convertTo(StandardUnits.KELVIN)

        assertEquals(298.15, result.value, 1e-10)
    }

    @Test
    fun `should convert Kelvin to Celsius`() {
        val quantity = Quantity(
            298.15,
            StandardUnits.KELVIN
        )

        val result = quantity.convertTo(StandardUnits.CELSIUS)

        assertEquals(25.0, result.value, 1e-10)
    }

    @Test
    fun `should reject incompatible dimensions`() {
        val quantity = Quantity(
            100.0,
            StandardUnits.METER
        )

        assertFailsWith<IllegalArgumentException> {
            quantity.convertTo(StandardUnits.KILOGRAM)
        }
    }

    @Test
    fun `should convert kilopascals to pascals`() {
        val quantity = Quantity(
            100.0,
            StandardUnits.KILOPASCAL
        )

        val result = quantity.convertTo(StandardUnits.PASCAL)

        assertEquals(100000.0, result.value, 1e-10)
    }
}

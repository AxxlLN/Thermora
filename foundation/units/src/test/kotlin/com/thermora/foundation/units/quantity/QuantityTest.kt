
package com.thermora.foundation.units.quantity

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QuantityTest {

    @Test
    fun `should convert millimeters to meters`() {
        val quantity = Quantity(1000.0, StandardUnits.MILLIMETER)

        val result = quantity.convertTo(StandardUnits.METER)

        assertEquals(1.0, result.value, 1e-10)
    }

    @Test
    fun `should add quantities with different units`() {
        val first = Quantity(1.0, StandardUnits.METER)
        val second = Quantity(50.0, StandardUnits.CENTIMETER)

        val result = first + second

        assertEquals(1.5, result.value, 1e-10)
        assertEquals(StandardUnits.METER, result.unit)
    }

    @Test
    fun `should subtract quantities with different units`() {
        val first = Quantity(1.0, StandardUnits.METER)
        val second = Quantity(25.0, StandardUnits.CENTIMETER)

        val result = first - second

        assertEquals(0.75, result.value, 1e-10)
    }

    @Test
    fun `should reject addition of incompatible dimensions`() {
        val first = Quantity(1.0, StandardUnits.METER)
        val second = Quantity(1.0, StandardUnits.KILOGRAM)

        assertFailsWith<IllegalArgumentException> {
            first + second
        }
    }

    @Test
    fun `should multiply lengths and produce area`() {
        val first = Quantity(2.0, StandardUnits.METER)
        val second = Quantity(3.0, StandardUnits.METER)

        val result = first * second

        assertEquals(6.0, result.value, 1e-10)
        assertEquals(PhysicalDimension.AREA, result.unit.dimension)
    }

    @Test
    fun `should divide energy by time and produce power`() {
        val energy = Quantity(10.0, StandardUnits.JOULE)
        val time = Quantity(2.0, StandardUnits.SECOND)

        val result = energy / time

        assertEquals(5.0, result.value, 1e-10)
        assertEquals(PhysicalDimension.POWER, result.unit.dimension)
    }

    @Test
    fun `should multiply quantity by scalar`() {
        val quantity = Quantity(5.0, StandardUnits.METER)

        val result = quantity * 3.0

        assertEquals(15.0, result.value, 1e-10)
        assertEquals(quantity.unit, result.unit)
    }

    @Test
    fun `should divide quantity by scalar`() {
        val quantity = Quantity(10.0, StandardUnits.METER)

        val result = quantity / 2.0

        assertEquals(5.0, result.value, 1e-10)
    }

    @Test
    fun `should negate quantity`() {
        val quantity = Quantity(5.0, StandardUnits.METER)

        assertEquals(-5.0, (-quantity).value, 1e-10)
    }

    @Test
    fun `should convert kilopascals to pascals`() {
        val quantity = Quantity(100.0, StandardUnits.KILOPASCAL)

        val result = quantity.convertTo(StandardUnits.PASCAL)

        assertEquals(100000.0, result.value, 1e-10)
    }

    @Test
    fun `should reject affine units`() {
        val celsius = MeasurementUnit(
            symbol = "°C",
            dimension = PhysicalDimension.TEMPERATURE,
            scaleToBase = 1.0,
            offsetToBase = 273.15
        )

        assertFailsWith<IllegalArgumentException> {
            Quantity(25.0, celsius)
        }
    }

    @Test
    fun `should reject standalone temperature dimension`() {
        val kelvin = MeasurementUnit(
            symbol = "K",
            dimension = PhysicalDimension.TEMPERATURE,
            scaleToBase = 1.0
        )

        assertFailsWith<IllegalArgumentException> {
            Quantity(298.15, kelvin)
        }
    }
}

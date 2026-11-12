
package com.thermora.foundation.units.unit

import com.thermora.foundation.units.dimension.PhysicalDimension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StandardUnitsTest {

    @Test
    fun `should define meter as base length unit`() {
        assertEquals(PhysicalDimension.LENGTH, StandardUnits.METER.dimension)
        assertEquals(1.0, StandardUnits.METER.scaleToBase)
    }

    @Test
    fun `should define millimeter correctly`() {
        assertEquals(PhysicalDimension.LENGTH, StandardUnits.MILLIMETER.dimension)
        assertEquals(1e-3, StandardUnits.MILLIMETER.scaleToBase)
    }

    @Test
    fun `should define centimeter correctly`() {
        assertEquals(PhysicalDimension.LENGTH, StandardUnits.CENTIMETER.dimension)
        assertEquals(1e-2, StandardUnits.CENTIMETER.scaleToBase)
    }

    @Test
    fun `should define square meter correctly`() {
        assertEquals(PhysicalDimension.AREA, StandardUnits.SQUARE_METER.dimension)
        assertEquals(1.0, StandardUnits.SQUARE_METER.scaleToBase)
    }

    @Test
    fun `should define square centimeter correctly`() {
        assertEquals(PhysicalDimension.AREA, StandardUnits.SQUARE_CENTIMETER.dimension)
        assertEquals(1e-4, StandardUnits.SQUARE_CENTIMETER.scaleToBase)
    }

    @Test
    fun `should define cubic meter correctly`() {
        assertEquals(PhysicalDimension.VOLUME, StandardUnits.CUBIC_METER.dimension)
        assertEquals(1.0, StandardUnits.CUBIC_METER.scaleToBase)
    }

    @Test
    fun `should define watt with power dimension`() {
        assertEquals(PhysicalDimension.POWER, StandardUnits.WATT.dimension)
        assertEquals(1.0, StandardUnits.WATT.scaleToBase)
    }

    @Test
    fun `should define only linear units`() {
        val units = listOf(
            StandardUnits.METER,
            StandardUnits.MILLIMETER,
            StandardUnits.CENTIMETER,
            StandardUnits.SQUARE_METER,
            StandardUnits.SQUARE_CENTIMETER,
            StandardUnits.CUBIC_METER,
            StandardUnits.WATT
        )

        units.forEach { unit ->
            assertEquals(0.0, unit.offsetToBase)
            assertTrue(unit.scaleToBase.isFinite())
            assertTrue(unit.scaleToBase > 0.0)
        }
    }
}

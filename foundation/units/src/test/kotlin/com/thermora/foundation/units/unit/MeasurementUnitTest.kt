
package com.thermora.foundation.units.unit

import com.thermora.foundation.units.dimension.PhysicalDimension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MeasurementUnitTest {

    @Test
    fun `should create valid measurement unit`() {
        val unit = MeasurementUnit(
            symbol = "mm",
            dimension = PhysicalDimension.LENGTH,
            scaleToBase = 1e-3
        )

        assertEquals("mm", unit.symbol)
        assertEquals(PhysicalDimension.LENGTH, unit.dimension)
        assertEquals(1e-3, unit.scaleToBase)
    }

    @Test
    fun `should reject blank symbol`() {
        assertFailsWith<IllegalArgumentException> {
            MeasurementUnit(
                symbol = " ",
                dimension = PhysicalDimension.LENGTH,
                scaleToBase = 1.0
            )
        }
    }

    @Test
    fun `should reject non-positive scale`() {
        assertFailsWith<IllegalArgumentException> {
            MeasurementUnit("m", PhysicalDimension.LENGTH, 0.0)
        }

        assertFailsWith<IllegalArgumentException> {
            MeasurementUnit("m", PhysicalDimension.LENGTH, -1.0)
        }
    }

    @Test
    fun `should reject non-finite scale`() {
        assertFailsWith<IllegalArgumentException> {
            MeasurementUnit("m", PhysicalDimension.LENGTH, Double.NaN)
        }

        assertFailsWith<IllegalArgumentException> {
            MeasurementUnit("m", PhysicalDimension.LENGTH, Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun `should reject non-finite offset`() {
        assertFailsWith<IllegalArgumentException> {
            MeasurementUnit(
                "custom",
                PhysicalDimension.LENGTH,
                1.0,
                Double.NaN
            )
        }
    }

    @Test
    fun `should convert value to base unit`() {
        val millimeter = MeasurementUnit(
            "mm",
            PhysicalDimension.LENGTH,
            1e-3
        )

        assertEquals(1.0, millimeter.toBase(1000.0), 1e-12)
    }

    @Test
    fun `should convert value from base unit`() {
        val millimeter = MeasurementUnit(
            "mm",
            PhysicalDimension.LENGTH,
            1e-3
        )

        assertEquals(1000.0, millimeter.fromBase(1.0), 1e-10)
    }

    @Test
    fun `should reject non-finite conversion input`() {
        assertFailsWith<IllegalArgumentException> {
            StandardUnits.METER.toBase(Double.NaN)
        }

        assertFailsWith<IllegalArgumentException> {
            StandardUnits.METER.fromBase(Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun `should reject conversion overflow`() {
        val largeUnit = MeasurementUnit(
            "large",
            PhysicalDimension.LENGTH,
            2.0
        )

        assertFailsWith<IllegalArgumentException> {
            largeUnit.toBase(Double.MAX_VALUE)
        }

        val tinyUnit = MeasurementUnit(
            "tiny",
            PhysicalDimension.LENGTH,
            1e-308
        )

        assertFailsWith<IllegalArgumentException> {
            tinyUnit.fromBase(Double.MAX_VALUE)
        }
    }

    @Test
    fun `should multiply compatible unit dimensions`() {
        val squareMeter = StandardUnits.METER * StandardUnits.METER

        assertEquals(PhysicalDimension.AREA, squareMeter.dimension)
        assertEquals(1.0, squareMeter.scaleToBase)
    }

    @Test
    fun `should divide compatible unit dimensions`() {
        val velocity = StandardUnits.METER / StandardUnits.SECOND

        assertEquals(PhysicalDimension.VELOCITY, velocity.dimension)
        assertEquals(1.0, velocity.scaleToBase)
    }

    @Test
    fun `should reject multiplication of affine units`() {
        val affineUnit = MeasurementUnit(
            "custom",
            PhysicalDimension.LENGTH,
            1.0,
            10.0
        )

        assertFailsWith<IllegalArgumentException> {
            affineUnit * StandardUnits.METER
        }
    }

    @Test
    fun `should reject division of affine units`() {
        val affineUnit = MeasurementUnit(
            "custom",
            PhysicalDimension.LENGTH,
            1.0,
            10.0
        )

        assertFailsWith<IllegalArgumentException> {
            StandardUnits.METER / affineUnit
        }
    }

    @Test
    fun `should reject multiplication scale overflow`() {
        val largeUnit = MeasurementUnit(
            "large",
            PhysicalDimension.LENGTH,
            1e200
        )

        assertFailsWith<IllegalArgumentException> {
            largeUnit * largeUnit
        }
    }

    @Test
    fun `should reject division scale underflow`() {
        val smallUnit = MeasurementUnit(
            "small",
            PhysicalDimension.LENGTH,
            1e-200
        )

        assertFailsWith<IllegalArgumentException> {
            smallUnit / MeasurementUnit(
                "large",
                PhysicalDimension.LENGTH,
                1e200
            )
        }
    }
}

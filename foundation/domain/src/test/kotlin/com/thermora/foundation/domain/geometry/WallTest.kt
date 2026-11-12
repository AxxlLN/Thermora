package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WallTest {

    @Test
    fun `should create wall with valid thickness`() {
        val wall = Wall(
            orientation = WallOrientation.HORIZONTAL_TOP,
            thickness = Quantity(26.5, StandardUnits.MILLIMETER)
        )

        assertEquals(WallOrientation.HORIZONTAL_TOP, wall.orientation)
        assertEquals(0.0265, wall.thickness.baseValue, 1e-12)
    }

    @Test
    fun `should reject non-positive thickness`() {
        assertFailsWith<IllegalArgumentException> {
            Wall(
                orientation = WallOrientation.HORIZONTAL_BOTTOM,
                thickness = Quantity(0.0, StandardUnits.METER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Wall(
                orientation = WallOrientation.HORIZONTAL_BOTTOM,
                thickness = Quantity(-1.0, StandardUnits.METER)
            )
        }
    }

    @Test
    fun `should reject incompatible thickness dimension`() {
        assertFailsWith<IllegalArgumentException> {
            Wall(
                orientation = WallOrientation.VERTICAL,
                thickness = Quantity(1.0, StandardUnits.WATT)
            )
        }
    }
}
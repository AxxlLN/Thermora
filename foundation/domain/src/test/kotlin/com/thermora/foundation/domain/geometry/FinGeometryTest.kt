
package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FinGeometryTest {

    private fun validGeometry() = FinGeometry(
        width = Quantity(10.0, StandardUnits.MILLIMETER),
        height = Quantity(20.0, StandardUnits.MILLIMETER),
        length = Quantity(100.0, StandardUnits.MILLIMETER),
        countPerSide = 5,
        spacing = Quantity(5.0, StandardUnits.MILLIMETER)
    )

    @Test
    fun `should create fin geometry with valid parameters`() {
        val geometry = validGeometry()

        assertEquals(0.01, geometry.width.baseValue, 1e-12)
        assertEquals(0.02, geometry.height.baseValue, 1e-12)
        assertEquals(0.1, geometry.length.baseValue, 1e-12)
        assertEquals(5, geometry.countPerSide)
        assertEquals(0.005, geometry.spacing.baseValue, 1e-12)
    }

    @Test
    fun `should reject incompatible width dimension`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                width = Quantity(10.0, StandardUnits.WATT)
            )
        }
    }

    @Test
    fun `should reject incompatible height dimension`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                height = Quantity(20.0, StandardUnits.WATT)
            )
        }
    }

    @Test
    fun `should reject incompatible length dimension`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                length = Quantity(100.0, StandardUnits.WATT)
            )
        }
    }

    @Test
    fun `should reject incompatible spacing dimension`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                spacing = Quantity(5.0, StandardUnits.WATT)
            )
        }
    }

    @Test
    fun `should reject non-positive width`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                width = Quantity(0.0, StandardUnits.MILLIMETER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                width = Quantity(-1.0, StandardUnits.MILLIMETER)
            )
        }
    }

    @Test
    fun `should reject non-positive height`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                height = Quantity(0.0, StandardUnits.MILLIMETER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                height = Quantity(-1.0, StandardUnits.MILLIMETER)
            )
        }
    }

    @Test
    fun `should reject non-positive length`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                length = Quantity(0.0, StandardUnits.MILLIMETER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                length = Quantity(-1.0, StandardUnits.MILLIMETER)
            )
        }
    }

    @Test
    fun `should reject non-positive spacing`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                spacing = Quantity(0.0, StandardUnits.MILLIMETER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                spacing = Quantity(-1.0, StandardUnits.MILLIMETER)
            )
        }
    }

    @Test
    fun `should reject non-positive fin count`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(countPerSide = 0)
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(countPerSide = -1)
        }
    }
}

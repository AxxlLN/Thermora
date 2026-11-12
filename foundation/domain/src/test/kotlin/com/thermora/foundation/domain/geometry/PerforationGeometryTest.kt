
package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PerforationGeometryTest {

    private fun validGeometry() = PerforationGeometry(
        openArea = Quantity(1.0, StandardUnits.SQUARE_CENTIMETER),
        transitionArea = Quantity(1.5, StandardUnits.SQUARE_CENTIMETER)
    )

    @Test
    fun `should create perforation geometry with valid parameters`() {
        val geometry = validGeometry()

        assertEquals(1e-4, geometry.openArea.baseValue, 1e-12)
        assertEquals(1.5e-4, geometry.transitionArea.baseValue, 1e-12)
    }

    @Test
    fun `should reject incompatible open area dimension`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                openArea = Quantity(100.0, StandardUnits.METER)
            )
        }
    }

    @Test
    fun `should reject incompatible transition area dimension`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                transitionArea = Quantity(150.0, StandardUnits.METER)
            )
        }
    }

    @Test
    fun `should reject non-positive open area`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                openArea = Quantity(0.0, StandardUnits.SQUARE_CENTIMETER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                openArea = Quantity(-1.0, StandardUnits.SQUARE_CENTIMETER)
            )
        }
    }

    @Test
    fun `should reject non-positive transition area`() {
        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                transitionArea = Quantity(0.0, StandardUnits.SQUARE_CENTIMETER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            validGeometry().copy(
                transitionArea = Quantity(-1.0, StandardUnits.SQUARE_CENTIMETER)
            )
        }
    }
}

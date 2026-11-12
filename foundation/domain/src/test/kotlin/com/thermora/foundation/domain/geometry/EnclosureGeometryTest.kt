
package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnclosureGeometryTest {

    @Test
    fun `should calculate volume in cubic meters`() {
        val enclosure = EnclosureGeometry(
            length = Quantity(121.0, StandardUnits.MILLIMETER),
            width = Quantity(78.0, StandardUnits.MILLIMETER),
            height = Quantity(39.0, StandardUnits.MILLIMETER)
        )

        val expected = 0.121 * 0.078 * 0.039

        assertEquals(
            PhysicalDimension.VOLUME,
            enclosure.volume.unit.dimension
        )
        assertEquals(
            expected,
            enclosure.volume.baseValue,
            1e-12
        )
    }

    @Test
    fun `should accept dimensions expressed in different units`() {
        val enclosure = EnclosureGeometry(
            length = Quantity(12.1, StandardUnits.CENTIMETER),
            width = Quantity(78.0, StandardUnits.MILLIMETER),
            height = Quantity(0.039, StandardUnits.METER)
        )

        assertEquals(
            0.121 * 0.078 * 0.039,
            enclosure.volume.baseValue,
            1e-12
        )
    }

    @Test
    fun `should reject non-positive dimensions`() {
        assertFailsWith<IllegalArgumentException> {
            EnclosureGeometry(
                length = Quantity(0.0, StandardUnits.METER),
                width = Quantity(1.0, StandardUnits.METER),
                height = Quantity(1.0, StandardUnits.METER)
            )
        }

        assertFailsWith<IllegalArgumentException> {
            EnclosureGeometry(
                length = Quantity(-1.0, StandardUnits.METER),
                width = Quantity(1.0, StandardUnits.METER),
                height = Quantity(1.0, StandardUnits.METER)
            )
        }
    }

    @Test
    fun `should reject quantity with incompatible dimension`() {
        assertFailsWith<IllegalArgumentException> {
            EnclosureGeometry(
                length = Quantity(1.0, StandardUnits.WATT),
                width = Quantity(1.0, StandardUnits.METER),
                height = Quantity(1.0, StandardUnits.METER)
            )
        }
    }
}

package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertFailsWith

class HeatSourceGeometryTest {

    @Test
    fun `should accept valid heat source geometry`() {
        HeatSourceGeometry(
            length = Quantity(112.0, StandardUnits.MILLIMETER),
            width = Quantity(69.0, StandardUnits.MILLIMETER),
            height = Quantity(5.5, StandardUnits.MILLIMETER),
            volumeFillCoefficient = 0.5
        )
    }

    @Test
    fun `should reject non-positive dimensions`() {
        assertFailsWith<IllegalArgumentException> {
            HeatSourceGeometry(
                length = Quantity(0.0, StandardUnits.METER),
                width = Quantity(1.0, StandardUnits.METER),
                height = Quantity(1.0, StandardUnits.METER),
                volumeFillCoefficient = 0.5
            )
        }
    }

    @Test
    fun `should reject invalid volume fill coefficient`() {
        val invalidCoefficients = listOf(
            0.0,
            -0.1,
            1.1,
            Double.NaN,
            Double.POSITIVE_INFINITY
        )

        invalidCoefficients.forEach { coefficient ->
            assertFailsWith<IllegalArgumentException> {
                HeatSourceGeometry(
                    length = Quantity(1.0, StandardUnits.METER),
                    width = Quantity(1.0, StandardUnits.METER),
                    height = Quantity(1.0, StandardUnits.METER),
                    volumeFillCoefficient = coefficient
                )
            }
        }
    }
}
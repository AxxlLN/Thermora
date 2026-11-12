package com.thermora.foundation.domain.thermal

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ThermalSourceTest {

    @Test
    fun `should accept valid power`() {
        ThermalSource(
            Quantity(4.3, StandardUnits.WATT)
        )
    }

    @Test
    fun `should reject incompatible dimension`() {
        assertFailsWith<IllegalArgumentException> {
            ThermalSource(
                Quantity(4.3, StandardUnits.METER)
            )
        }
    }

    @Test
    fun `should reject negative power`() {
        assertFailsWith<IllegalArgumentException> {
            ThermalSource(
                Quantity(-1.0, StandardUnits.WATT)
            )
        }
    }
}
package com.thermora.foundation.domain.thermal

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ThermalResistanceTest {

    @Test
    fun `should accept valid thermal resistance`() {
        ThermalResistance(
            Quantity(5.0, StandardUnits.KELVIN_PER_WATT)
        )
    }

    @Test
    fun `should reject incompatible dimension`() {
        assertFailsWith<IllegalArgumentException> {
            ThermalResistance(
                Quantity(5.0, StandardUnits.WATT)
            )
        }
    }

    @Test
    fun `should reject non-positive resistance`() {
        assertFailsWith<IllegalArgumentException> {
            ThermalResistance(
                Quantity(0.0, StandardUnits.KELVIN_PER_WATT)
            )
        }
    }
}
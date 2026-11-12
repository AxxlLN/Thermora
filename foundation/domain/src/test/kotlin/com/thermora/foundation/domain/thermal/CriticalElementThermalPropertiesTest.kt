package com.thermora.foundation.domain.thermal

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CriticalElementThermalPropertiesTest {

    @Test
    fun `should accept valid thermal properties`() {
        CriticalElementThermalProperties(
            power = Quantity(1.25, StandardUnits.WATT),
            transitionToCaseResistance = ThermalResistance(
                Quantity(5.0, StandardUnits.KELVIN_PER_WATT)
            ),
            caseToRadiatorResistance = ThermalResistance(
                Quantity(0.5, StandardUnits.KELVIN_PER_WATT)
            )
        )
    }

    @Test
    fun `should reject incompatible power dimension`() {
        assertFailsWith<IllegalArgumentException> {
            CriticalElementThermalProperties(
                power = Quantity(1.25, StandardUnits.METER),
                transitionToCaseResistance = ThermalResistance(
                    Quantity(5.0, StandardUnits.KELVIN_PER_WATT)
                ),
                caseToRadiatorResistance = ThermalResistance(
                    Quantity(0.5, StandardUnits.KELVIN_PER_WATT)
                )
            )
        }
    }
}
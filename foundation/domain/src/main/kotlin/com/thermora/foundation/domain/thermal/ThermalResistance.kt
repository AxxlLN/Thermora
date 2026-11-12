package com.thermora.foundation.domain.thermal

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class ThermalResistance(
    val value: Quantity
) {
    init {
        require(
            value.unit.dimension == PhysicalDimension.THERMAL_RESISTANCE
        ) {
            "Thermal resistance must have K/W dimension"
        }

        require(value.baseValue > 0.0) {
            "Thermal resistance must be positive"
        }
    }
}
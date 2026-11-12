package com.thermora.foundation.domain.thermal

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class ThermalSource(
    val power: Quantity
) {
    init {
        require(power.unit.dimension == PhysicalDimension.POWER) {
            "Thermal source power must have power dimension"
        }

        require(power.baseValue >= 0.0) {
            "Thermal source power cannot be negative"
        }
    }
}
package com.thermora.foundation.domain.thermal

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class CriticalElementThermalProperties(
    val power: Quantity,
    val transitionToCaseResistance: ThermalResistance,
    val caseToRadiatorResistance: ThermalResistance
) {
    init {
        require(power.unit.dimension == PhysicalDimension.POWER) {
            "Critical element power must have power dimension"
        }

        require(power.baseValue >= 0.0) {
            "Critical element power cannot be negative"
        }
    }
}
package com.thermora.foundation.domain.ventilation

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class FanSpecification(
    val nominalVolumeFlowRate: Quantity,
    val characteristicDimension: Quantity
) {
    init {
        require(
            nominalVolumeFlowRate.unit.dimension ==
                    PhysicalDimension.VOLUMETRIC_FLOW
        ) {
            "Fan volume flow rate must have volumetric flow dimension"
        }

        require(nominalVolumeFlowRate.baseValue > 0.0) {
            "Fan volume flow rate must be positive"
        }

        require(
            characteristicDimension.unit.dimension ==
                    PhysicalDimension.LENGTH
        ) {
            "Fan characteristic dimension must have length dimension"
        }

        require(characteristicDimension.baseValue > 0.0) {
            "Fan characteristic dimension must be positive"
        }
    }
}
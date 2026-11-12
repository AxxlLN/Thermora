package com.thermora.foundation.domain.ventilation

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class AirflowConditions(
    val velocity: Quantity
) {
    init {
        require(
            velocity.unit.dimension == PhysicalDimension.VELOCITY
        ) {
            "Airflow velocity must have velocity dimension"
        }

        require(velocity.baseValue >= 0.0) {
            "Airflow velocity cannot be negative"
        }
    }
}
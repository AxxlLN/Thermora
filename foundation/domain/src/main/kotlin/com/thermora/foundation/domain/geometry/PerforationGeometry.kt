package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class PerforationGeometry(
    val openArea: Quantity,
    val transitionArea: Quantity
) {
    init {
        requireArea(openArea, "openArea")
        requireArea(transitionArea, "transitionArea")

        require(openArea.baseValue > 0.0) {
            "Open area must be positive"
        }

        require(transitionArea.baseValue > 0.0) {
            "Transition area must be positive"
        }
    }

    private fun requireArea(
        quantity: Quantity,
        fieldName: String
    ) {
        require(
            quantity.unit.dimension == PhysicalDimension.AREA
        ) {
            "$fieldName must have area dimension"
        }
    }
}
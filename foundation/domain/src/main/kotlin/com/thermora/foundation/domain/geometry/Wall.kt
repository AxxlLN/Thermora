package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class Wall(
    val orientation: WallOrientation,
    val thickness: Quantity
) {
    init {
        require(
            thickness.unit.dimension == PhysicalDimension.LENGTH
        ) {
            "Wall thickness must have length dimension"
        }

        require(thickness.baseValue > 0.0) {
            "Wall thickness must be positive"
        }
    }
}
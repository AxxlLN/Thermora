package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class HeatSourceGeometry(
    val length: Quantity,
    val width: Quantity,
    val height: Quantity,
    val volumeFillCoefficient: Double
) {
    init {
        requireLength(length, "length")
        requireLength(width, "width")
        requireLength(height, "height")

        require(length.baseValue > 0.0)
        require(width.baseValue > 0.0)
        require(height.baseValue > 0.0)

        require(
            volumeFillCoefficient.isFinite() &&
                    volumeFillCoefficient > 0.0 &&
                    volumeFillCoefficient <= 1.0
        ) {
            "Volume fill coefficient must be in range (0, 1]"
        }
    }

    private fun requireLength(
        quantity: Quantity,
        fieldName: String
    ) {
        require(quantity.unit.dimension == PhysicalDimension.LENGTH) {
            "$fieldName must have length dimension"
        }
    }
}
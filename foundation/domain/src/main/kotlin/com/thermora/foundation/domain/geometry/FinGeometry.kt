package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class FinGeometry(
    val width: Quantity,
    val height: Quantity,
    val length: Quantity,
    val countPerSide: Int,
    val spacing: Quantity
) {
    init {
        requireLength(width, "width")
        requireLength(height, "height")
        requireLength(length, "length")
        requireLength(spacing, "spacing")

        require(width.baseValue > 0.0)
        require(height.baseValue > 0.0)
        require(length.baseValue > 0.0)
        require(spacing.baseValue > 0.0)

        require(countPerSide > 0) {
            "Fin count must be positive"
        }
    }

    private fun requireLength(
        quantity: Quantity,
        fieldName: String
    ) {
        require(
            quantity.unit.dimension == PhysicalDimension.LENGTH
        ) {
            "$fieldName must have length dimension"
        }
    }
}
package com.thermora.foundation.domain.geometry

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity

data class EnclosureGeometry(
    val length: Quantity,
    val width: Quantity,
    val height: Quantity
) {
    init {
        requireLength(length, "length")
        requireLength(width, "width")
        requireLength(height, "height")

        require(length.baseValue > 0.0) {
            "Enclosure length must be positive"
        }
        require(width.baseValue > 0.0) {
            "Enclosure width must be positive"
        }
        require(height.baseValue > 0.0) {
            "Enclosure height must be positive"
        }
    }

    val volume: Quantity
        get() = length * width * height

    private fun requireLength(
        quantity: Quantity,
        fieldName: String
    ) {
        require(quantity.unit.dimension == PhysicalDimension.LENGTH) {
            "$fieldName must have length dimension"
        }
    }
}
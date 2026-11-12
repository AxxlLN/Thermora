package com.thermora.foundation.units.quantity

import com.thermora.foundation.units.unit.MeasurementUnit

data class Quantity(
    val value: Double,
    val unit: MeasurementUnit
) {
    init {
        require(value.isFinite())
    }

    val baseValue: Double
        get() = unit.toBase(value)

    fun convertTo(target: MeasurementUnit): Quantity {
        require(unit.dimension == target.dimension) {
            "Incompatible physical dimensions: " +
                    "${unit.dimension} and ${target.dimension}"
        }

        return Quantity(
            value = target.fromBase(baseValue),
            unit = target
        )
    }
}
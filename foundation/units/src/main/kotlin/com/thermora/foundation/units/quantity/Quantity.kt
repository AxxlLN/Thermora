
package com.thermora.foundation.units.quantity

import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.unit.MeasurementUnit

data class Quantity(
    val value: Double,
    val unit: MeasurementUnit
) {
    init {
        require(value.isFinite()) {
            "Quantity value must be finite"
        }

        require(unit.offsetToBase == 0.0) {
            "Affine units are not supported by Quantity"
        }

        require(unit.dimension != PhysicalDimension.TEMPERATURE) {
            "Absolute temperature and temperature differences " +
                    "must use specialized types"
        }
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

    operator fun plus(other: Quantity): Quantity {
        val converted = other.convertTo(unit)

        return Quantity(
            value = value + converted.value,
            unit = unit
        )
    }

    operator fun minus(other: Quantity): Quantity {
        val converted = other.convertTo(unit)

        return Quantity(
            value = value - converted.value,
            unit = unit
        )
    }

    operator fun times(other: Quantity): Quantity =
        Quantity(
            value = value * other.value,
            unit = unit * other.unit
        )

    operator fun div(other: Quantity): Quantity =
        Quantity(
            value = value / other.value,
            unit = unit / other.unit
        )

    operator fun times(scalar: Double): Quantity {
        require(scalar.isFinite())

        return Quantity(
            value = value * scalar,
            unit = unit
        )
    }

    operator fun div(scalar: Double): Quantity {
        require(scalar.isFinite() && scalar != 0.0)

        return Quantity(
            value = value / scalar,
            unit = unit
        )
    }

    operator fun unaryMinus(): Quantity =
        Quantity(
            value = -value,
            unit = unit
        )
}

package com.thermora.foundation.units.unit

import com.thermora.foundation.units.dimension.PhysicalDimension

data class MeasurementUnit(
    val symbol: String,
    val dimension: PhysicalDimension,
    val scaleToBase: Double,
    val offsetToBase: Double = 0.0
) {
    init {
        require(symbol.isNotBlank())
        require(scaleToBase.isFinite() && scaleToBase > 0.0)
        require(offsetToBase.isFinite())
    }

    fun toBase(value: Double): Double {
        require(value.isFinite())

        val result = value * scaleToBase + offsetToBase

        require(result.isFinite()) {
            "Conversion to base unit overflow"
        }

        return result
    }

    fun fromBase(value: Double): Double {
        require(value.isFinite())

        val result = (value - offsetToBase) / scaleToBase

        require(result.isFinite()) {
            "Conversion from base unit overflow"
        }

        return result
    }
}
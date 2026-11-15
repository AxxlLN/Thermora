package com.thermora.models.externalcooling.legacy.internal

import java.math.BigDecimal

/**
 * Effective heated-zone surface area.
 *
 * Sz = 2 * (L * W + (L + W) * Kz * H)
 *
 * Dimensions are in meters.
 * Result is in square meters.
 */
internal object LegacyHeatedZoneArea {

    fun calculate(
        lengthMeters: Double,
        widthMeters: Double,
        heightMeters: Double,
        volumeFillCoefficient: Double
    ): BigDecimal {
        require(
            listOf(
                lengthMeters,
                widthMeters,
                heightMeters
            ).all { it.isFinite() && it > 0.0 }
        ) {
            "Heated-zone dimensions must be finite and positive"
        }

        require(
            volumeFillCoefficient.isFinite() &&
                    volumeFillCoefficient in 0.0..1.0
        ) {
            "Volume fill coefficient must be between 0 and 1"
        }

        val length = BigDecimal(lengthMeters)
        val width = BigDecimal(widthMeters)
        val height = BigDecimal(heightMeters)
        val fill = BigDecimal(volumeFillCoefficient)

        return length.multiply(width)
            .add(length.add(width).multiply(fill).multiply(height))
            .multiply(BigDecimal(2))
    }
}

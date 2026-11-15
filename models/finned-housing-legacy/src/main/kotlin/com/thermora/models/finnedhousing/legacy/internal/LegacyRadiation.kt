package com.thermora.models.finnedhousing.legacy.internal

import java.math.BigDecimal

internal object LegacyRadiation {
    private val STEFAN_BOLTZMANN = BigDecimal(5.67 * 1e-8)

    fun coefficient(first: Double, second: Double, emissivity: Double, cell: String,
                    geometricFactor: BigDecimal = BigDecimal.ONE): Double {
        LegacyArithmetic.nonnegative(first, cell)
        LegacyArithmetic.nonnegative(second, cell)
        LegacyArithmetic.nonnegative(emissivity, cell)
        if (geometricFactor.signum() < 0) throw LegacyCalculationException(cell, "Radiation geometry factor must be non-negative")
        val a = BigDecimal(first)
        val b = BigDecimal(second)
        return LegacyArithmetic.nonnegative(a.add(b).multiply(a.multiply(a).add(b.multiply(b)))
            .multiply(STEFAN_BOLTZMANN).multiply(BigDecimal(emissivity))
            .multiply(geometricFactor).toDouble(), cell)
    }
}

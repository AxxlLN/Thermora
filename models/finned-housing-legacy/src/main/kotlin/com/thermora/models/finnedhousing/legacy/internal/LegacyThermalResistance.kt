package com.thermora.models.finnedhousing.legacy.internal

import java.math.BigDecimal

internal object LegacyThermalResistance {
    fun parallelPaths(paths: List<Pair<Double, Double>>, cell: String): Double {
        val conductance = paths.fold(BigDecimal.ZERO) { sum, (coefficient, area) ->
            LegacyArithmetic.nonnegative(coefficient, cell)
            LegacyArithmetic.positive(area, cell)
            sum.add(BigDecimal(coefficient).multiply(BigDecimal(area)))
        }
        if (conductance.signum() <= 0) throw LegacyCalculationException(cell, "Total thermal conductance must be positive")
        return LegacyArithmetic.positive(BigDecimal.ONE.divide(conductance, LegacyArithmetic.context).toDouble(), cell)
    }

    fun dissipatedPower(rise: Double, resistance: Double, cell: String): Double {
        LegacyArithmetic.positive(rise, cell)
        LegacyArithmetic.positive(resistance, cell)
        return LegacyArithmetic.positive(BigDecimal(rise).divide(BigDecimal(resistance), LegacyArithmetic.context).toDouble(), cell)
    }
}

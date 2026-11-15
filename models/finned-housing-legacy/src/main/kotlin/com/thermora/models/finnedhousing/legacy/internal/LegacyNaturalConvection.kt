package com.thermora.models.finnedhousing.legacy.internal

import java.math.BigDecimal

internal object LegacyNaturalConvection {
    fun coefficient(meanTemperature: Double, rise: Double, length: Double, cell: String): Double {
        LegacyArithmetic.positive(meanTemperature, cell)
        val temperature = BigDecimal(meanTemperature)
        val factor = temperature.multiply(temperature).multiply(BigDecimal(0.000007))
            .subtract(temperature.multiply(BigDecimal(0.0061))).add(BigDecimal(2.5529))
        val power = LegacyArithmetic.ratioPower(rise, length, 0.25, cell)
        return LegacyArithmetic.nonnegative(factor.multiply(BigDecimal(power)).toDouble(), cell)
    }
}

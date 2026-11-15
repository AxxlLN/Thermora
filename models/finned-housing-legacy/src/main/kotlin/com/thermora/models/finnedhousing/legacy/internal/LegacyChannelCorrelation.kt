package com.thermora.models.finnedhousing.legacy.internal

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

internal object LegacyChannelCorrelation {
    fun parameter(meanTemperature: Double, rise: Double, spacing: Double, length: Double, cell: String): Double {
        LegacyArithmetic.positive(meanTemperature, cell)
        LegacyArithmetic.positive(rise, cell)
        LegacyArithmetic.positive(spacing, cell)
        LegacyArithmetic.positive(length, cell)
        val result = 357.6 * meanTemperature.pow(-1.2) * spacing *
            LegacyArithmetic.ratioPower(rise, length, 0.25, cell)
        return LegacyArithmetic.positive(if (result.isFinite() && result > 0.0) result else
            exp(ln(357.6) - 1.2 * ln(meanTemperature) + ln(spacing) + 0.25 * (ln(rise) - ln(length))), cell)
    }

    fun correction(parameter: Double, cell: String): Double {
        val x = LegacyArithmetic.positive(parameter, cell) * 100.0
        return LegacyArithmetic.nonnegative(0.095 * x.pow(3) - 0.6324 * x.pow(2) + 1.4525 * x - 0.1597, cell)
    }
}

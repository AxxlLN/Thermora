package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Collections
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

internal class LegacyCalculationException(val cell: String, message: String) : IllegalArgumentException(message)

internal object LegacyArithmetic {
    val context = MathContext(40, RoundingMode.HALF_EVEN)

    fun finite(value: Double, cell: String): Double {
        if (!value.isFinite()) throw LegacyCalculationException(cell, "Calculated value must be finite")
        return value
    }

    fun positive(value: Double, cell: String): Double {
        finite(value, cell)
        if (value <= 0.0) throw LegacyCalculationException(cell, "Calculated value must be positive and representable")
        return value
    }

    fun nonnegative(value: Double, cell: String): Double {
        finite(value, cell)
        if (value < 0.0) throw LegacyCalculationException(cell, "Calculated value must be non-negative")
        return value
    }

    fun temperature(value: Double, cell: String): Temperature =
        Temperature.fromKelvin(nonnegative(value, cell))

    fun area(value: BigDecimal, cell: String): Quantity =
        Quantity(positive(value.toDouble(), cell), StandardUnits.SQUARE_METER)

    fun coefficient(value: Double, cell: String): Quantity =
        Quantity(nonnegative(value, cell), StandardUnits.WATT_PER_SQUARE_METER_KELVIN)

    fun resistance(value: Double, cell: String): ThermalResistance =
        ThermalResistance(Quantity(positive(value, cell), StandardUnits.KELVIN_PER_WATT))

    fun power(value: Double, cell: String): Quantity =
        Quantity(positive(value, cell), StandardUnits.WATT)

    fun mean(first: Double, second: Double): Double {
        val sum = first + second
        return if (sum.isFinite()) sum / 2.0 else first / 2.0 + second / 2.0
    }

    fun ratioPower(numerator: Double, denominator: Double, exponent: Double, cell: String): Double {
        nonnegative(numerator, cell)
        positive(denominator, cell)
        if (numerator == 0.0) return 0.0
        val ratio = numerator / denominator
        val result = if (ratio.isFinite() && ratio > 0.0) ratio.pow(exponent)
            else exp(exponent * (ln(numerator) - ln(denominator)))
        return positive(result, cell)
    }

    fun <T> freeze(values: List<T>): List<T> = Collections.unmodifiableList(ArrayList(values))
}

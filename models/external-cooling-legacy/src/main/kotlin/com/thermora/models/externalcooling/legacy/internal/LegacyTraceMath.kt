package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.unit.StandardUnits
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Collections

internal data class LegacyTraceAreas(val enclosure: Double, val zone: Double, val xy: Double, val xz: Double, val yz: Double)

internal object LegacyTraceMath {
    val context = MathContext(40, RoundingMode.HALF_EVEN)

    fun check(condition: Boolean, cell: String, message: String) {
        if (!condition) throw IllegalArgumentException("$cell: $message")
    }
    fun finite(value: Double, cell: String): Double {
        check(value.isFinite(), cell, "Result is not finite")
        return value
    }
    fun positive(value: Double, cell: String): Double {
        finite(value, cell)
        check(value > 0.0, cell, "Positive result is not representable")
        return value
    }
    fun quantity(value: Double, unit: MeasurementUnit, cell: String, allowZero: Boolean = false): Quantity {
        if (allowZero) check(finite(value, cell) >= 0.0, cell, "Negative value") else positive(value, cell)
        return Quantity(value, unit)
    }
    fun temperature(value: Double, cell: String): Temperature {
        check(finite(value, cell) >= 0.0, cell, "Negative absolute temperature")
        return Temperature.fromKelvin(value)
    }
    fun mean(a: Double, b: Double): Double {
        val sum = a + b
        return if (sum.isFinite()) sum / 2.0 else a / 2.0 + b / 2.0
    }
    fun radiation(a: Double, b: Double, emissivity: Double, cell: String): Double {
        val x = BigDecimal(a)
        val y = BigDecimal(b)
        return positive(x.add(y).multiply(x.multiply(x).add(y.multiply(y)))
            .multiply(BigDecimal(5.67 * 1e-8)).multiply(BigDecimal(emissivity)).toDouble(), cell)
    }
    fun areas(enclosure: EnclosureGeometry, zone: HeatSourceGeometry): LegacyTraceAreas {
        val l = BigDecimal(enclosure.length.baseValue)
        val w = BigDecimal(enclosure.width.baseValue)
        val h = BigDecimal(enclosure.height.baseValue)
        val zl = BigDecimal(zone.length.baseValue)
        val zw = BigDecimal(zone.width.baseValue)
        val zh = BigDecimal(zone.height.baseValue)
        val fill = BigDecimal(zone.volumeFillCoefficient)
        val xy = zl.multiply(zw)
        val xz = zl.multiply(zh).multiply(fill)
        val yz = zw.multiply(zh).multiply(fill)
        return LegacyTraceAreas(positive(l.multiply(w).add(h.multiply(l.add(w))).multiply(BigDecimal(2)).toDouble(), "V11"),
            positive(xy.add(xz).add(yz).multiply(BigDecimal(2)).toDouble(), "V12"),
            positive(xy.toDouble(), "V13"), positive(xz.toDouble(), "V14"), positive(yz.toDouble(), "V15"))
    }
    fun coefficient(value: Double, cell: String) = quantity(value, StandardUnits.WATT_PER_SQUARE_METER_KELVIN, cell, true)
    fun area(value: Double, cell: String) = quantity(value, StandardUnits.SQUARE_METER, cell)
    fun resistance(value: Double, cell: String) = ThermalResistance(quantity(value, StandardUnits.KELVIN_PER_WATT, cell))
    fun parallel(paths: List<Pair<Double, Double>>, cell: String): Double {
        val conductance = paths.fold(BigDecimal.ZERO) { sum, (coefficient, area) ->
            check(finite(coefficient, cell) >= 0.0, cell, "Negative conductance")
            sum.add(BigDecimal(coefficient).multiply(BigDecimal(positive(area, cell))))
        }
        check(conductance.signum() > 0, cell, "Non-positive conductance")
        return positive(BigDecimal.ONE.divide(conductance, context).toDouble(), cell)
    }
    fun ratio(a: Double, b: Double, cell: String): Double =
        positive(BigDecimal(a).divide(BigDecimal(positive(b, cell)), context).toDouble(), cell)
    fun <T> freeze(values: List<T>): List<T> = Collections.unmodifiableList(ArrayList(values))
}

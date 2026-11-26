package com.thermora.models.perforatedhousing.legacy.internal

import com.thermora.foundation.common.diagnostic.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.units.unit.*
import com.thermora.models.perforatedhousing.legacy.input.*
import com.thermora.models.perforatedhousing.legacy.output.*
import kotlin.math.*

internal class PerforatedNumericalFailure(val cell: String, message: String, val sheet: String = "Перфорированный корпус") : RuntimeException(message)

/** Literal one-step Excel equations. 273 is the original Celsius coordinate offset, not a Foundation conversion. */
internal class PerforatedHousingCalculations {
    private val warnings = mutableListOf<Diagnostic>()
    private fun warn(cell: String, code: String, message: String) {
        warnings += Diagnostic("PERFORATED_HOUSING_$code", message, DiagnosticSeverity.WARNING, cell,
            mapOf("sheet" to "Перфорированный корпус", "cell" to cell))
    }
    private fun finite(cell: String, value: Double, sheet: String = "Перфорированный корпус"): Double {
        if (!value.isFinite()) throw PerforatedNumericalFailure(cell, "Non-finite numerical result", sheet)
        return value
    }
    private fun divide(cell: String, numerator: Double, denominator: Double): Double {
        if (denominator == 0.0) throw PerforatedNumericalFailure(cell, "Zero denominator")
        return finite(cell, finite(cell, numerator) / finite(cell, denominator))
    }
    private fun root(cell: String, value: Double): Double {
        if (!value.isFinite() || value < 0) throw PerforatedNumericalFailure(cell, "Invalid square-root argument")
        return sqrt(value)
    }
    private fun q(cell: String, value: Double, unit: MeasurementUnit) = Quantity(finite(cell, value), unit)
    private fun temp(cell: String, kelvin: Double): Temperature {
        if (!kelvin.isFinite() || kelvin < 0) throw PerforatedNumericalFailure(cell, "Invalid absolute temperature")
        return Temperature.fromKelvin(kelvin)
    }
    private fun percent(cell: String, initial: Double, calculated: Double): Double? {
        val result = if (initial == 0.0) null else (100 * (initial - calculated) / initial).takeIf { it.isFinite() }
        if (result == null) warn(cell, "UNDEFINED_AUXILIARY", "Relative convergence is undefined")
        return result
    }
    private fun nextTemperature(cell: String, value: Double): Temperature? {
        if (!value.isFinite() || value < 0) {
            warn(cell, "UNDEFINED_AUXILIARY", "Suggested temperature is not representable")
            return null
        }
        return Temperature.fromKelvin(value)
    }
    fun calculate(i: PerforatedHousingLegacyInput): Pair<PerforatedHousingLegacyOutput, List<Diagnostic>> {
        val ta = i.ambient.temperature.kelvin
        val tk = i.trialEnclosureTemperature.kelvin
        val tz = i.trialZoneTemperature.kelvin
        val trialCaseC = tk - 273.0 // H3
        val tc = (ta - 273.0) + (tk - ta) // E7, retain Excel addition order
        val zc = tz - 273.0 // H4
        val ac = ta - 273.0
        val rise = tk - ta // E5
        val power = i.source.power.baseValue
        val alpha = i.convectionCoefficient.baseValue
        val length = i.enclosure.length.baseValue
        val width = i.enclosure.width.baseValue
        val height = i.enclosure.height.baseValue
        val zl = i.heatedZone.length.baseValue
        val zw = i.heatedZone.width.baseValue
        val zh = i.heatedZone.height.baseValue
        val sc = finite("V11", 2 * (width * length + height * (width + length)), "Исходные данные")
        val sz = finite("V12", 2 * (zl * zw + zh * i.heatedZone.volumeFillCoefficient * (zl + zw)), "Исходные данные")
        val volume = finite("C17", length * width * height - zl * zw * zh)
        if (volume <= 0) throw PerforatedNumericalFailure("C17", "Free air volume must be positive")
        // J9/J7: retain quotient and its singularity, do not replace with a limit formula.
        val radiation = divide("J9", 5.67e-8 * ((zc + 273).pow(4) - (tc + 273).pow(4)), zc - tc)
        val radiationPower = finite("J7", 0.8 * radiation * sz * (zc - tc))
        // C10 intentionally uses POWER squared, unlike H16. This is not a typo in the port.
        val d = finite("C10", 2 * (tc - divide("C10", radiationPower - (0.0025 * power.pow(2) + 0.3997 * rise - 0.3275), alpha * sc)))
        val a = finite("C11", sc + sz)
        val delta = finite("C12", 2 * (sz * zc + sc * tc))
        val b = finite("C13", 0.5 * d * (sz * (zc - ac) + sc * (tc - ac)) + ac * (sz * zc + sc * tc))
        val discriminant = finite("I12", delta.pow(2) - 4 * a * b)
        if (discriminant < 0) throw PerforatedNumericalFailure("I12", "Negative discriminant")
        // G13 and J13 use the same binary64 I12 value displayed by the sheet.
        val rootDiscriminant = sqrt(discriminant)
        val lower = divide("J13", delta - rootDiscriminant, 2 * a)
        val upper = divide("G13", delta + rootDiscriminant, 2 * a)
        val second = finite("H15", d - lower)
        val zoneAir = temp("E14", (2 * (lower - ta + 273) + ac) + 273)
        val outlet = temp("E15", (2 * (second - lower) + ac) + 273)
        val t1 = temp("J14", lower + 273)
        val t2 = temp("J15", second + 273)
        val density = finite("C16", 374 * ta.pow(-1.009))
        val open = i.perforation.openArea.baseValue
        val transition = i.perforation.transitionArea.baseValue
        val openingTerm = divide("C16", ta + outlet.kelvin, open.pow(2))
        val transitionTerm = divide("C16", zoneAir.kelvin, 4 * transition.pow(2))
        val buoyancy = 1 / ta - 0.5 * (divide("C16", 1.0, t1.kelvin) + divide("C16", 1.0, t2.kelvin))
        val flow = finite("C16", 0.65 * density * ta * root("C16", 0.5 * 9.81 * height) *
            root("C16", divide("C16", buoyancy, openingTerm + transitionTerm)))
        val mass = finite("C17", density * volume)
        val exchange = divide("G17", flow, mass)
        val nextAlpha = finite("I17", 23.1 * root("I17", exchange))
        val caseC = finite("E16", lower + divide("E16", divide("E16", 4 * 1000 * flow * (lower - ta + 273), alpha) + sz * (lower - zc), sc))
        val airPower = finite("E17", 2 * flow * 1000 * (second - lower))
        val casePower = finite("H16", 0.0025 * rise.pow(2) + 0.4045 * rise - 0.3271)
        val total = finite("H16", casePower + airPower)
        val pCase = percent("P4", trialCaseC, caseC)
        val pPower = percent("P5", power, total)
        val pAir = percent("P6", power - casePower, airPower)
        val percentages = listOf(pCase, pPower, pAir)
        fun within(threshold: Double): Boolean? =
            if (percentages.any { it == null }) null else percentages.filterNotNull().all { abs(it) < threshold }
        val withinThreePercent = within(3.0)
        val withinOnePercent = within(1.0)
        val caseTemperature = temp("T15", caseC + 273)
        // T16 deliberately uses manual F4, not suggested N15.
        val element = temp("T18", ta + (tz - ta) * (0.75 + 0.25 * divide("T18", i.criticalElement.power.baseValue * sz, i.elementWithRadiatorArea.baseValue * power)))
        val junction = temp("T19", element.kelvin + (i.criticalElement.transitionToCaseResistance.value.baseValue + i.criticalElement.caseToRadiatorResistance.value.baseValue) * i.criticalElement.power.baseValue)
        val nextCase = nextTemperature("N16", tk - 0.1 * (caseTemperature.kelvin - tk))
        val first = i.firstIterationPower.baseValue
        val nextZone = if (total == 0.0 || first - power + 0.2 == 0.0) {
            warn("N15", "UNDEFINED_AUXILIARY", "Zero denominator in suggested zone update"); null
        } else nextTemperature("N15", tz - ((total - power) / total) * (first / (first - power + 0.2)))
        warn("C10,H16", "LEGACY_CORRELATIONS", "Unverified empirical correlations and legacy Celsius coordinates are preserved literally")
        when (withinThreePercent) {
            null -> warn("P4:P6", "CONVERGENCE_UNDEFINED", "One or more relative convergence percentages are undefined")
            false -> warn("P4:P6", "NOT_CONVERGED", "Manual trial does not meet the 3 percent convergence criterion")
            true -> Unit
        }
        val massFlowUnit = MeasurementUnit("kg/s", PhysicalDimension.MASS / PhysicalDimension.TIME, 1.0)
        val rateUnit = MeasurementUnit("1/s", PhysicalDimension.DIMENSIONLESS / PhysicalDimension.TIME, 1.0)
        return PerforatedHousingLegacyOutput(
            PerforatedHousingGeometryResults(q("V11", sc, StandardUnits.SQUARE_METER), q("V12", sz, StandardUnits.SQUARE_METER), q("C17", volume, StandardUnits.CUBIC_METER)),
            PerforatedHousingQuadraticResults(d, a, delta, b, discriminant, temp("J13", lower + 273), temp("G13", upper + 273), t2),
            PerforatedHousingHeatTransferResults(q("J9", radiation, StandardUnits.WATT_PER_SQUARE_METER_KELVIN), q("J7", radiationPower, StandardUnits.WATT)),
            PerforatedHousingAirBalanceResults(zoneAir, outlet, q("C16", flow, massFlowUnit), q("C17", mass, StandardUnits.KILOGRAM), q("G17", exchange, rateUnit),
                q("E17", airPower, StandardUnits.WATT), q("H16", casePower, StandardUnits.WATT), q("H16", total, StandardUnits.WATT), caseTemperature, q("I17", nextAlpha, StandardUnits.WATT_PER_SQUARE_METER_KELVIN)),
            PerforatedHousingConvergenceResults(TemperatureDelta.fromKelvin(finite("O4", trialCaseC - caseC)), q("O5", power - total, StandardUnits.WATT), q("O6", power - casePower - airPower, StandardUnits.WATT), pCase, pPower, pAir, withinThreePercent, withinOnePercent),
            PerforatedHousingNextStep(nextCase, nextZone, q("N13", nextAlpha, StandardUnits.WATT_PER_SQUARE_METER_KELVIN)),
            PerforatedHousingFinalResults(caseTemperature, i.trialZoneTemperature, temp("T17", (t1.kelvin + t2.kelvin) / 2), element, junction)
        ) to warnings.toList()
    }
}

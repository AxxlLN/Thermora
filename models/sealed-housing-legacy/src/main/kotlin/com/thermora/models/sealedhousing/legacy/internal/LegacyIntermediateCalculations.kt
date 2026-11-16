package com.thermora.models.sealedhousing.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.domain.geometry.WallOrientation
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.*
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

internal object LegacyIntermediateCalculations {
    fun validate(input: SealedHousingLegacyInput): ValidationReport {
        val config = input.heatTransfer ?: return ValidationReport.valid()
        val walls = config.wallGaps
        val valid = config.enclosureEmissivity.isFinite() && config.enclosureEmissivity > 0.0 && config.enclosureEmissivity <= 1.0 &&
            config.enclosureReferenceRise.kelvin > 0.0 && config.enclosureProbeRises.size == 3 &&
            config.enclosureProbeRises.all { it.kelvin > 0.0 } && config.enclosureProbeResistances.size == 3 &&
            walls.size == 6 && walls.map { it.wallIndex }.toSet() == (1..6).toSet() &&
            walls.all { it.gap.unit.dimension == com.thermora.foundation.units.dimension.PhysicalDimension.LENGTH && it.gap.baseValue > 0.0 } &&
            config.zoneProbeRises.size == 3 && config.zoneProbeRises.all { it.kelvin > 0.0 } &&
            config.zoneProbeResistances.size == 3 && config.referenceZoneTemperature.kelvin > input.ambient.temperature.kelvin
        return if (valid) ValidationReport.valid() else ValidationReport.of(Diagnostic(
            "SEALED_LEGACY_INVALID_HEAT_TRANSFER_INPUT", "Invalid explicit heat transfer parameters",
            DiagnosticSeverity.ERROR, "heatTransfer"))
    }

    fun warnings(input: SealedHousingLegacyInput): List<Diagnostic> = if (input.heatTransfer == null) emptyList() else listOf(
        Diagnostic("SEALED_LEGACY_INTERMEDIATE_SOURCE_LIMITATIONS",
            "Excel empty Q15/Q16 references are treated as zero; S13 mixes height and element power, and E27/E28 use C26 in the final ratio; manual trial resistances are preserved",
            DiagnosticSeverity.WARNING, "sealed-housing-legacy", mapOf("cells" to "S12:S13,E27:E28,V11:V13,W24:W26")))

    fun attach(input: SealedHousingLegacyInput, output: SealedHousingLegacyOutput): SealedHousingLegacyOutput {
        val config = input.heatTransfer ?: return output
        val a = LegacyTraceMath.areas(config.enclosure, input.heatedZone)
        val l = config.enclosure.length.baseValue
        val w = config.enclosure.width.baseValue
        val h = config.enclosure.height.baseValue
        val ambient = input.ambient.temperature.kelvin
        val rise = config.enclosureReferenceRise.kelvin
        val mean = LegacyTraceMath.positive(LegacyTraceMath.mean(ambient, ambient + rise), "Q8")
        val a2 = LegacyTraceMath.positive(0.000007 * mean.pow(2) - 0.0061 * mean + 2.5529, "I9")
        val a3 = LegacyTraceMath.positive(0.00001 * mean.pow(2) - 0.0101 * mean + 3.7079, "Q9")
        val laminar = listOf(0.7 * a2 * (rise / w).pow(0.25), 1.3 * a2 * (rise / w).pow(0.25), a2 * (rise / h).pow(0.25))
            .mapIndexed { index, value -> LegacyTraceMath.coefficient(value, "B${index + 11}") }
        val turbulent = listOf(0.7 * a3 * rise.pow(0.333), 1.3 * a3 * rise.pow(0.333), a3 * rise.pow(0.333))
            .mapIndexed { index, value -> LegacyTraceMath.coefficient(value, "J${index + 11}") }
        val lowerThreshold = LegacyTraceMath.positive((0.84 / min(l, w)).pow(3), "S11")
        val upperThreshold = LegacyTraceMath.positive((0.84 / min(w, h)).pow(3), "S12")
        val sideThreshold = LegacyTraceMath.positive((0.84 / min(h, input.criticalElement.power.baseValue)).pow(3), "S13")
        val regimes = listOf(if (rise <= lowerThreshold) SealedHousingConvectionRegime.LAMINAR else SealedHousingConvectionRegime.TURBULENT,
            if (0.0 <= upperThreshold) SealedHousingConvectionRegime.LAMINAR else SealedHousingConvectionRegime.TURBULENT,
            if (0.0 <= sideThreshold) SealedHousingConvectionRegime.LAMINAR else SealedHousingConvectionRegime.TURBULENT)
        val selected = regimes.mapIndexed { index, regime ->
            if (regime == SealedHousingConvectionRegime.LAMINAR) laminar[index] else turbulent[index] }
        val radiation = LegacyTraceMath.radiation(ambient + rise, ambient, config.enclosureEmissivity, "W5")
        val horizontal = LegacyTraceMath.positive(l * w, "V14")
        val sideArea = LegacyTraceMath.positive(2 * h * (l + w), "V14")
        val externalResistance = LegacyTraceMath.parallel(listOf(radiation to horizontal, selected[0].baseValue to horizontal,
            radiation to horizontal, selected[1].baseValue to horizontal, radiation to sideArea, selected[2].baseValue to sideArea), "V14")
        val externalRises = config.enclosureProbeRises.toList()
        val externalResistances = config.enclosureProbeResistances.toList()
        val externalTrials = externalRises.mapIndexed { index, delta -> SealedHousingExternalProbe(delta, externalResistances[index],
            LegacyTraceMath.quantity(LegacyTraceMath.ratio(delta.kelvin, externalResistances[index].value.baseValue, "X${index + 12}"),
                StandardUnits.WATT, "X${index + 12}")) }
        val external = SealedHousingExternalHeatTransferResults(config.enclosureReferenceRise,
            LegacyTraceMath.temperature(mean, "Q8"), LegacyTraceMath.coefficient(radiation, "W5"), a2, a3,
            LegacyTraceMath.freeze(laminar), LegacyTraceMath.freeze(turbulent), LegacyTraceMath.freeze(selected),
            LegacyTraceMath.freeze(regimes), TemperatureDelta.fromKelvin(lowerThreshold), TemperatureDelta.fromKelvin(upperThreshold),
            sideThreshold, LegacyTraceMath.resistance(externalResistance, "V14"), LegacyTraceMath.freeze(externalTrials))
        val caseTemperature = output.enclosureTemperature
        val reference = config.referenceZoneTemperature
        val drop = LegacyTraceMath.positive(reference.kelvin - caseTemperature.kelvin, "H25-G25")
        val gapMean = LegacyTraceMath.mean(reference.kelvin, caseTemperature.kelvin)
        val walls = config.wallGaps.sortedBy { it.wallIndex }
        val coefficients = walls.map { wall ->
            val row = wall.wallIndex + 24
            val rectangle = when (wall.wallIndex) { 1, 3 -> h * l; else -> w * l }
            val multiplier = when (wall.wallIndex) {
                1 -> 1.0
                5 -> 0.7
                6 -> 1.3
                else -> if (wall.orientation == WallOrientation.VERTICAL) 1.0 else 1.3
            }
            val ownGap = wall.gap.baseValue
            val lastGap = if (wall.wallIndex == 3 || wall.wallIndex == 4) walls[1].gap.baseValue else ownGap
            LegacyTraceMath.coefficient(multiplier * (6.25 - 5.25 * (1 + ownGap / sqrt(rectangle)).pow(-1.67)) *
                (23.278 * gapMean.pow(-0.639)) * (drop / lastGap).pow(0.25), "E$row")
        }
        val emissivity = LegacyTraceMath.positive(1.0 / (1.0 / 0.8 + (a.zone / a.enclosure) * (1.0 / config.enclosureEmissivity - 1)), "E31")
        val internalRadiation = LegacyTraceMath.radiation(reference.kelvin, caseTemperature.kelvin, emissivity, "I31")
        val paths = mutableListOf<Pair<Double, Double>>()
        listOf(Triple(0, 2, a.yz), Triple(1, 3, a.xz), Triple(4, 5, a.xy)).forEach { (first, second, area) ->
            paths.add(internalRadiation to area); paths.add(internalRadiation to area)
            paths.add(coefficients[first].baseValue to area); paths.add(coefficients[second].baseValue to area)
        }
        val zoneResistance = LegacyTraceMath.parallel(paths, "O31")
        val zoneRises = config.zoneProbeRises.toList()
        val zoneResistances = config.zoneProbeResistances.toList()
        val zoneTrials = zoneRises.mapIndexed { index, delta -> SealedHousingInternalProbe(
            LegacyTraceMath.temperature(caseTemperature.kelvin + delta.kelvin, "N${index + 28}"), delta, zoneResistances[index],
            LegacyTraceMath.quantity(LegacyTraceMath.ratio(delta.kelvin, zoneResistances[index].value.baseValue, "U${index + 28}"),
                StandardUnits.WATT, "U${index + 28}")) }
        val sinkResistance = LegacyTraceMath.resistance(input.criticalElement.transitionToCaseResistance.value.baseValue +
            input.criticalElement.caseToRadiatorResistance.value.baseValue, "O32")
        return output.copy(geometryResults = SealedHousingGeometryResults(LegacyTraceMath.area(a.enclosure, "V11"),
            LegacyTraceMath.area(a.zone, "V12"), LegacyTraceMath.area(a.xy, "V13"),
            LegacyTraceMath.area(a.xz, "V14"), LegacyTraceMath.area(a.yz, "V15")),
            externalHeatTransfer = external,
            internalHeatTransfer = SealedHousingInternalHeatTransferResults(caseTemperature, reference,
                LegacyTraceMath.temperature(gapMean, "F25"), LegacyTraceMath.freeze(coefficients), emissivity,
                LegacyTraceMath.coefficient(internalRadiation, "I31"), LegacyTraceMath.resistance(zoneResistance, "O31"),
                LegacyTraceMath.freeze(zoneTrials), sinkResistance))
    }
}

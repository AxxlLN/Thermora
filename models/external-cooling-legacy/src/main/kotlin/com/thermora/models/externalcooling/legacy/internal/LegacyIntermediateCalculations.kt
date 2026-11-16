package com.thermora.models.externalcooling.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.output.*
import kotlin.math.pow
import kotlin.math.sqrt

internal object LegacyIntermediateCalculations {
    fun validate(input: ExternalCoolingLegacyInput): ValidationReport {
        val config = input.heatTransfer ?: return ValidationReport.valid()
        val walls = config.wallGaps
        val valid = config.enclosureEmissivity.isFinite() && config.enclosureEmissivity > 0.0 && config.enclosureEmissivity <= 1.0 &&
            config.enclosureProbeRises.size == 3 && config.enclosureProbeRises.all { it.kelvin > 0.0 } &&
            walls.size == 6 && walls.map { it.wallIndex }.toSet() == (1..6).toSet() &&
            walls.all { it.gap.unit.dimension == com.thermora.foundation.units.dimension.PhysicalDimension.LENGTH && it.gap.baseValue > 0.0 } &&
            config.zoneProbeRises.size == 3 && config.zoneProbeRises.all { it.kelvin > 0.0 } &&
            config.zoneProbeResistances.size == 3 &&
            config.referenceZoneTemperature.kelvin > input.ambient.temperature.kelvin
        return if (valid) ValidationReport.valid() else ValidationReport.of(Diagnostic(
            "EXTERNAL_COOLING_INVALID_HEAT_TRANSFER_INPUT", "Invalid explicit heat transfer parameters",
            DiagnosticSeverity.ERROR, "heatTransfer"))
    }

    fun warnings(input: ExternalCoolingLegacyInput): List<Diagnostic> = if (input.heatTransfer == null) emptyList() else listOf(
        Diagnostic("EXTERNAL_COOLING_LEGACY_INTERMEDIATE_LIMITATIONS",
            "Manual trial resistances and the V13 face area in reduced emissivity are preserved; intermediate transfer does not recalibrate final power correlations",
            DiagnosticSeverity.WARNING, "external-cooling-legacy", mapOf("cells" to "R16,E18,W11:W13,AA11,AA23")))

    fun attach(input: ExternalCoolingLegacyInput, output: ExternalCoolingLegacyOutput): ExternalCoolingLegacyOutput {
        val config = input.heatTransfer ?: return output
        val a = LegacyTraceMath.areas(config.enclosure, input.heatedZone)
        val l = config.enclosure.length.baseValue
        val w = config.enclosure.width.baseValue
        val h = config.enclosure.height.baseValue
        val flowLength = LegacyTraceMath.positive(l + w, "V16")
        val geometry = ExternalCoolingGeometryResults(LegacyTraceMath.area(a.enclosure, "V11"),
            LegacyTraceMath.area(a.zone, "V12"), LegacyTraceMath.area(a.xy, "V13"),
            LegacyTraceMath.area(a.xz, "V14"), LegacyTraceMath.area(a.yz, "V15"),
            LegacyTraceMath.quantity(flowLength, StandardUnits.METER, "V16"))
        val ambient = input.ambient.temperature.kelvin
        val external = config.enclosureProbeRises.toList().mapIndexed { index, rise ->
            val row = index + 6
            val temperature = LegacyTraceMath.temperature(ambient + rise.kelvin, "C$row")
            val mean = LegacyTraceMath.mean(ambient, temperature.kelvin)
            val radiation = LegacyTraceMath.radiation(temperature.kelvin, ambient, config.enclosureEmissivity, "F$row")
            val viscosity = LegacyTraceMath.positive(0.0003 * mean.pow(1.9097) * 1e-6, "H$row")
            val conductivity = LegacyTraceMath.positive(0.2035 * mean.pow(0.853) * 1e-3, "J$row")
            val convection = LegacyTraceMath.finite(0.8 * sqrt(config.airflow.velocity.baseValue * flowLength / viscosity) * conductivity / flowLength, "L$row")
            val resistance = LegacyTraceMath.parallel(listOf(radiation to a.enclosure, convection to a.enclosure), "N$row")
            ExternalCoolingExternalProbe(rise, temperature, LegacyTraceMath.temperature(mean, "H$row,J$row"),
                LegacyTraceMath.coefficient(radiation, "F$row"),
                LegacyTraceMath.quantity(viscosity, StandardUnits.SQUARE_METER_PER_SECOND, "H$row"),
                LegacyTraceMath.quantity(conductivity, StandardUnits.WATT_PER_METER_KELVIN, "J$row"),
                LegacyTraceMath.coefficient(convection, "L$row"), LegacyTraceMath.resistance(resistance, "N$row"),
                LegacyTraceMath.quantity(LegacyTraceMath.ratio(rise.kelvin, resistance, "P$row"), StandardUnits.WATT, "P$row"))
        }
        val caseTemperature = output.enclosureTemperature
        val reference = config.referenceZoneTemperature
        val drop = LegacyTraceMath.positive(reference.kelvin - caseTemperature.kelvin, "H12-G12")
        val mean = LegacyTraceMath.mean(reference.kelvin, caseTemperature.kelvin)
        val walls = config.wallGaps.sortedBy { it.wallIndex }.map { wall ->
            val row = wall.wallIndex + 11
            val rectangle = when (wall.wallIndex) { 1, 3 -> h * l; 2, 4 -> h * w; else -> w * l }
            val multiplier = when (wall.wallIndex) { 5 -> 0.7; 6 -> 1.3; else -> 1.0 }
            val gap = wall.gap.baseValue
            LegacyTraceMath.coefficient(multiplier * (6.25 - 5.25 * (1 + gap / sqrt(rectangle)).pow(-1.67)) *
                (23.278 * mean.pow(-0.639)) * (drop / gap).pow(0.25), "E$row")
        }
        val emissivity = LegacyTraceMath.positive(1.0 / (1.0 / 0.8 + (a.xy / a.enclosure) * (1.0 / config.enclosureEmissivity - 1)), "E18")
        val radiation = LegacyTraceMath.radiation(reference.kelvin, caseTemperature.kelvin, emissivity, "I18")
        val paths = mutableListOf<Pair<Double, Double>>()
        listOf(Triple(0, 2, a.yz), Triple(1, 3, a.xz), Triple(4, 5, a.xy)).forEach { (first, second, area) ->
            paths.add(radiation to area); paths.add(radiation to area)
            paths.add(walls[first].baseValue to area); paths.add(walls[second].baseValue to area)
        }
        val internalResistance = LegacyTraceMath.parallel(paths, "O18")
        val rises = config.zoneProbeRises.toList()
        val resistances = config.zoneProbeResistances.toList()
        val trials = rises.mapIndexed { index, rise -> ExternalCoolingInternalProbe(
            LegacyTraceMath.temperature(caseTemperature.kelvin + rise.kelvin, "N${index + 15}"), rise, resistances[index],
            LegacyTraceMath.quantity(LegacyTraceMath.ratio(rise.kelvin, resistances[index].value.baseValue, "U${index + 15}"),
                StandardUnits.WATT, "U${index + 15}")) }
        return output.copy(geometryResults = geometry,
            externalHeatTransfer = ExternalCoolingExternalHeatTransferResults(LegacyTraceMath.freeze(external)),
            internalHeatTransfer = ExternalCoolingInternalHeatTransferResults(caseTemperature, reference,
                LegacyTraceMath.temperature(mean, "F12"), LegacyTraceMath.freeze(walls), emissivity,
                LegacyTraceMath.coefficient(radiation, "I18"), LegacyTraceMath.resistance(internalResistance, "O18"),
                LegacyTraceMath.freeze(trials)))
    }
}

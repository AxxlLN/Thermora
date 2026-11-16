package com.thermora.models.internalmixing.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import com.thermora.models.internalmixing.legacy.output.*
import java.math.BigDecimal
import kotlin.math.pow

internal object LegacyIntermediateCalculations {
    private const val ADDED_WETTED_AREA = 0.04
    private const val SPECIFIC_HEAT = 1000.0

    fun validate(input: InternalMixingLegacyInput): ValidationReport {
        val config = input.heatTransfer ?: return ValidationReport.valid()
        val valid = config.reducedEmissivity.isFinite() && config.reducedEmissivity > 0.0 && config.reducedEmissivity <= 1.0 &&
            config.zoneProbeRises.size == 3 && config.zoneProbeRises.all { it.kelvin > 0.0 }
        return if (valid) ValidationReport.valid() else ValidationReport.of(Diagnostic(
            "INTERNAL_MIXING_INVALID_HEAT_TRANSFER_INPUT", "Invalid explicit heat transfer parameters",
            DiagnosticSeverity.ERROR, "heatTransfer"))
    }

    fun warnings(input: InternalMixingLegacyInput): List<Diagnostic> = if (input.heatTransfer == null) emptyList() else listOf(
        Diagnostic("INTERNAL_MIXING_LEGACY_CONVECTION_ONLY_RESISTANCE",
            "Total coefficients include radiation, but trial resistance and dissipated power use convection alone as in Excel",
            DiagnosticSeverity.WARNING, "internal-mixing-legacy", mapOf("cells" to "Q11:Q13,Q22:Q24,Q33:Q35")))

    fun attach(input: InternalMixingLegacyInput, output: InternalMixingLegacyOutput): InternalMixingLegacyOutput {
        val config = input.heatTransfer ?: return output
        val a = LegacyTraceMath.areas(config.enclosure, input.heatedZone)
        val e = config.enclosure
        val z = input.heatedZone
        val volume = BigDecimal(e.length.baseValue).multiply(BigDecimal(e.width.baseValue)).multiply(BigDecimal(e.height.baseValue))
            .subtract(BigDecimal(z.length.baseValue).multiply(BigDecimal(z.width.baseValue)).multiply(BigDecimal(z.height.baseValue)))
        val freeVolume = LegacyTraceMath.positive(volume.toDouble(), "V4")
        val wettedArea = LegacyTraceMath.positive(a.enclosure + a.zone + ADDED_WETTED_AREA, "Q4")
        val diameter = LegacyTraceMath.positive(4.0 * freeVolume / wettedArea, "Q5")
        val fanFlow = config.fan.nominalVolumeFlowRate.baseValue
        val enclosureVolume = BigDecimal(e.length.baseValue).multiply(BigDecimal(e.width.baseValue)).multiply(BigDecimal(e.height.baseValue))
        val numerator = BigDecimal(2).multiply(BigDecimal(fanFlow)).multiply(enclosureVolume)
        val fanSize = BigDecimal(config.fan.characteristicDimension.baseValue)
        val denominator = BigDecimal(e.width.baseValue).multiply(BigDecimal(e.height.baseValue))
            .add(fanSize.multiply(fanSize)).multiply(volume)
        val velocity = LegacyTraceMath.positive(numerator.divide(denominator, LegacyTraceMath.context).toDouble(), "V5")
        val geometry = InternalMixingGeometryResults(LegacyTraceMath.area(a.enclosure, "V11"),
            LegacyTraceMath.area(a.zone, "V12"), LegacyTraceMath.area(a.xy, "V13"),
            LegacyTraceMath.area(a.xz, "V14"), LegacyTraceMath.area(a.yz, "V15"),
            LegacyTraceMath.quantity(freeVolume, StandardUnits.CUBIC_METER, "V4"),
            LegacyTraceMath.area(wettedArea, "Q4"), LegacyTraceMath.quantity(diameter, StandardUnits.METER, "Q5"),
            LegacyTraceMath.quantity(fanFlow, StandardUnits.CUBIC_METER_PER_SECOND, "V6"),
            LegacyTraceMath.quantity(velocity, StandardUnits.METER_PER_SECOND, "V5"))
        val caseTemperature = input.sealedHousing.enclosureTemperature.kelvin
        val probes = config.zoneProbeRises.toList().mapIndexed { index, rise ->
            val offset = index * 11
            val zoneTemperature = LegacyTraceMath.temperature(caseTemperature + rise.kelvin, "N${index + 28}")
            val mean = LegacyTraceMath.positive(LegacyTraceMath.mean(caseTemperature, zoneTemperature.kelvin), "S${index + 5}")
            val density = LegacyTraceMath.positive(374.69 * mean.pow(-1.009), "Q${offset + 7}")
            val viscosity = LegacyTraceMath.positive(0.0003 * mean.pow(1.9097) * 1e-6, "Q${offset + 8}")
            val conductivity = LegacyTraceMath.positive(0.2035 * mean.pow(0.853) * 1e-3, "Q${offset + 9}")
            val prandtl = LegacyTraceMath.positive(SPECIFIC_HEAT * density * viscosity / conductivity, "Q${offset + 10}")
            val convection = LegacyTraceMath.positive((24 * (1 + diameter / z.width.baseValue).pow(2.9) - 23) *
                0.019 * conductivity * prandtl.pow(0.43) / viscosity.pow(0.8) * velocity.pow(0.8) /
                z.length.baseValue.pow(0.2), "Q${offset + 3}")
            val radiation = LegacyTraceMath.radiation(zoneTemperature.kelvin, caseTemperature, config.reducedEmissivity, "T${index + 5}")
            val total = LegacyTraceMath.positive(convection + radiation, "Q${offset + 11}")
            val resistance = LegacyTraceMath.parallel(listOf(convection to a.zone), "Q${offset + 12}")
            InternalMixingHeatTransferProbe(zoneTemperature, rise, LegacyTraceMath.temperature(mean, "S${index + 5}"),
                LegacyTraceMath.quantity(SPECIFIC_HEAT, StandardUnits.JOULE_PER_KILOGRAM_KELVIN, "Q${offset + 6}"),
                LegacyTraceMath.quantity(density, StandardUnits.KILOGRAM_PER_CUBIC_METER, "Q${offset + 7}"),
                LegacyTraceMath.quantity(viscosity, StandardUnits.SQUARE_METER_PER_SECOND, "Q${offset + 8}"),
                LegacyTraceMath.quantity(conductivity, StandardUnits.WATT_PER_METER_KELVIN, "Q${offset + 9}"), prandtl,
                LegacyTraceMath.coefficient(convection, "Q${offset + 3}"), LegacyTraceMath.coefficient(radiation, "T${index + 5}"),
                LegacyTraceMath.coefficient(total, "Q${offset + 11}"), LegacyTraceMath.resistance(resistance, "Q${offset + 12}"),
                LegacyTraceMath.quantity(LegacyTraceMath.ratio(rise.kelvin, resistance, "Q${offset + 13}"),
                    StandardUnits.WATT, "Q${offset + 13}"))
        }
        return output.copy(geometryResults = geometry, heatTransferResults = InternalMixingHeatTransferResults(LegacyTraceMath.freeze(probes)))
    }
}

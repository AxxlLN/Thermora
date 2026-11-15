package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.output.ExternalHeatTransferProbe
import com.thermora.models.finnedhousing.legacy.output.ExternalHeatTransferResults
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingGeometryResults
import java.math.BigDecimal

internal object LegacyExternalHeatTransfer {
    private const val SURFACE_EMISSIVITY = 0.9

    fun calculate(input: FinnedHousingLegacyInput, geometry: FinnedHousingGeometryResults): ExternalHeatTransferResults {
        val ambient = input.ambient.temperature.kelvin
        val spacing = input.fins.spacing.baseValue
        val length = input.fins.length.baseValue
        val geometricFactor = BigDecimal(spacing).divide(BigDecimal(input.fins.height.baseValue)
            .multiply(BigDecimal(2)).add(BigDecimal(spacing)), LegacyArithmetic.context)
        val probes = input.externalProbes.enclosureRises.mapIndexed { index, rise ->
            val row = index + 5
            val wallTemperature = LegacyArithmetic.temperature(ambient + rise.kelvin, "B$row").kelvin
            val mean = LegacyArithmetic.positive(LegacyArithmetic.mean(ambient, wallTemperature), "D$row")
            val smoothRadiation = LegacyRadiation.coefficient(wallTemperature, ambient, SURFACE_EMISSIVITY, "F$row")
            val smoothConvection = LegacyNaturalConvection.coefficient(mean, rise.kelvin,
                input.enclosure.width.baseValue, "H$row")
            val parameter = LegacyChannelCorrelation.parameter(mean, rise.kelvin, spacing, length, "J$row")
            val correction = LegacyChannelCorrelation.correction(parameter, "L$row")
            val channelTemperature = LegacyArithmetic.temperature(BigDecimal(ambient).add(BigDecimal(rise.kelvin))
                .subtract(BigDecimal(correction).multiply(BigDecimal(rise.kelvin))).toDouble(), "N$row")
            val finnedMean = LegacyArithmetic.positive(LegacyArithmetic.mean(wallTemperature, channelTemperature.kelvin), "O$row")
            val finnedRadiation = LegacyRadiation.coefficient(wallTemperature, channelTemperature.kelvin,
                SURFACE_EMISSIVITY, "P$row", geometricFactor)
            val channelDrop = BigDecimal(correction).multiply(BigDecimal(rise.kelvin)).toDouble()
            val finnedConvection = LegacyNaturalConvection.coefficient(finnedMean, channelDrop, length, "Q$row")
            val finnedArea = geometry.finnedSurfaceArea.baseValue
            val smoothArea = geometry.smoothSurfaceArea.baseValue
            val resistance = LegacyThermalResistance.parallelPaths(listOf(finnedRadiation to finnedArea,
                finnedConvection to finnedArea, smoothRadiation to smoothArea, smoothConvection to smoothArea), "R$row")
            ExternalHeatTransferProbe(
                rise,
                LegacyArithmetic.temperature(mean, "D$row"),
                LegacyArithmetic.coefficient(smoothRadiation, "F$row"),
                LegacyArithmetic.coefficient(smoothConvection, "H$row"),
                parameter,
                correction,
                channelTemperature,
                LegacyArithmetic.temperature(finnedMean, "O$row"),
                LegacyArithmetic.coefficient(finnedRadiation, "P$row"),
                LegacyArithmetic.coefficient(finnedConvection, "Q$row"),
                LegacyArithmetic.resistance(resistance, "R$row"),
                LegacyArithmetic.power(LegacyThermalResistance.dissipatedPower(rise.kelvin, resistance, "U$row"), "U$row")
            )
        }
        return ExternalHeatTransferResults(LegacyArithmetic.freeze(probes))
    }
}

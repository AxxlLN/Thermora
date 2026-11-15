package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingGeometryResults
import com.thermora.models.finnedhousing.legacy.output.InternalHeatTransferProbe
import com.thermora.models.finnedhousing.legacy.output.InternalHeatTransferResults
import java.math.BigDecimal
import kotlin.math.pow

internal object LegacyInternalHeatTransfer {
    private val ZONE_EMISSIVITY = BigDecimal("0.8")

    fun calculate(input: FinnedHousingLegacyInput, geometry: FinnedHousingGeometryResults,
                  enclosureTemperature: Temperature): InternalHeatTransferResults {
        val reference = input.internalProbes.referenceZoneTemperature
        val drop = LegacyArithmetic.positive(reference.kelvin - enclosureTemperature.kelvin, "H16-G16")
        val mean = LegacyArithmetic.positive(LegacyArithmetic.mean(reference.kelvin, enclosureTemperature.kelvin), "F16")
        val height = BigDecimal(input.enclosure.height.baseValue)
        val width = BigDecimal(input.enclosure.width.baseValue)
        val length = BigDecimal(input.enclosure.length.baseValue)
        val walls = input.internalProbes.wallGaps.sortedBy { it.wallIndex }.map { wall ->
            val row = wall.wallIndex + 15
            val rectangle = when (wall.wallIndex) {
                1, 3 -> height.multiply(length)
                2, 4 -> width.multiply(height)
                else -> width.multiply(length)
            }
            val characteristic = LegacyArithmetic.positive(rectangle.sqrt(LegacyArithmetic.context).toDouble(), "E$row")
            val normalizedGap = BigDecimal(wall.gap.baseValue).divide(BigDecimal(characteristic), LegacyArithmetic.context)
            val attenuation = if (normalizedGap > BigDecimal(Double.MAX_VALUE)) 0.0
                else (1.0 + normalizedGap.toDouble()).pow(-1.67)
            val coefficient = BigDecimal(wall.convectionMultiplier).multiply(BigDecimal(6.25 - 5.25 * attenuation))
                .multiply(BigDecimal(23.278 * mean.pow(-0.639)))
                .multiply(BigDecimal(LegacyArithmetic.ratioPower(drop, wall.gap.baseValue, 0.25, "E$row")))
            LegacyArithmetic.coefficient(LegacyArithmetic.positive(coefficient.toDouble(), "E$row"), "E$row")
        }
        val areaRatio = BigDecimal(geometry.heatedZoneArea.baseValue)
            .divide(BigDecimal(geometry.originalEnclosureArea.baseValue), LegacyArithmetic.context)
        val denominator = BigDecimal.ONE.divide(ZONE_EMISSIVITY, LegacyArithmetic.context)
            .add(areaRatio.multiply(BigDecimal.ONE.divide(BigDecimal(input.enclosureEmissivity), LegacyArithmetic.context)
                .subtract(BigDecimal.ONE)))
        val emissivity = LegacyArithmetic.positive(BigDecimal.ONE.divide(denominator, LegacyArithmetic.context).toDouble(), "E22")
        val radiation = LegacyRadiation.coefficient(reference.kelvin, enclosureTemperature.kelvin, emissivity, "I22")
        val paths = mutableListOf<Pair<Double, Double>>()
        listOf(Triple(0, 2, geometry.heatedZoneFaceAreaYZ.baseValue),
            Triple(1, 3, geometry.heatedZoneFaceAreaXZ.baseValue),
            Triple(4, 5, geometry.heatedZoneFaceAreaXY.baseValue)).forEach { (first, second, area) ->
            paths.add(radiation to area)
            paths.add(radiation to area)
            paths.add(walls[first].baseValue to area)
            paths.add(walls[second].baseValue to area)
        }
        val resistance = LegacyThermalResistance.parallelPaths(paths, "O22")
        val trials = input.internalProbes.trialZoneRises.mapIndexed { index, rise ->
            val row = index + 19
            val trialResistance = input.internalProbes.trialResistances[index]
            InternalHeatTransferProbe(
                LegacyArithmetic.temperature(enclosureTemperature.kelvin + rise.kelvin, "N$row"),
                rise,
                trialResistance,
                LegacyArithmetic.power(LegacyThermalResistance.dissipatedPower(rise.kelvin,
                    trialResistance.value.baseValue, "U$row"), "U$row")
            )
        }
        return InternalHeatTransferResults(enclosureTemperature, reference,
            LegacyArithmetic.temperature(mean, "F16"), emissivity,
            LegacyArithmetic.coefficient(radiation, "I22"), LegacyArithmetic.freeze(walls),
            LegacyArithmetic.resistance(resistance, "O22"), LegacyArithmetic.freeze(trials))
    }
}

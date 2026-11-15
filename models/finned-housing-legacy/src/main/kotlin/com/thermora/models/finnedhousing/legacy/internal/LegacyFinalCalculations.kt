package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingFinalResults
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingGeometryResults
import java.math.BigDecimal
import kotlin.math.pow

internal object LegacyFinalCalculations {
    private const val ENCLOSURE_SCALE = 2.209
    private const val ENCLOSURE_EXPONENT = 0.8499
    private const val ZONE_SCALE = 5.5825
    private const val ZONE_EXPONENT = 0.858

    fun enclosureRise(power: Double): Double {
        LegacyArithmetic.positive(power, "AC12")
        return LegacyArithmetic.positive(ENCLOSURE_SCALE * power.pow(ENCLOSURE_EXPONENT), "AC12")
    }

    fun enclosureTemperature(input: FinnedHousingLegacyInput): Temperature =
        LegacyArithmetic.temperature(input.ambient.temperature.kelvin + enclosureRise(input.source.power.baseValue), "AA28")

    fun calculate(input: FinnedHousingLegacyInput, geometry: FinnedHousingGeometryResults): FinnedHousingFinalResults {
        val power = input.source.power.baseValue
        val ambient = input.ambient.temperature.kelvin
        val elementPower = input.criticalElement.power.baseValue
        val caseRise = enclosureRise(power)
        val zoneRise = LegacyArithmetic.positive(caseRise + ZONE_SCALE * power.pow(ZONE_EXPONENT), "AC25")
        val caseTemperature = LegacyArithmetic.temperature(ambient + caseRise, "AA28")
        val zoneTemperature = LegacyArithmetic.temperature(ambient + zoneRise, "AA29")
        val rise = BigDecimal(zoneRise)
        val denominator = BigDecimal(input.elementWithRadiatorArea.baseValue).multiply(BigDecimal(power))
        val base = BigDecimal(ambient).add(rise.multiply(BigDecimal("0.75")))
        val elementNumerator = rise.multiply(BigDecimal("0.25"))
            .multiply(BigDecimal(elementPower)).multiply(BigDecimal(geometry.heatedZoneArea.baseValue))
        val element = LegacyArithmetic.temperature(base.multiply(denominator).add(elementNumerator)
            .divide(denominator, LegacyArithmetic.context).toDouble(), "AA31")
        val junction = BigDecimal(input.criticalElement.transitionToCaseResistance.value.baseValue)
            .add(BigDecimal(input.criticalElement.caseToRadiatorResistance.value.baseValue))
            .multiply(BigDecimal(elementPower)).add(BigDecimal(element.kelvin))
        return FinnedHousingFinalResults(
            TemperatureDelta.fromKelvin(caseRise),
            LegacyArithmetic.resistance(caseRise / power, "AC13"),
            TemperatureDelta.fromKelvin(zoneRise),
            LegacyArithmetic.resistance(zoneRise / power, "AC26"),
            caseTemperature,
            zoneTemperature,
            LegacyArithmetic.temperature(LegacyArithmetic.mean(caseTemperature.kelvin, zoneTemperature.kelvin), "AA30"),
            element,
            LegacyArithmetic.temperature(junction.toDouble(), "AA32")
        )
    }
}

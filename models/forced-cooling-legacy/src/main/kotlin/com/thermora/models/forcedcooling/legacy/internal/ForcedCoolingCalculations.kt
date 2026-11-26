package com.thermora.models.forcedcooling.legacy.internal

import com.thermora.foundation.common.diagnostic.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.units.unit.*
import com.thermora.models.forcedcooling.legacy.input.*
import com.thermora.models.forcedcooling.legacy.output.*
import kotlin.math.*

internal class ForcedNumericalFailure(val cell: String, message: String, val sheet: String = "Продуваемый корпус") : RuntimeException(message)

/** Literal one-step Excel equations. 273 is the original Celsius coordinate offset, not a Foundation conversion. */
internal class ForcedCoolingCalculations {
    private val warnings = mutableListOf<Diagnostic>()
    private fun warn(cell: String, code: String, message: String) {
        warnings += Diagnostic("FORCED_COOLING_$code", message, DiagnosticSeverity.WARNING, cell,
            mapOf("sheet" to "Продуваемый корпус", "cell" to cell))
    }
    private fun finite(cell: String, value: Double, sheet: String = "Продуваемый корпус"): Double {
        if (!value.isFinite()) throw ForcedNumericalFailure(cell, "Non-finite numerical result", sheet)
        return value
    }
    private fun divide(cell: String, numerator: Double, denominator: Double): Double {
        if (denominator == 0.0) throw ForcedNumericalFailure(cell, "Zero denominator")
        return finite(cell, finite(cell, numerator) / finite(cell, denominator))
    }
    private fun root(cell: String, value: Double): Double {
        if (!value.isFinite() || value < 0) throw ForcedNumericalFailure(cell, "Invalid square-root argument")
        return sqrt(value)
    }
    private fun q(cell: String, value: Double, unit: MeasurementUnit) = Quantity(finite(cell, value), unit)
    private fun temp(cell: String, kelvin: Double): Temperature {
        if (!kelvin.isFinite() || kelvin < 0) throw ForcedNumericalFailure(cell, "Invalid absolute temperature")
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
    fun calculate(i: ForcedCoolingLegacyInput): Pair<ForcedCoolingLegacyOutput, List<Diagnostic>> {
        val ta=i.ambient.temperature.kelvin
        val tk=i.trialEnclosureTemperature.kelvin
        val tz=i.trialZoneTemperature.kelvin
        val rise=tk-ta // E5
        val zoneRise=tz-ta // J5
        val power=i.source.power.baseValue
        val length=i.enclosure.length.baseValue
        val width=i.enclosure.width.baseValue
        val height=i.enclosure.height.baseValue
        val zl=i.heatedZone.length.baseValue
        val zw=i.heatedZone.width.baseValue
        val zh=i.heatedZone.height.baseValue
        val sc=finite("V11", 2 * (width * length + height * (width + length)), "Исходные данные")
        val sz=finite("V12", 2 * (zl * zw + zh * i.heatedZone.volumeFillCoefficient * (zl + zw)), "Исходные данные")
        val volume=finite("V4", length * width * height - zl * zw * zh, "Исходные данные")
        if(volume<=0) throw ForcedNumericalFailure("V4", "Non-positive free air volume", "Исходные данные")
        val flow=i.fanVolumeFlow.baseValue
        val speed=divide("G9",flow*length,volume)
        val mean=temp("G10",(ta+tk)/2)
        val densityMean=temp("G13",(tz+tk)/2)
        val viscosity=finite("G10",0.0003*mean.kelvin.pow(1.9097)*1e-6)
        val alpha=divide("G10",0.8*0.026*root("G10",speed),root("G10",i.componentLength.baseValue*viscosity))
        val radiationFunction=divide("J8",5.67e-8*((tz-273+273).pow(4)-((ta-273+rise)+273).pow(4)),(tz-273)-(ta-273+rise))
        val radiation=finite("J6",0.8*radiationFunction)
        val casePower=finite("G11",0.0025*rise.pow(2)+0.3997*rise-0.3275)
        val airPower=finite("G12",power-casePower)
        val density=finite("G13",374.69*densityMean.kelvin.pow(-1.009))
        val airRise=divide("G13",airPower,2*flow*1000*density)
        val calculatedZoneRise=divide("G15",power+alpha*sz*airRise+radiation*sz*rise,sz*(alpha+radiation))
        val calculatedCaseRise=finite("G16",airRise-divide("G16",casePower-radiation*sz*(zoneRise-rise),alpha*sz))
        val calculatedAirPower=finite("G17",alpha*(sz*(zoneRise-airRise)+sc*(rise-airRise)))
        val calculatedCaseC=finite("N4",ta-273+calculatedCaseRise)
        val calculatedZoneC=finite("N5",ta-273+calculatedZoneRise)
        val calculatedCase=temp("T15",calculatedCaseC+273)
        val calculatedZone=temp("T16",calculatedZoneC+273)
        val air=temp("T17",ta+airRise)
        val element=temp("T18",ta+(calculatedZone.kelvin-ta)*(0.75+0.25*divide("T18",i.criticalElement.power.baseValue*sz,i.elementWithRadiatorArea.baseValue*power)))
        val junction=temp("T19",element.kelvin+(i.criticalElement.transitionToCaseResistance.value.baseValue+i.criticalElement.caseToRadiatorResistance.value.baseValue)*i.criticalElement.power.baseValue)
        val enclosurePercent=percent("P4",tk-273,calculatedCaseC)
        val zonePercent=percent("P5",tz-273,calculatedZoneC)
        val airPercent=percent("P6",airPower,calculatedAirPower)
        val percentages = listOf(enclosurePercent, zonePercent, airPercent)
        fun within(threshold: Double): Boolean? =
            if (percentages.any { it == null }) null else percentages.filterNotNull().all { abs(it) < threshold }
        val withinThreePercent = within(3.0)
        val withinOnePercent = within(1.0)
        val nextCase=nextTemperature("O12",tk+0.1*((calculatedCaseRise+ta)-tk))
        val nextZone=if(calculatedAirPower-airPower==0.0) {
            warn("O13","UNDEFINED_AUXILIARY","Zero denominator in suggested zone update");null
        } else nextTemperature("O13",tz+0.1*((calculatedZoneRise+ta)-tz)*(calculatedAirPower/(calculatedAirPower-airPower)))
        warn("G10:G17","LEGACY_CORRELATIONS","Unverified empirical correlations and legacy Celsius convergence coordinates are preserved literally")
        when (withinThreePercent) {
            null -> warn("P4:P6", "CONVERGENCE_UNDEFINED", "One or more relative convergence percentages are undefined")
            false -> warn("P4:P6","NOT_CONVERGED","Manual trial does not meet the 3 percent criterion")
            true -> Unit
        }
        if(airPower<0) warn("G12","NEGATIVE_AIR_POWER","Legacy polynomial predicts negative air power")
        return ForcedCoolingLegacyOutput(
            ForcedCoolingGeometryResults(q("V11",sc,StandardUnits.SQUARE_METER),q("V12",sz,StandardUnits.SQUARE_METER),q("V4",volume,StandardUnits.CUBIC_METER)),
            ForcedCoolingHeatTransferResults(mean,densityMean,q("G9",speed,StandardUnits.METER_PER_SECOND),q("G10",viscosity,StandardUnits.SQUARE_METER_PER_SECOND),q("G13",density,StandardUnits.KILOGRAM_PER_CUBIC_METER),
                q("J8",radiationFunction,StandardUnits.WATT_PER_SQUARE_METER_KELVIN),q("J6",radiation,StandardUnits.WATT_PER_SQUARE_METER_KELVIN),q("G10",alpha,StandardUnits.WATT_PER_SQUARE_METER_KELVIN)),
            ForcedCoolingAirBalanceResults(q("G11",casePower,StandardUnits.WATT),q("G12",airPower,StandardUnits.WATT),TemperatureDelta.fromKelvin(airRise),TemperatureDelta.fromKelvin(calculatedZoneRise),TemperatureDelta.fromKelvin(calculatedCaseRise),q("G17",calculatedAirPower,StandardUnits.WATT)),
            ForcedCoolingConvergenceResults(TemperatureDelta.fromKelvin((tk-273)-calculatedCaseC),TemperatureDelta.fromKelvin((tz-273)-calculatedZoneC),q("O6",airPower-calculatedAirPower,StandardUnits.WATT),enclosurePercent,zonePercent,airPercent,withinThreePercent,withinOnePercent),
            ForcedCoolingNextStep(nextCase,nextZone),
            ForcedCoolingFinalResults(calculatedCase,calculatedZone,air,element,junction)
        ) to warnings.toList()
    }
}

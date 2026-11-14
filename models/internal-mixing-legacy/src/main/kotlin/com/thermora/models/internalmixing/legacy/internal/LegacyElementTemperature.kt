package com.thermora.models.internalmixing.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Legacy Excel calculations for critical element
 * and semiconductor junction temperatures.
 *
 * AB22: element temperature
 * AB23: junction temperature
 */
internal object LegacyElementTemperature {

    private val BASE_FACTOR = BigDecimal("0.75")
    private val POWER_FACTOR = BigDecimal("0.25")
    private val TEMPERATURE_CONTEXT = MathContext(40, RoundingMode.HALF_EVEN)

    fun calculateElement(
        ambientTemperature: Temperature,
        heatedZoneRise: TemperatureDelta,
        heatedZoneArea: BigDecimal,
        elementPowerWatts: Double,
        totalPowerWatts: Double,
        elementArea: Double
    ): Temperature {

        require(totalPowerWatts.isFinite() && totalPowerWatts > 0.0)
        require(elementArea.isFinite() && elementArea > 0.0)
        require(heatedZoneArea.signum() > 0)
        require(elementPowerWatts.isFinite() && elementPowerWatts >= 0.0)

        val rise = BigDecimal(heatedZoneRise.kelvin)
        val denominator = BigDecimal(elementArea).multiply(BigDecimal(totalPowerWatts))
        val baseTemperature = BigDecimal(ambientTemperature.kelvin)
            .add(rise.multiply(BASE_FACTOR))
        val additionalHeatingNumerator = rise.multiply(POWER_FACTOR)
            .multiply(heatedZoneArea)
            .multiply(BigDecimal(elementPowerWatts))

        val result = baseTemperature.multiply(denominator)
            .add(additionalHeatingNumerator)
            .divide(denominator, TEMPERATURE_CONTEXT)
            .toDouble()

        require(result.isFinite() && result >= 0.0) {
            "Calculated element temperature is invalid"
        }

        return Temperature.fromKelvin(result)
    }

    fun calculateJunction(
        heatedZoneTemperature: Temperature,
        elementPowerWatts: Double,
        transitionToCaseResistance: Double,
        caseToRadiatorResistance: Double
    ): Temperature {

        require(elementPowerWatts.isFinite() && elementPowerWatts >= 0.0)
        require(transitionToCaseResistance.isFinite() &&
                transitionToCaseResistance >= 0.0)
        require(caseToRadiatorResistance.isFinite() &&
                caseToRadiatorResistance >= 0.0)

        val result = BigDecimal(transitionToCaseResistance)
            .add(BigDecimal(caseToRadiatorResistance))
            .multiply(BigDecimal(elementPowerWatts))
            .add(BigDecimal(heatedZoneTemperature.kelvin))
            .toDouble()

        require(result.isFinite() && result >= 0.0) {
            "Calculated junction temperature is invalid"
        }

        return Temperature.fromKelvin(result)
    }
}

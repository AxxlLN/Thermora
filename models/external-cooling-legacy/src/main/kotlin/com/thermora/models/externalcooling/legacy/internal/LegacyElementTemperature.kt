package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Excel AA30.
 *
 * Pe — critical element power, W.
 * Sz — heated-zone area, m².
 * Ae — element with radiator area, m².
 * P  — total power, W.
 */
internal object LegacyElementTemperature {

    private val TEMPERATURE_CONTEXT = MathContext(40, RoundingMode.HALF_EVEN)

    fun calculate(
        ambient: Temperature,
        heatedZoneRise: TemperatureDelta,
        elementPowerWatts: Double,
        heatedZoneAreaSquareMeters: BigDecimal,
        elementAreaSquareMeters: Double,
        totalPowerWatts: Double
    ): Temperature {
        require(
            listOf(
                elementPowerWatts,
                elementAreaSquareMeters,
                totalPowerWatts
            ).all { it.isFinite() }
        ) {
            "Thermal parameters must be finite"
        }

        require(
            elementPowerWatts >= 0.0 &&
                    heatedZoneAreaSquareMeters.signum() > 0 &&
                    elementAreaSquareMeters > 0.0 &&
                    totalPowerWatts > 0.0
        ) {
            "Invalid thermal parameters"
        }

        val rise = BigDecimal(heatedZoneRise.kelvin)
        val denominator = BigDecimal(elementAreaSquareMeters).multiply(BigDecimal(totalPowerWatts))
        val baseTemperature = BigDecimal(ambient.kelvin).add(rise.multiply(BigDecimal("0.75")))
        val additionalHeating = rise.multiply(BigDecimal("0.25"))
            .multiply(BigDecimal(elementPowerWatts))
            .multiply(heatedZoneAreaSquareMeters)
        val result = baseTemperature.multiply(denominator)
            .add(additionalHeating)
            .divide(denominator, TEMPERATURE_CONTEXT)
            .toDouble()

        return Temperature.fromKelvin(result)
    }
}

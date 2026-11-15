package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import java.math.BigDecimal

/**
 * Excel AA31.
 *
 * Rjc — junction-to-case resistance, K/W.
 * Rcr — case-to-radiator resistance, K/W.
 * Pe  — critical element power, W.
 */
internal object LegacyJunctionTemperature {

    fun calculate(
        elementTemperature: Temperature,
        elementPowerWatts: Double,
        junctionToCaseResistance: Double,
        caseToRadiatorResistance: Double
    ): Temperature {
        require(
            listOf(
                elementPowerWatts,
                junctionToCaseResistance,
                caseToRadiatorResistance
            ).all { it.isFinite() && it >= 0.0 }
        ) {
            "Power and thermal resistances must be finite and non-negative"
        }

        val result = BigDecimal(junctionToCaseResistance)
            .add(BigDecimal(caseToRadiatorResistance))
            .multiply(BigDecimal(elementPowerWatts))
            .add(BigDecimal(elementTemperature.kelvin))
            .toDouble()

        return Temperature.fromKelvin(result)
    }
}

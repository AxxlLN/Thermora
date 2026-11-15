package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.TemperatureDelta
import kotlin.math.pow

/**
 * Legacy empirical model for heated-zone temperature rise.
 *
 * Excel: AA23 = AA11 + 5.5605 * P^0.857
 *
 * P — total dissipated power, W.
 * AA11 — enclosure temperature rise, K.
 */
internal object LegacyHeatedZoneRiseCurve {

    fun calculate(
        enclosureRise: TemperatureDelta,
        totalPowerWatts: Double
    ): TemperatureDelta {
        require(totalPowerWatts > 0.0 && totalPowerWatts.isFinite()) {
            "Total power must be finite and positive"
        }

        val riseKelvin =
            enclosureRise.kelvin +
                    5.5605 * totalPowerWatts.pow(0.857)

        return TemperatureDelta.fromKelvin(riseKelvin)
    }
}

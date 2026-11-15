package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.TemperatureDelta
import kotlin.math.pow

/**
 * Legacy empirical model for enclosure temperature rise.
 *
 * Excel: AA11 = 1.7011 * P^0.9649
 *
 * P — total dissipated power, W.
 * Result — temperature rise, K.
 */
internal object LegacyEnclosureRiseCurve {

    fun calculate(totalPowerWatts: Double): TemperatureDelta {
        require(totalPowerWatts > 0.0 && totalPowerWatts.isFinite()) {
            "Total power must be finite and positive"
        }

        val riseKelvin =
            1.7011 * totalPowerWatts.pow(0.9649)

        return TemperatureDelta.fromKelvin(riseKelvin)
    }
}

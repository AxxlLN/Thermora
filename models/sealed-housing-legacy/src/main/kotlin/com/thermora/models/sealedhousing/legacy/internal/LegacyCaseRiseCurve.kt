package com.thermora.models.sealedhousing.legacy.internal

import com.thermora.foundation.units.temperature.TemperatureDelta
import kotlin.math.pow

internal object LegacyCaseRiseCurve {

    private const val SCALE = 3.1009
    private const val EXPONENT = 0.8601

    fun calculate(powerWatts: Double): TemperatureDelta {
        require(powerWatts > 0.0 && powerWatts.isFinite()) {
            "Power must be positive and finite"
        }

        val riseKelvin = SCALE * powerWatts.pow(EXPONENT)

        require(riseKelvin.isFinite()) {
            "Calculated enclosure temperature rise must be finite"
        }

        return TemperatureDelta.fromKelvin(riseKelvin)
    }
}
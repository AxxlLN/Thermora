package com.thermora.models.sealedhousing.legacy.internal

import com.thermora.foundation.units.temperature.TemperatureDelta
import kotlin.math.pow

internal object LegacyZoneRiseCurve {

    private const val SCALE = 5.5413
    private const val EXPONENT = 0.8578

    fun calculate(
        powerWatts: Double,
        enclosureRise: TemperatureDelta
    ): TemperatureDelta {
        require(powerWatts > 0.0 && powerWatts.isFinite()) {
            "Power must be positive and finite"
        }

        val additionalRiseKelvin = SCALE * powerWatts.pow(EXPONENT)

        val totalRiseKelvin =
            additionalRiseKelvin + enclosureRise.kelvin

        require(totalRiseKelvin.isFinite()) {
            "Calculated heated zone temperature rise must be finite"
        }

        return TemperatureDelta.fromKelvin(totalRiseKelvin)
    }
}
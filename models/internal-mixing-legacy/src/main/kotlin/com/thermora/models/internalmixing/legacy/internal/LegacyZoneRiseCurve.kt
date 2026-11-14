
package com.thermora.models.internalmixing.legacy.internal

import com.thermora.foundation.units.temperature.TemperatureDelta
import kotlin.math.pow

/**
 * Legacy heated-zone temperature rise correlation.
 *
 * Reproduces the original Excel empirical equation.
 * The applicability range is not established.
 */
internal object LegacyZoneRiseCurve {

    private const val COEFFICIENT = 5.2013
    private const val EXPONENT = 0.6493

    fun calculate(
        totalPowerWatts: Double,
        enclosureRise: TemperatureDelta
    ): TemperatureDelta {

        require(totalPowerWatts.isFinite() && totalPowerWatts > 0.0) {
            "Total power must be positive and finite"
        }

        val zoneRise =
            COEFFICIENT * totalPowerWatts.pow(EXPONENT) +
                    enclosureRise.kelvin

        require(zoneRise.isFinite() && zoneRise >= 0.0) {
            "Calculated zone rise must be finite and non-negative"
        }

        return TemperatureDelta.fromKelvin(zoneRise)
    }
}

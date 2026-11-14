package com.thermora.models.internalmixing.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

/**
 * Temperature arithmetic used by the original
 * internal mixing Excel model.
 */
internal object LegacyTemperatureArithmetic {

    fun addRise(
        temperature: Temperature,
        rise: TemperatureDelta
    ): Temperature {
        val result = temperature.kelvin + rise.kelvin

        require(result.isFinite() && result >= 0.0) {
            "Calculated absolute temperature must be finite and non-negative"
        }

        return Temperature.fromKelvin(result)
    }

    fun mean(
        first: Double,
        second: Double
    ): Double {
        require(first.isFinite() && second.isFinite()) {
            "Temperatures must be finite"
        }

        val sum = first + second
        return if (sum.isFinite()) sum / 2.0 else first / 2.0 + second / 2.0
    }

    fun meanTemperature(
        first: Temperature,
        second: Temperature
    ): Temperature =
        Temperature.fromKelvin(
            mean(first.kelvin, second.kelvin)
        )
}

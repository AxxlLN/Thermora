package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

/**
 * Legacy temperature calculations from Excel.
 *
 * AA27 — enclosure temperature.
 * AA28 — heated-zone temperature.
 * AA29 — internal air temperature.
 */
internal object LegacyTemperatureCalculator {

    fun enclosureTemperature(
        ambient: Temperature,
        enclosureRise: TemperatureDelta
    ): Temperature =
        Temperature.fromKelvin(
            ambient.kelvin + enclosureRise.kelvin
        )

    fun heatedZoneTemperature(
        ambient: Temperature,
        heatedZoneRise: TemperatureDelta
    ): Temperature =
        Temperature.fromKelvin(
            ambient.kelvin + heatedZoneRise.kelvin
        )

    fun internalAirTemperature(
        enclosure: Temperature,
        heatedZone: Temperature
    ): Temperature {
        val sum = enclosure.kelvin + heatedZone.kelvin
        val mean = if (sum.isFinite()) sum / 2.0 else enclosure.kelvin / 2.0 + heatedZone.kelvin / 2.0
        return Temperature.fromKelvin(mean)
    }
}

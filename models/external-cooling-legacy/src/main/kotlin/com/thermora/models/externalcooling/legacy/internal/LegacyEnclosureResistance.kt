package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits

/**
 * Excel: AA12 = AA11 / P
 *
 * Enclosure-to-ambient thermal resistance, K/W.
 */
internal object LegacyEnclosureResistance {

    fun calculate(
        enclosureRise: TemperatureDelta,
        totalPowerWatts: Double
    ): ThermalResistance {
        require(totalPowerWatts > 0.0 && totalPowerWatts.isFinite()) {
            "Total power must be finite and positive"
        }

        val resistanceKelvinPerWatt =
            enclosureRise.kelvin / totalPowerWatts

        return ThermalResistance(
            Quantity(
                resistanceKelvinPerWatt,
                StandardUnits.KELVIN_PER_WATT
            )
        )
    }
}

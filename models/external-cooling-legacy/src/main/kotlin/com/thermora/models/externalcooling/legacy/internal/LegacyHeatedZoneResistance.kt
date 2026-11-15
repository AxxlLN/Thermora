package com.thermora.models.externalcooling.legacy.internal

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits

/**
 * Excel: AA24 = AA23 / P
 *
 * Heated-zone-to-ambient thermal resistance, K/W.
 */
internal object LegacyHeatedZoneResistance {

    fun calculate(
        heatedZoneRise: TemperatureDelta,
        totalPowerWatts: Double
    ): ThermalResistance {
        require(totalPowerWatts > 0.0 && totalPowerWatts.isFinite()) {
            "Total power must be finite and positive"
        }

        val resistanceKelvinPerWatt =
            heatedZoneRise.kelvin / totalPowerWatts

        return ThermalResistance(
            Quantity(
                resistanceKelvinPerWatt,
                StandardUnits.KELVIN_PER_WATT
            )
        )
    }
}

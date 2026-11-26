package com.thermora.models.forcedcooling.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*

/** Saved one-step Excel state, no automatic iteration. */
data class ForcedCoolingAirBalanceResults(
    val enclosurePower: Quantity,
    val initialAirPower: Quantity,
    val airRise: TemperatureDelta,
    val calculatedZoneRise: TemperatureDelta,
    val calculatedEnclosureRise: TemperatureDelta,
    val calculatedAirPower: Quantity
)

package com.thermora.models.forcedcooling.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*

/** Saved one-step Excel state, no automatic iteration. */
data class ForcedCoolingConvergenceResults(
    val enclosureDifference: TemperatureDelta,
    val zoneDifference: TemperatureDelta,
    val airPowerDifference: Quantity,
    val enclosurePercent: Double?,
    val zonePercent: Double?,
    val airPowerPercent: Double?,
    val withinThreePercent: Boolean?,
    val withinOnePercent: Boolean?
)

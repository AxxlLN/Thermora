package com.thermora.models.forcedcooling.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*

/** Saved one-step Excel state, no automatic iteration. */
data class ForcedCoolingHeatTransferResults(
    val convectionMeanTemperature: Temperature,
    val densityMeanTemperature: Temperature,
    val airVelocity: Quantity,
    val kinematicViscosity: Quantity,
    val airDensity: Quantity,
    val radiationFunction: Quantity,
    val radiationCoefficient: Quantity,
    val convectionCoefficient: Quantity
)

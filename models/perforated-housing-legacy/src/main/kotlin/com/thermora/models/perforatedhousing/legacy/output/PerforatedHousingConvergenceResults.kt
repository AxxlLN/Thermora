package com.thermora.models.perforatedhousing.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*

/** Saved one-step results; legacy quadratic coefficients use Celsius-based Excel coordinates. */
data class PerforatedHousingConvergenceResults(
    val enclosureDifference: TemperatureDelta,
    val totalPowerDifference: Quantity,
    val airPowerDifference: Quantity,
    val enclosurePercent: Double?,
    val totalPowerPercent: Double?,
    val airPowerPercent: Double?,
    val withinThreePercent: Boolean?,
    val withinOnePercent: Boolean?
)

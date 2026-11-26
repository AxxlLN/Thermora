package com.thermora.models.perforatedhousing.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*

/** Saved one-step results; legacy quadratic coefficients use Celsius-based Excel coordinates. */
data class PerforatedHousingQuadraticResults(
    val legacyD: Double,
    val a: Double,
    val delta: Double,
    val b: Double,
    val discriminant: Double,
    val lowerRootTemperature: Temperature,
    val upperRootTemperature: Temperature,
    val secondAirTemperature: Temperature
)

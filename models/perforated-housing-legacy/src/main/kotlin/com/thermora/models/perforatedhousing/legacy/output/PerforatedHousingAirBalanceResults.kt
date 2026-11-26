package com.thermora.models.perforatedhousing.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*

/** Saved one-step results; legacy quadratic coefficients use Celsius-based Excel coordinates. */
data class PerforatedHousingAirBalanceResults(
    val heatedZoneAirTemperature: Temperature,
    val outletAirTemperature: Temperature,
    val massFlow: Quantity,
    val airMass: Quantity,
    val exchangeRate: Quantity,
    val airPower: Quantity,
    val enclosurePower: Quantity,
    val calculatedTotalPower: Quantity,
    val calculatedEnclosureTemperature: Temperature,
    val calculatedConvectionCoefficient: Quantity
)

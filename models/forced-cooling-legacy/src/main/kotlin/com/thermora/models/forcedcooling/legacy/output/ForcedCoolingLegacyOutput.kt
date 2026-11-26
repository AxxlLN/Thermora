package com.thermora.models.forcedcooling.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.calculation.api.model.CalculationOutput

/** Saved one-step Excel state, no automatic iteration. */
data class ForcedCoolingLegacyOutput(
    val geometry: ForcedCoolingGeometryResults,
    val heatTransfer: ForcedCoolingHeatTransferResults,
    val airBalance: ForcedCoolingAirBalanceResults,
    val convergence: ForcedCoolingConvergenceResults,
    val nextStep: ForcedCoolingNextStep,
    val finalResults: ForcedCoolingFinalResults
) : CalculationOutput

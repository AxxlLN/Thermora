package com.thermora.models.perforatedhousing.legacy.output
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.calculation.api.model.CalculationOutput

/** Saved one-step results; legacy quadratic coefficients use Celsius-based Excel coordinates. */
data class PerforatedHousingLegacyOutput(
    val geometry: PerforatedHousingGeometryResults,
    val quadratic: PerforatedHousingQuadraticResults,
    val heatTransfer: PerforatedHousingHeatTransferResults,
    val airBalance: PerforatedHousingAirBalanceResults,
    val convergence: PerforatedHousingConvergenceResults,
    val nextStep: PerforatedHousingNextStep,
    val finalResults: PerforatedHousingFinalResults
) : CalculationOutput

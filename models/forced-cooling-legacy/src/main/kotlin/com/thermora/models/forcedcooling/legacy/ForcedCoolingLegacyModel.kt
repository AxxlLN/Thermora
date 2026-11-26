package com.thermora.models.forcedcooling.legacy

import com.thermora.calculation.api.metadata.*
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.foundation.common.diagnostic.*
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.forcedcooling.legacy.input.ForcedCoolingLegacyInput
import com.thermora.models.forcedcooling.legacy.output.ForcedCoolingLegacyOutput
import com.thermora.models.forcedcooling.legacy.internal.*

class ForcedCoolingLegacyModel : CalculationModel<ForcedCoolingLegacyInput, ForcedCoolingLegacyOutput> {
    override val descriptor = CalculationModelDescriptor(CalculationModelId("forced-cooling-legacy"), "Forced Cooling (Legacy)", "1.0.0", "One manual Excel trial with convergence diagnostics")
    override val inputType = ForcedCoolingLegacyInput::class
    override val outputType = ForcedCoolingLegacyOutput::class
    override fun validate(input: ForcedCoolingLegacyInput) = ForcedCoolingLegacyValidator().validate(input)
    override fun calculate(input: ForcedCoolingLegacyInput): CalculationResult<ForcedCoolingLegacyOutput> {
        val validation = validate(input)
        if (!validation.isValid) return CalculationResult.Failure(validation.diagnostics)
        return try {
            val (output, diagnostics) = ForcedCoolingCalculations().calculate(input)
            CalculationResult.Success(output, diagnostics)
        } catch (e: ForcedNumericalFailure) {
            CalculationResult.Failure(listOf(Diagnostic("FORCED_COOLING_NUMERICAL_ERROR", e.message ?: "Invalid result", DiagnosticSeverity.ERROR, e.cell, mapOf("sheet" to e.sheet, "cell" to e.cell))))
        }
    }
}

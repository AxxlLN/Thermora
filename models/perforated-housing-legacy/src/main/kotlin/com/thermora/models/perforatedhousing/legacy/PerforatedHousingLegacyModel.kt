package com.thermora.models.perforatedhousing.legacy

import com.thermora.calculation.api.metadata.*
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.foundation.common.diagnostic.*
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.perforatedhousing.legacy.input.PerforatedHousingLegacyInput
import com.thermora.models.perforatedhousing.legacy.output.PerforatedHousingLegacyOutput
import com.thermora.models.perforatedhousing.legacy.internal.*

class PerforatedHousingLegacyModel : CalculationModel<PerforatedHousingLegacyInput, PerforatedHousingLegacyOutput> {
    override val descriptor = CalculationModelDescriptor(CalculationModelId("perforated-housing-legacy"), "Perforated Housing (Legacy)", "1.0.0", "One manual Excel trial with convergence diagnostics")
    override val inputType = PerforatedHousingLegacyInput::class
    override val outputType = PerforatedHousingLegacyOutput::class
    override fun validate(input: PerforatedHousingLegacyInput) = PerforatedHousingLegacyValidator().validate(input)
    override fun calculate(input: PerforatedHousingLegacyInput): CalculationResult<PerforatedHousingLegacyOutput> {
        val validation = validate(input)
        if (!validation.isValid) return CalculationResult.Failure(validation.diagnostics)
        return try {
            val (output, diagnostics) = PerforatedHousingCalculations().calculate(input)
            CalculationResult.Success(output, diagnostics)
        } catch (e: PerforatedNumericalFailure) {
            CalculationResult.Failure(listOf(Diagnostic("PERFORATED_HOUSING_NUMERICAL_ERROR", e.message ?: "Invalid result", DiagnosticSeverity.ERROR, e.cell, mapOf("sheet" to e.sheet, "cell" to e.cell))))
        }
    }
}

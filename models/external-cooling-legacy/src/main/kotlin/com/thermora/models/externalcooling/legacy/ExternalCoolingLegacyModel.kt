package com.thermora.models.externalcooling.legacy

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.internal.ExternalCoolingLegacyValidator
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput
import kotlin.reflect.KClass

class ExternalCoolingLegacyModel :
    CalculationModel<ExternalCoolingLegacyInput, ExternalCoolingLegacyOutput> {

    private val inputValidator = ExternalCoolingLegacyValidator()

    override val descriptor = CalculationModelDescriptor(
        id = CalculationModelId("external-cooling-legacy"),
        name = "External Cooling Legacy",
        version = "1.0.0",
        description = "Legacy external cooling thermal model based on Excel"
    )

    override val inputType: KClass<ExternalCoolingLegacyInput> =
        ExternalCoolingLegacyInput::class

    override val outputType: KClass<ExternalCoolingLegacyOutput> =
        ExternalCoolingLegacyOutput::class

    override fun validate(
        input: ExternalCoolingLegacyInput
    ): ValidationReport = inputValidator.validate(input)

    override fun calculate(
        input: ExternalCoolingLegacyInput
    ): CalculationResult<ExternalCoolingLegacyOutput> {

        val validation = validate(input)

        if (!validation.isValid) {
            return CalculationResult.Failure(validation.diagnostics)
        }

        return CalculationResult.Failure(
            listOf(
                Diagnostic(
                    code = "EXTERNAL_COOLING_NOT_IMPLEMENTED",
                    message = "External cooling calculations are not implemented yet",
                    severity = DiagnosticSeverity.ERROR,
                    source = descriptor.id.value
                )
            )
        )
    }
}

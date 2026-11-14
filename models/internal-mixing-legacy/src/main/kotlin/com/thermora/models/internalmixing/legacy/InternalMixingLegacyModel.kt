
package com.thermora.models.internalmixing.legacy

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput

class InternalMixingLegacyModel :
    CalculationModel<InternalMixingLegacyInput, InternalMixingLegacyOutput> {

    override val descriptor = CalculationModelDescriptor(
        id = CalculationModelId("internal-mixing-legacy"),
        name = "Internal Mixing (Legacy)",
        version = "1.0.0",
        description = "Internal mixing thermal model based on the original Excel methodology"
    )

    override val inputType = InternalMixingLegacyInput::class

    override val outputType = InternalMixingLegacyOutput::class

    override fun validate(
        input: InternalMixingLegacyInput
    ): ValidationReport {
        TODO("Input validation is not implemented yet")
    }

    override fun calculate(
        input: InternalMixingLegacyInput
    ): CalculationResult<InternalMixingLegacyOutput> {
        TODO("Internal mixing calculation is not implemented yet")
    }
}

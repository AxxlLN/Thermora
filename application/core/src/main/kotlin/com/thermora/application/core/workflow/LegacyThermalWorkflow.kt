package com.thermora.application.core.workflow

import com.thermora.application.core.ThermoraCore
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import com.thermora.models.internalmixing.legacy.input.SealedHousingBoundarySnapshot
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput

/**
 * Application-level orchestration of legacy thermal models.
 *
 * Execution order:
 * 1. Sealed housing
 * 2. Internal mixing
 *
 * All calculations are executed through ThermoraCore.
 */
class LegacyThermalWorkflow(
    private val core: ThermoraCore
) {

    fun execute(
        input: SealedHousingLegacyInput
    ): CalculationResult<LegacyThermalWorkflowOutput> =
        execute(LegacyThermalWorkflowInput(input, input.criticalElement))

    fun execute(
        input: LegacyThermalWorkflowInput
    ): CalculationResult<LegacyThermalWorkflowOutput> {
        val sealedInput = input.sealedHousingInput

        val sealedResult = core.calculate(
            modelId = SEALED_HOUSING_ID,
            input = sealedInput,
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        val sealedSuccess = when (sealedResult) {
            is CalculationResult.Success -> sealedResult
            is CalculationResult.Failure -> return sealedResult
        }

        val sealedOutput = sealedSuccess.value

        val boundary = SealedHousingBoundarySnapshot(
            ambient = sealedInput.ambient,
            source = sealedInput.source,
            enclosureRise = sealedOutput.enclosureRise,
            enclosureTemperature = sealedOutput.enclosureTemperature
        )

        val mixingInput = InternalMixingLegacyInput(
            sealedHousing = boundary,
            heatedZone = sealedInput.heatedZone,
            criticalElement = input.internalMixingCriticalElement,
            elementWithRadiatorArea = sealedInput.elementWithRadiatorArea
        )

        val mixingResult = core.calculate(
            modelId = INTERNAL_MIXING_ID,
            input = mixingInput,
            expectedOutputType = InternalMixingLegacyOutput::class
        )

        return when (mixingResult) {
            is CalculationResult.Success -> {
                CalculationResult.Success(
                    value = LegacyThermalWorkflowOutput(
                        sealedHousing = sealedOutput,
                        internalMixing = mixingResult.value
                    ),
                    diagnostics = sealedSuccess.diagnostics +
                            mixingResult.diagnostics
                )
            }

            is CalculationResult.Failure -> {
                CalculationResult.Failure(
                    diagnostics = sealedSuccess.diagnostics +
                            mixingResult.diagnostics
                )
            }
        }
    }

    private companion object {
        val SEALED_HOUSING_ID =
            CalculationModelId("sealed-housing-legacy")

        val INTERNAL_MIXING_ID =
            CalculationModelId("internal-mixing-legacy")
    }
}

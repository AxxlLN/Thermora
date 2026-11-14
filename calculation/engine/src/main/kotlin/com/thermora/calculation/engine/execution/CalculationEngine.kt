
package com.thermora.calculation.engine.execution

import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import java.util.concurrent.CancellationException
import kotlin.reflect.KClass

class CalculationEngine(
    private val registry: CalculationModelRegistry
) {

    fun <O : CalculationOutput> calculate(
        modelId: CalculationModelId,
        input: CalculationInput,
        expectedOutputType: KClass<O>
    ): CalculationResult<O> {

        val model = registry.getRegistered(modelId)
            ?: return failure(
                code = "CALCULATION_MODEL_NOT_FOUND",
                message = "Calculation model was not found",
                source = modelId.value
            )

        if (!model.inputType.isInstance(input)) {
            return failure(
                code = "CALCULATION_INPUT_TYPE_MISMATCH",
                message = "Input type does not match calculation model",
                source = modelId.value
            )
        }

        if (model.outputType != expectedOutputType) {
            return failure(
                code = "CALCULATION_OUTPUT_TYPE_MISMATCH",
                message = "Expected output type does not match calculation model",
                source = modelId.value
            )
        }

        val result = try {
            model.calculate(input)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw exception
        } catch (exception: Exception) {
            return failure(
                code = "CALCULATION_EXECUTION_FAILED",
                message = exception.message
                    ?: "Unexpected error during calculation",
                source = modelId.value
            )
        }

        return when (result) {
            is CalculationResult.Success -> {
                if (!expectedOutputType.isInstance(result.value)) {
                    failure(
                        code = "CALCULATION_INVALID_OUTPUT",
                        message = "Calculation model returned an invalid output type",
                        source = modelId.value
                    )
                } else {
                    @Suppress("UNCHECKED_CAST")
                    CalculationResult.Success(
                        value = result.value as O,
                        diagnostics = result.diagnostics
                    )
                }
            }

            is CalculationResult.Failure -> result
        }
    }

    private fun <T> failure(
        code: String,
        message: String,
        source: String
    ): CalculationResult<T> {
        return CalculationResult.Failure(
            listOf(
                Diagnostic(
                    code = code,
                    message = message,
                    severity = DiagnosticSeverity.ERROR,
                    source = source
                )
            )
        )
    }
}

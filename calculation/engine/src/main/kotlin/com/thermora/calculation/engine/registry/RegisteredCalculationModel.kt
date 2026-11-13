package com.thermora.calculation.engine.registry

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import kotlin.reflect.KClass

internal interface RegisteredCalculationModel {

    val descriptor: CalculationModelDescriptor

    val inputType: KClass<out CalculationInput>

    val outputType: KClass<out CalculationOutput>

    fun calculate(input: CalculationInput): CalculationResult<CalculationOutput>
}

internal class TypedRegisteredCalculationModel<
        I : CalculationInput,
        O : CalculationOutput
        >(
    private val model: CalculationModel<I, O>
) : RegisteredCalculationModel {

    override val descriptor = model.descriptor

    override val inputType = model.inputType

    override val outputType = model.outputType

    override fun calculate(
        input: CalculationInput
    ): CalculationResult<CalculationOutput> {

        if (!inputType.isInstance(input)) {
            return CalculationResult.Failure(
                listOf(
                    Diagnostic(
                        code = "CALCULATION_INPUT_TYPE_MISMATCH",
                        message = "Input type does not match calculation model",
                        severity = DiagnosticSeverity.ERROR,
                        source = descriptor.id.value,
                        details = mapOf(
                            "expected" to inputType.qualifiedName.orEmpty(),
                            "actual" to input::class.qualifiedName.orEmpty()
                        )
                    )
                )
            )
        }

        @Suppress("UNCHECKED_CAST")
        return model.calculate(input as I)
    }
}

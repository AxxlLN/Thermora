
package com.thermora.calculation.api.model

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.foundation.common.result.CalculationResult
import kotlin.reflect.KClass

/**
 * Contract implemented by every physical calculation model.
 *
 * @param I input data type
 * @param O output data type
 */
interface CalculationModel<
        I : CalculationInput,
        O : CalculationOutput
        > {
    val descriptor: CalculationModelDescriptor

    /**
     * Runtime type of the model input.
     */
    val inputType: KClass<I>

    /**
     * Runtime type of the model output.
     */
    val outputType: KClass<O>

    fun calculate(input: I): CalculationResult<O>
}

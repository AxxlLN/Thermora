package com.thermora.cli.binding

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput

/**
 * Associates a calculation model with its compatible
 * input and output snapshot codecs.
 *
 * Preserves the relationship between model and codec
 * types without unchecked casts.
 */
internal class ModelBinding<
        I : CalculationInput,
        O : CalculationOutput
        >(
    val model: CalculationModel<I, O>,
    val inputCodec: SnapshotCodec<I>,
    val outputCodec: SnapshotCodec<O>
) {

    val descriptor
        get() = model.descriptor

    val inputSchema
        get() = inputCodec.schema

    val outputSchema
        get() = outputCodec.schema

    init {
        require(model.inputType == inputCodec.valueType) {
            "Input codec type mismatch for model '${descriptor.id}'"
        }

        require(model.outputType == outputCodec.valueType) {
            "Output codec type mismatch for model '${descriptor.id}'"
        }
    }
}
package com.thermora.cli.binding

import com.thermora.application.core.codec.SnapshotCodecRegistry
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.infrastructure.serialization.registry.StandardSnapshotCodecs
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.internalmixing.legacy.InternalMixingLegacyModel
import com.thermora.models.externalcooling.legacy.ExternalCoolingLegacyModel
import com.thermora.models.finnedhousing.legacy.FinnedHousingLegacyModel
import com.thermora.models.perforatedhousing.legacy.PerforatedHousingLegacyModel
import com.thermora.models.forcedcooling.legacy.ForcedCoolingLegacyModel
import java.util.Collections

/**
 * Provides the default set of calculation model bindings
 * supported by the Thermora CLI.
 *
 * Each binding explicitly associates a model with its
 * versioned input and output snapshot schemas.
 */
internal object DefaultModelBindings {

    /**
     * Creates an independent set of default model bindings.
     */
    fun create(): List<ModelBinding<*, *>> {
        val codecs = SnapshotCodecRegistry(
            StandardSnapshotCodecs.all()
        )

        return Collections.unmodifiableList(
            listOf(
                bind(
                    model = SealedHousingLegacyModel(),
                    inputSchema = SnapshotSchema(
                        "thermora.sealed-housing.legacy.input",
                        1
                    ),
                    outputSchema = SnapshotSchema(
                        "thermora.sealed-housing.legacy.output",
                        1
                    ),
                    codecs = codecs
                ),

                bind(
                    model = InternalMixingLegacyModel(),
                    inputSchema = SnapshotSchema(
                        "thermora.internal-mixing.legacy.input",
                        1
                    ),
                    outputSchema = SnapshotSchema(
                        "thermora.internal-mixing.legacy.output",
                        1
                    ),
                    codecs = codecs
                ),

                bind(
                    model = ExternalCoolingLegacyModel(),
                    inputSchema = SnapshotSchema(
                        "thermora.external-cooling.legacy.input",
                        1
                    ),
                    outputSchema = SnapshotSchema(
                        "thermora.external-cooling.legacy.output",
                        1
                    ),
                    codecs = codecs
                ),

                bind(
                    model = FinnedHousingLegacyModel(),
                    inputSchema = SnapshotSchema(
                        "thermora.finned-housing.legacy.input",
                        1
                    ),
                    outputSchema = SnapshotSchema(
                        "thermora.finned-housing.legacy.output",
                        1
                    ),
                    codecs = codecs
                ),

                bind(
                    model = PerforatedHousingLegacyModel(),
                    inputSchema = SnapshotSchema(
                        "thermora.perforated-housing.legacy.input",
                        1
                    ),
                    outputSchema = SnapshotSchema(
                        "thermora.perforated-housing.legacy.output",
                        1
                    ),
                    codecs = codecs
                ),

                bind(
                    model = ForcedCoolingLegacyModel(),
                    inputSchema = SnapshotSchema(
                        "thermora.forced-cooling.legacy.input",
                        1
                    ),
                    outputSchema = SnapshotSchema(
                        "thermora.forced-cooling.legacy.output",
                        1
                    ),
                    codecs = codecs
                )
            )
        )
    }

    /**
     * Resolves compatible codecs and creates a type-safe
     * binding for the specified calculation model.
     */
    private fun <
            I : CalculationInput,
            O : CalculationOutput
            > bind(
        model: CalculationModel<I, O>,
        inputSchema: SnapshotSchema,
        outputSchema: SnapshotSchema,
        codecs: SnapshotCodecRegistry
    ): ModelBinding<I, O> {

        val inputCodec = checkNotNull(
            codecs.find(inputSchema, model.inputType)
        ) {
            "Compatible input codec not found: $inputSchema"
        }

        val outputCodec = checkNotNull(
            codecs.find(outputSchema, model.outputType)
        ) {
            "Compatible output codec not found: $outputSchema"
        }

        return ModelBinding(
            model = model,
            inputCodec = inputCodec,
            outputCodec = outputCodec
        )
    }
}
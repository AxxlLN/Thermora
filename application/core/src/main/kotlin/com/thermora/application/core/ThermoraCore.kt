package com.thermora.application.core

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.engine.execution.CalculationEngine
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.result.CalculationResult
import kotlin.reflect.KClass

/**
 * Public facade for the Thermora calculation system.
 *
 * Client applications interact with this class instead
 * of accessing CalculationEngine and CalculationModelRegistry directly.
 */
class ThermoraCore internal constructor(
    private val engine: CalculationEngine,
    private val registry: CalculationModelRegistry
) {

    /**
     * Executes a registered calculation model.
     */
    fun <O : CalculationOutput> calculate(
        modelId: CalculationModelId,
        input: CalculationInput,
        expectedOutputType: KClass<O>
    ): CalculationResult<O> {
        return engine.calculate(
            modelId = modelId,
            input = input,
            expectedOutputType = expectedOutputType
        )
    }

    /**
     * Returns descriptors of all registered models.
     */
    fun availableModels(): List<CalculationModelDescriptor> {
        return registry.getAll()
    }

    /**
     * Returns a model descriptor by its identifier.
     */
    fun findModel(
        modelId: CalculationModelId
    ): CalculationModelDescriptor? {
        return registry.find(modelId)
    }
}

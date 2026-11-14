package com.thermora.application.core

import com.thermora.application.core.configuration.DefaultModelConfiguration
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.engine.execution.CalculationEngine
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.result.CalculationResult

/**
 * Factory for creating configured ThermoraCore instances.
 */
object ThermoraCoreFactory {

    /**
     * Creates the core with the standard set of calculation models.
     */
    fun createDefault(): ThermoraCore {
        return create(DefaultModelConfiguration.models())
    }

    /**
     * Creates the core with a custom set of calculation models.
     */
    fun create(
        models: List<CalculationModel<out CalculationInput, out CalculationOutput>>
    ): ThermoraCore {
        val registry = CalculationModelRegistry()

        models.forEach { model ->
            registerModel(registry, model)
        }

        return ThermoraCore(
            engine = CalculationEngine(registry),
            registry = registry
        )
    }

    private fun <I : CalculationInput, O : CalculationOutput> registerModel(
        registry: CalculationModelRegistry,
        model: CalculationModel<I, O>
    ) {
        val result = registry.register(model)

        check(result is CalculationResult.Success) {
            "Failed to register model '${model.descriptor.id}': $result"
        }
    }
}

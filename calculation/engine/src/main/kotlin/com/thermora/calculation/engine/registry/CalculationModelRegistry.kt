package com.thermora.calculation.engine.registry

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import java.util.concurrent.ConcurrentHashMap

class CalculationModelRegistry {

    private val models =
        ConcurrentHashMap<CalculationModelId, RegisteredCalculationModel>()

    fun <I : CalculationInput, O : CalculationOutput> register(
        model: CalculationModel<I, O>
    ): CalculationResult<Unit> {

        val registered = TypedRegisteredCalculationModel(model)

        val existing = models.putIfAbsent(
            model.descriptor.id,
            registered
        )

        if (existing != null) {
            return CalculationResult.Failure(
                listOf(
                    Diagnostic(
                        code = "CALCULATION_MODEL_ALREADY_REGISTERED",
                        message = "Calculation model is already registered",
                        severity = DiagnosticSeverity.ERROR,
                        source = model.descriptor.id.value
                    )
                )
            )
        }

        return CalculationResult.Success(Unit)
    }

    fun unregister(id: CalculationModelId): Boolean {
        return models.remove(id) != null
    }

    fun find(id: CalculationModelId): CalculationModelDescriptor? {
        return models[id]?.descriptor
    }

    fun getAll(): List<CalculationModelDescriptor> {
        return models.values
            .map { it.descriptor }
            .sortedBy { it.id.value }
    }

    internal fun getRegistered(
        id: CalculationModelId
    ): RegisteredCalculationModel? {
        return models[id]
    }
}

package com.thermora.cli.binding

import com.thermora.calculation.api.metadata.CalculationModelId
import java.util.Collections

/**
 * Immutable registry of calculation model bindings.
 *
 * Each model identifier is associated with exactly one binding.
 * Registered bindings are exposed in deterministic identifier order.
 */
internal class ModelBindingRegistry(
    bindings: Collection<ModelBinding<*, *>>
) {

    private val bindingsById:
            Map<CalculationModelId, ModelBinding<*, *>>

    private val registeredBindings:
            List<ModelBinding<*, *>>

    init {
        val entries =
            LinkedHashMap<CalculationModelId, ModelBinding<*, *>>()

        for (binding in bindings) {
            val id = binding.descriptor.id

            require(!entries.containsKey(id)) {
                "Duplicate model binding: '${id.value}'"
            }

            entries[id] = binding
        }

        bindingsById = Collections.unmodifiableMap(entries)

        registeredBindings = Collections.unmodifiableList(
            entries.values.sortedBy {
                it.descriptor.id.value
            }
        )
    }

    /**
     * Returns a binding associated with the specified identifier,
     * or null if the model is not registered.
     */
    fun find(
        id: CalculationModelId
    ): ModelBinding<*, *>? = bindingsById[id]

    /**
     * Returns all registered bindings in identifier order.
     */
    fun all(): List<ModelBinding<*, *>> =
        registeredBindings

    /**
     * Checks whether the specified model is registered.
     */
    fun contains(id: CalculationModelId): Boolean =
        bindingsById.containsKey(id)

    /**
     * Number of registered model bindings.
     */
    val size: Int
        get() = bindingsById.size
}
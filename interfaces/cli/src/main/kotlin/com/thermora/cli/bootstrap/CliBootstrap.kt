package com.thermora.cli.bootstrap

import com.thermora.application.core.ThermoraCore
import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.cli.binding.DefaultModelBindings
import com.thermora.cli.binding.ModelBinding
import com.thermora.cli.binding.ModelBindingRegistry

/**
 * Runtime configuration of the Thermora CLI.
 *
 * Contains the calculation core and its corresponding
 * model binding registry.
 */
internal class CliRuntime(
    val core: ThermoraCore,
    val bindings: ModelBindingRegistry
)

/**
 * Composition root for the Thermora CLI.
 *
 * Ensures that the calculation core and the binding registry
 * operate on the same calculation model instances.
 */
internal object CliBootstrap {

    /**
     * Creates the default CLI runtime configuration.
     */
    fun createDefault(): CliRuntime =
        create(DefaultModelBindings.create())

    /**
     * Creates a CLI runtime using the specified model bindings.
     *
     * Supports custom configurations and isolated testing.
     */
    fun create(
        bindings: Collection<ModelBinding<*, *>>
    ): CliRuntime {

        val registry = ModelBindingRegistry(bindings)

        val core = ThermoraCoreFactory.create(
            registry.all().map { it.model }
        )

        return CliRuntime(
            core = core,
            bindings = registry
        )
    }
}
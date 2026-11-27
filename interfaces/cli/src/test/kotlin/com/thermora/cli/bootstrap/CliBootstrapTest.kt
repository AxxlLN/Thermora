package com.thermora.cli.bootstrap

import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.cli.binding.DefaultModelBindings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CliBootstrapTest {

    @Test
    fun `creates default runtime with six models`() {
        val runtime = CliBootstrap.createDefault()

        assertEquals(6, runtime.bindings.size)
        assertEquals(
            6,
            runtime.core.availableModels().size
        )
    }

    @Test
    fun `core and registry expose identical descriptors`() {
        val runtime = CliBootstrap.createDefault()

        val bindingDescriptors = runtime.bindings.all()
            .map { it.descriptor }

        val coreDescriptors =
            runtime.core.availableModels()

        assertEquals(
            bindingDescriptors,
            coreDescriptors
        )
    }

    @Test
    fun `all registered models are available through core`() {
        val runtime = CliBootstrap.createDefault()

        for (binding in runtime.bindings.all()) {
            assertEquals(
                binding.descriptor,
                runtime.core.findModel(
                    binding.descriptor.id
                )
            )
        }
    }

    @Test
    fun `supports custom model configuration`() {
        val binding = DefaultModelBindings.create().first()

        val runtime = CliBootstrap.create(
            listOf(binding)
        )

        assertEquals(1, runtime.bindings.size)
        assertEquals(
            1,
            runtime.core.availableModels().size
        )

        assertSame(
            binding,
            runtime.bindings.find(
                binding.descriptor.id
            )
        )

        assertEquals(
            binding.descriptor,
            runtime.core.findModel(
                binding.descriptor.id
            )
        )
    }

    @Test
    fun `preserves provided binding instances`() {
        val bindings = DefaultModelBindings.create()

        val runtime = CliBootstrap.create(bindings)

        for (binding in bindings) {
            val registered = runtime.bindings.find(
                binding.descriptor.id
            )

            assertSame(binding, registered)

            assertSame(
                binding.model,
                registered?.model
            )
        }
    }

    @Test
    fun `supports empty runtime configuration`() {
        val runtime = CliBootstrap.create(emptyList())

        assertEquals(0, runtime.bindings.size)

        assertTrue(
            runtime.bindings.all().isEmpty()
        )

        assertTrue(
            runtime.core.availableModels().isEmpty()
        )
    }

    @Test
    fun `rejects duplicate model identifiers`() {
        val binding = DefaultModelBindings.create().first()

        assertFailsWith<IllegalArgumentException> {
            CliBootstrap.create(
                listOf(binding, binding)
            )
        }
    }

    @Test
    fun `unknown model is absent from core and registry`() {
        val runtime = CliBootstrap.createDefault()

        val unknownId = CalculationModelId(
            "unknown-model"
        )

        assertNull(
            runtime.bindings.find(unknownId)
        )

        assertNull(
            runtime.core.findModel(unknownId)
        )
    }

    @Test
    fun `creates independent runtime instances`() {
        val first = CliBootstrap.createDefault()
        val second = CliBootstrap.createDefault()

        assertNotSame(first, second)
        assertNotSame(first.core, second.core)
        assertNotSame(first.bindings, second.bindings)

        for (binding in first.bindings.all()) {
            val correspondingBinding =
                second.bindings.find(
                    binding.descriptor.id
                )

            assertNotSame(
                binding,
                correspondingBinding
            )

            assertNotSame(
                binding.model,
                correspondingBinding?.model
            )
        }
    }
}
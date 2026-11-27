package com.thermora.cli.binding

import com.thermora.calculation.api.metadata.CalculationModelId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ModelBindingRegistryTest {

    @Test
    fun `registers all provided bindings`() {
        val bindings = DefaultModelBindings.create()
        val registry = ModelBindingRegistry(bindings)

        assertEquals(bindings.size, registry.size)

        for (binding in bindings) {
            assertSame(
                binding,
                registry.find(binding.descriptor.id)
            )
        }
    }

    @Test
    fun `rejects duplicate model identifiers`() {
        val binding = DefaultModelBindings.create().first()

        assertFailsWith<IllegalArgumentException> {
            ModelBindingRegistry(
                listOf(binding, binding)
            )
        }
    }

    @Test
    fun `returns null for unknown model identifier`() {
        val registry = ModelBindingRegistry(
            DefaultModelBindings.create()
        )

        assertNull(
            registry.find(
                CalculationModelId("unknown-model")
            )
        )
    }

    @Test
    fun `checks whether model is registered`() {
        val bindings = DefaultModelBindings.create()
        val registry = ModelBindingRegistry(bindings)

        assertTrue(
            registry.contains(
                bindings.first().descriptor.id
            )
        )

        assertFalse(
            registry.contains(
                CalculationModelId("unknown-model")
            )
        )
    }

    @Test
    fun `returns bindings sorted by model identifier`() {
        val bindings = DefaultModelBindings.create()
        val registry = ModelBindingRegistry(
            bindings.reversed()
        )

        val expectedIds = bindings
            .map { it.descriptor.id.value }
            .sorted()

        val actualIds = registry.all()
            .map { it.descriptor.id.value }

        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun `supports empty registry`() {
        val registry = ModelBindingRegistry(emptyList())

        assertEquals(0, registry.size)
        assertTrue(registry.all().isEmpty())

        assertNull(
            registry.find(
                CalculationModelId("unknown-model")
            )
        )
    }

    @Test
    fun `protects registry from source collection mutation`() {
        val bindings = DefaultModelBindings.create()
            .toMutableList()

        val expectedSize = bindings.size

        val registry = ModelBindingRegistry(bindings)

        bindings.clear()

        assertEquals(expectedSize, registry.size)
        assertEquals(expectedSize, registry.all().size)
    }

    @Test
    fun `prevents mutation of returned binding list`() {
        val registry = ModelBindingRegistry(
            DefaultModelBindings.create()
        )

        val bindings = registry.all()
                as MutableList<ModelBinding<*, *>>

        assertFailsWith<UnsupportedOperationException> {
            bindings.clear()
        }

        assertEquals(6, registry.size)
        assertEquals(6, registry.all().size)
    }
}
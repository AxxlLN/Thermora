package com.thermora.cli.binding

import com.thermora.application.ports.snapshot.SnapshotSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class DefaultModelBindingsTest {

    @Test
    fun `creates bindings for all six legacy models`() {
        val bindings = DefaultModelBindings.create()

        val expectedIds = setOf(
            "sealed-housing-legacy",
            "internal-mixing-legacy",
            "external-cooling-legacy",
            "finned-housing-legacy",
            "perforated-housing-legacy",
            "forced-cooling-legacy"
        )

        val actualIds = bindings
            .map { it.descriptor.id.value }
            .toSet()

        assertEquals(6, bindings.size)
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun `input codecs match model input types`() {
        val bindings = DefaultModelBindings.create()

        for (binding in bindings) {
            assertEquals(
                binding.model.inputType,
                binding.inputCodec.valueType,
                "Input type mismatch for ${binding.descriptor.id}"
            )
        }
    }

    @Test
    fun `output codecs match model output types`() {
        val bindings = DefaultModelBindings.create()

        for (binding in bindings) {
            assertEquals(
                binding.model.outputType,
                binding.outputCodec.valueType,
                "Output type mismatch for ${binding.descriptor.id}"
            )
        }
    }

    @Test
    fun `all input and output schemas are unique`() {
        val bindings = DefaultModelBindings.create()

        val schemas = bindings.flatMap {
            listOf(
                it.inputSchema,
                it.outputSchema
            )
        }

        assertEquals(12, schemas.size)
        assertEquals(12, schemas.toSet().size)
    }

    @Test
    fun `all snapshot schemas use version one`() {
        val bindings = DefaultModelBindings.create()

        for (binding in bindings) {
            assertEquals(
                1,
                binding.inputSchema.version
            )

            assertEquals(
                1,
                binding.outputSchema.version
            )
        }
    }

    @Test
    fun `all legacy models use version one point zero point zero`() {
        val bindings = DefaultModelBindings.create()

        for (binding in bindings) {
            assertEquals(
                "1.0.0",
                binding.descriptor.version
            )
        }
    }

    @Test
    fun `creates independent model instances`() {
        val first = DefaultModelBindings.create()
            .associateBy { it.descriptor.id }

        val second = DefaultModelBindings.create()
            .associateBy { it.descriptor.id }

        assertEquals(first.keys, second.keys)

        for (id in first.keys) {
            assertNotSame(
                first.getValue(id).model,
                second.getValue(id).model
            )
        }
    }

    @Test
    fun `all input schemas have expected identifiers`() {
        val expectedSchemas = mapOf(
            "sealed-housing-legacy" to
                    "thermora.sealed-housing.legacy.input",

            "internal-mixing-legacy" to
                    "thermora.internal-mixing.legacy.input",

            "external-cooling-legacy" to
                    "thermora.external-cooling.legacy.input",

            "finned-housing-legacy" to
                    "thermora.finned-housing.legacy.input",

            "perforated-housing-legacy" to
                    "thermora.perforated-housing.legacy.input",

            "forced-cooling-legacy" to
                    "thermora.forced-cooling.legacy.input"
        )

        for (binding in DefaultModelBindings.create()) {
            val expectedSchema = SnapshotSchema(
                expectedSchemas.getValue(
                    binding.descriptor.id.value
                ),
                1
            )

            assertEquals(
                expectedSchema,
                binding.inputSchema
            )
        }
    }

    @Test
    fun `all output schemas have expected identifiers`() {
        val expectedSchemas = mapOf(
            "sealed-housing-legacy" to
                    "thermora.sealed-housing.legacy.output",

            "internal-mixing-legacy" to
                    "thermora.internal-mixing.legacy.output",

            "external-cooling-legacy" to
                    "thermora.external-cooling.legacy.output",

            "finned-housing-legacy" to
                    "thermora.finned-housing.legacy.output",

            "perforated-housing-legacy" to
                    "thermora.perforated-housing.legacy.output",

            "forced-cooling-legacy" to
                    "thermora.forced-cooling.legacy.output"
        )

        for (binding in DefaultModelBindings.create()) {
            val expectedSchema = SnapshotSchema(
                expectedSchemas.getValue(
                    binding.descriptor.id.value
                ),
                1
            )

            assertEquals(
                expectedSchema,
                binding.outputSchema
            )
        }
    }
}
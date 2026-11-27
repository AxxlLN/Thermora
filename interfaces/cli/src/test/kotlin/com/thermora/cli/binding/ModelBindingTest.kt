package com.thermora.cli.binding

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ModelBindingTest {

    @Test
    fun `preserves model and codec instances`() {
        val binding = DefaultModelBindings.create().first()

        verifyBinding(binding)
    }

    @Test
    fun `exposes model descriptor`() {
        val binding = DefaultModelBindings.create().first()

        assertEquals(
            binding.model.descriptor,
            binding.descriptor
        )
    }

    @Test
    fun `exposes input schema`() {
        val binding = DefaultModelBindings.create().first()

        assertEquals(
            binding.inputCodec.schema,
            binding.inputSchema
        )
    }

    @Test
    fun `exposes output schema`() {
        val binding = DefaultModelBindings.create().first()

        assertEquals(
            binding.outputCodec.schema,
            binding.outputSchema
        )
    }

    @Test
    fun `rejects incompatible input codec`() {
        val bindings = DefaultModelBindings.create()

        verifyIncompatibleInput(
            bindings[0],
            bindings[1].inputCodec
        )
    }

    @Test
    fun `rejects incompatible output codec`() {
        val bindings = DefaultModelBindings.create()

        verifyIncompatibleOutput(
            bindings[0],
            bindings[1].outputCodec
        )
    }

    private fun <
            I : CalculationInput,
            O : CalculationOutput
            > verifyBinding(
        original: ModelBinding<I, O>
    ) {
        val binding = ModelBinding(
            model = original.model,
            inputCodec = original.inputCodec,
            outputCodec = original.outputCodec
        )

        assertSame(original.model, binding.model)
        assertSame(original.inputCodec, binding.inputCodec)
        assertSame(original.outputCodec, binding.outputCodec)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <
            I : CalculationInput,
            O : CalculationOutput
            > verifyIncompatibleInput(
        binding: ModelBinding<I, O>,
        incompatibleCodec: SnapshotCodec<*>
    ) {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                ModelBinding(
                    model = binding.model,
                    inputCodec = incompatibleCodec
                            as SnapshotCodec<I>,
                    outputCodec = binding.outputCodec
                )
            }

        assertTrue(
            exception.message.orEmpty()
                .contains("Input codec type mismatch")
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <
            I : CalculationInput,
            O : CalculationOutput
            > verifyIncompatibleOutput(
        binding: ModelBinding<I, O>,
        incompatibleCodec: SnapshotCodec<*>
    ) {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                ModelBinding(
                    model = binding.model,
                    inputCodec = binding.inputCodec,
                    outputCodec = incompatibleCodec
                            as SnapshotCodec<O>
                )
            }

        assertTrue(
            exception.message.orEmpty()
                .contains("Output codec type mismatch")
        )
    }
}
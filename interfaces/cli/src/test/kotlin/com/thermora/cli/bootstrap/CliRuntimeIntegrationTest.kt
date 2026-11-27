package com.thermora.cli.bootstrap

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.codec.SnapshotCodecResult
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.cli.binding.ModelBinding
import com.thermora.foundation.common.result.CalculationResult
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class CliRuntimeIntegrationTest {

    @Test
    fun `core executes the exact model instance registered in binding`() {
        val model = TestModel()
        val binding = createBinding(model)

        val runtime = CliBootstrap.create(
            listOf(binding)
        )

        assertSame(
            model,
            runtime.bindings.find(
                model.descriptor.id
            )?.model
        )

        val result = runtime.core.calculate(
            modelId = model.descriptor.id,
            input = TestInput(21),
            expectedOutputType = TestOutput::class
        )

        val success = assertIs<
                CalculationResult.Success<TestOutput>
                >(result)

        assertEquals(TestOutput(42), success.value)
        assertEquals(1, model.validationCalls)
        assertEquals(1, model.calculationCalls)
    }

    @Test
    fun `supports complete decode calculate encode workflow`() {
        val model = TestModel()
        val binding = createBinding(model)

        val runtime = CliBootstrap.create(
            listOf(binding)
        )

        val inputPayload = SnapshotPayload(
            schema = binding.inputSchema,
            content = """{"value":21}"""
        )

        val decoded = assertIs<
                SnapshotCodecResult.Success<TestInput>
                >(
            binding.inputCodec.decode(inputPayload)
        )

        val calculated = assertIs<
                CalculationResult.Success<TestOutput>
                >(
            runtime.core.calculate(
                modelId = binding.descriptor.id,
                input = decoded.value,
                expectedOutputType = binding.model.outputType
            )
        )

        val encoded = assertIs<
                SnapshotCodecResult.Success<SnapshotPayload>
                >(
            binding.outputCodec.encode(calculated.value)
        )

        assertEquals(
            binding.outputSchema,
            encoded.value.schema
        )

        assertEquals(
            """{"value":42}""",
            encoded.value.content
        )

        assertEquals(1, model.validationCalls)
        assertEquals(1, model.calculationCalls)
    }

    private fun createBinding(
        model: TestModel
    ): ModelBinding<TestInput, TestOutput> =
        ModelBinding(
            model = model,
            inputCodec = TestInputCodec(),
            outputCodec = TestOutputCodec()
        )

    private data class TestInput(
        val value: Int
    ) : CalculationInput

    private data class TestOutput(
        val value: Int
    ) : CalculationOutput

    private class TestModel :
        CalculationModel<TestInput, TestOutput> {

        override val descriptor =
            CalculationModelDescriptor(
                id = CalculationModelId("test-model"),
                name = "Test Model",
                version = "1.0.0"
            )

        override val inputType: KClass<TestInput> =
            TestInput::class

        override val outputType: KClass<TestOutput> =
            TestOutput::class

        var validationCalls = 0
            private set

        var calculationCalls = 0
            private set

        override fun validate(
            input: TestInput
        ): ValidationReport {
            validationCalls++

            return ValidationReport()
        }

        override fun calculate(
            input: TestInput
        ): CalculationResult<TestOutput> {
            calculationCalls++

            return CalculationResult.Success(
                TestOutput(input.value * 2)
            )
        }
    }

    private class TestInputCodec :
        SnapshotCodec<TestInput> {

        override val schema = SnapshotSchema(
            "thermora.test.input",
            1
        )

        override val valueType: KClass<TestInput> =
            TestInput::class

        override fun encode(
            value: TestInput
        ): SnapshotCodecResult<SnapshotPayload> =
            SnapshotCodecResult.Success(
                SnapshotPayload(
                    schema = schema,
                    content = """{"value":${value.value}}"""
                )
            )

        override fun decode(
            payload: SnapshotPayload
        ): SnapshotCodecResult<TestInput> {
            check(payload.schema == schema)
            check(payload.content == """{"value":21}""")

            return SnapshotCodecResult.Success(
                TestInput(21)
            )
        }
    }

    private class TestOutputCodec :
        SnapshotCodec<TestOutput> {

        override val schema = SnapshotSchema(
            "thermora.test.output",
            1
        )

        override val valueType: KClass<TestOutput> =
            TestOutput::class

        override fun encode(
            value: TestOutput
        ): SnapshotCodecResult<SnapshotPayload> =
            SnapshotCodecResult.Success(
                SnapshotPayload(
                    schema = schema,
                    content = """{"value":${value.value}}"""
                )
            )

        override fun decode(
            payload: SnapshotPayload
        ): SnapshotCodecResult<TestOutput> {
            check(payload.schema == schema)
            check(payload.content == """{"value":42}""")

            return SnapshotCodecResult.Success(
                TestOutput(42)
            )
        }
    }
}
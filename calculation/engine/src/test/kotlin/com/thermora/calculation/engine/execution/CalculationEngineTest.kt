package com.thermora.calculation.engine.execution

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame

class CalculationEngineTest {

    private data class TestInput(
        val value: Double
    ) : CalculationInput

    private data class AnotherInput(
        val value: Double
    ) : CalculationInput

    private data class TestOutput(
        val value: Double
    ) : CalculationOutput

    private data class AnotherOutput(
        val value: Double
    ) : CalculationOutput

    private class TestModel(
        override val descriptor: CalculationModelDescriptor
    ) : CalculationModel<TestInput, TestOutput> {

        override val inputType = TestInput::class
        override val outputType = TestOutput::class

        override fun calculate(
            input: TestInput
        ): CalculationResult<TestOutput> {
            return CalculationResult.Success(
                TestOutput(input.value * 2)
            )
        }
    }

    private class FailingModel(
        override val descriptor: CalculationModelDescriptor
    ) : CalculationModel<TestInput, TestOutput> {

        override val inputType = TestInput::class
        override val outputType = TestOutput::class

        override fun calculate(
            input: TestInput
        ): CalculationResult<TestOutput> {
            throw IllegalStateException("Test calculation failure")
        }
    }

    private class ThrowingModel(
        override val descriptor: CalculationModelDescriptor,
        private val exception: Throwable
    ) : CalculationModel<TestInput, TestOutput> {
        override val inputType = TestInput::class
        override val outputType = TestOutput::class

        override fun calculate(input: TestInput): CalculationResult<TestOutput> {
            throw exception
        }
    }

    private fun descriptor(
        id: String
    ) = CalculationModelDescriptor(
        id = CalculationModelId(id),
        name = "Test model",
        version = "1.0.0"
    )

    private class DiagnosticModel(
        override val descriptor: CalculationModelDescriptor
    ) : CalculationModel<TestInput, TestOutput> {

        override val inputType = TestInput::class
        override val outputType = TestOutput::class

        private val diagnostic = Diagnostic(
            code = "TEST_WARNING",
            message = "Test warning",
            severity = DiagnosticSeverity.WARNING,
            source = descriptor.id.value
        )

        override fun calculate(
            input: TestInput
        ): CalculationResult<TestOutput> {
            return CalculationResult.Success(
                value = TestOutput(input.value * 2),
                diagnostics = listOf(diagnostic)
            )
        }
    }

    private class BlockingModel(
        override val descriptor: CalculationModelDescriptor,
        private val started: CountDownLatch,
        private val release: CountDownLatch
    ) : CalculationModel<TestInput, TestOutput> {

        override val inputType = TestInput::class
        override val outputType = TestOutput::class

        override fun calculate(
            input: TestInput
        ): CalculationResult<TestOutput> {
            started.countDown()

            check(release.await(5, TimeUnit.SECONDS)) {
                "Calculation was not released in time"
            }

            return CalculationResult.Success(
                TestOutput(input.value * 2)
            )
        }
    }

    @Test
    fun `calculate should return successful result`() {
        val registry = CalculationModelRegistry()
        val model = TestModel(descriptor("test.model"))

        registry.register(model)

        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = model.descriptor.id,
            input = TestInput(10.0),
            expectedOutputType = TestOutput::class
        )

        val success = assertIs<CalculationResult.Success<TestOutput>>(result)

        assertEquals(20.0, success.value.value)
    }

    @Test
    fun `calculate should fail when model is not registered`() {
        val engine = CalculationEngine(CalculationModelRegistry())

        val result = engine.calculate(
            modelId = CalculationModelId("unknown.model"),
            input = TestInput(10.0),
            expectedOutputType = TestOutput::class
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertEquals(
            "CALCULATION_MODEL_NOT_FOUND",
            failure.diagnostics.single().code
        )
    }

    @Test
    fun `calculate should fail when input type does not match`() {
        val registry = CalculationModelRegistry()
        val model = TestModel(descriptor("test.model"))

        registry.register(model)

        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = model.descriptor.id,
            input = AnotherInput(10.0),
            expectedOutputType = TestOutput::class
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertEquals(
            "CALCULATION_INPUT_TYPE_MISMATCH",
            failure.diagnostics.single().code
        )
    }

    @Test
    fun `calculate should fail when output type does not match`() {
        val registry = CalculationModelRegistry()
        val model = TestModel(descriptor("test.model"))

        registry.register(model)

        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = model.descriptor.id,
            input = TestInput(10.0),
            expectedOutputType = AnotherOutput::class
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertEquals(
            "CALCULATION_OUTPUT_TYPE_MISMATCH",
            failure.diagnostics.single().code
        )
    }

    @Test
    fun `calculate should convert model exception into failure`() {
        val registry = CalculationModelRegistry()
        val model = FailingModel(descriptor("test.failing"))

        registry.register(model)

        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = model.descriptor.id,
            input = TestInput(10.0),
            expectedOutputType = TestOutput::class
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertEquals(
            "CALCULATION_EXECUTION_FAILED",
            failure.diagnostics.single().code
        )
        assertEquals("Test calculation failure", failure.diagnostics.single().message)
        assertEquals(DiagnosticSeverity.ERROR, failure.diagnostics.single().severity)
        assertEquals(model.descriptor.id.value, failure.diagnostics.single().source)
    }

    @Test
    fun `calculate should propagate original cancellation and cause`() {
        val cause = IllegalStateException("Cancellation reason")
        val exception = CancellationException("Calculation cancelled").apply {
            initCause(cause)
        }
        val registry = CalculationModelRegistry()
        val model = ThrowingModel(descriptor("test.cancelled"), exception)
        registry.register(model)
        val engine = CalculationEngine(registry)

        val thrown = assertFailsWith<CancellationException> {
            engine.calculate(model.descriptor.id, TestInput(10.0), TestOutput::class)
        }

        assertSame(exception, thrown)
        assertSame(cause, thrown.cause)
    }

    @Test
    fun `calculate should restore interrupt flag and propagate original interruption`() {
        try {
            assertFalse(Thread.currentThread().isInterrupted)
            val cause = IllegalStateException("Interruption reason")
            val exception = InterruptedException("Calculation interrupted").apply {
                initCause(cause)
            }
            val registry = CalculationModelRegistry()
            val model = ThrowingModel(descriptor("test.interrupted"), exception)
            registry.register(model)
            val engine = CalculationEngine(registry)

            val thrown = assertFailsWith<InterruptedException> {
                engine.calculate(model.descriptor.id, TestInput(10.0), TestOutput::class)
            }

            assertTrue(Thread.currentThread().isInterrupted)
            assertSame(exception, thrown)
            assertSame(cause, thrown.cause)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun `calculate should propagate JVM errors`() {
        val error = AssertionError("Model error")
        val registry = CalculationModelRegistry()
        val model = ThrowingModel(descriptor("test.error"), error)
        registry.register(model)
        val engine = CalculationEngine(registry)

        val thrown = assertFailsWith<AssertionError> {
            engine.calculate(model.descriptor.id, TestInput(10.0), TestOutput::class)
        }

        assertSame(error, thrown)
    }

    @Test
    fun `calculate should preserve model diagnostics`() {
        val registry = CalculationModelRegistry()
        val model = DiagnosticModel(descriptor("test.diagnostics"))

        registry.register(model)

        val engine = CalculationEngine(registry)

        val result = engine.calculate(
            modelId = model.descriptor.id,
            input = TestInput(10.0),
            expectedOutputType = TestOutput::class
        )

        val success = assertIs<CalculationResult.Success<TestOutput>>(result)

        assertEquals(20.0, success.value.value)

        assertEquals(1, success.diagnostics.size)
        assertEquals("TEST_WARNING", success.diagnostics.single().code)
        assertEquals(
            DiagnosticSeverity.WARNING,
            success.diagnostics.single().severity
        )
    }

    @Test
    fun `calculate should support concurrent execution`() {
        val registry = CalculationModelRegistry()
        val model = TestModel(descriptor("test.concurrent"))

        registry.register(model)

        val engine = CalculationEngine(registry)
        val executor = Executors.newFixedThreadPool(8)

        try {
            val futures = (1..100).map { value ->
                executor.submit<CalculationResult<TestOutput>> {
                    engine.calculate(
                        modelId = model.descriptor.id,
                        input = TestInput(value.toDouble()),
                        expectedOutputType = TestOutput::class
                    )
                }
            }

            futures.forEachIndexed { index, future ->
                val result = future.get()

                val success = assertIs<CalculationResult.Success<TestOutput>>(
                    result
                )

                assertEquals(
                    (index + 1) * 2.0,
                    success.value.value
                )
            }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `ongoing calculation should finish after model is unregistered`() {
        val registry = CalculationModelRegistry()

        val started = CountDownLatch(1)
        val release = CountDownLatch(1)

        val model = BlockingModel(
            descriptor = descriptor("test.blocking"),
            started = started,
            release = release
        )

        registry.register(model)

        val engine = CalculationEngine(registry)
        val executor = Executors.newSingleThreadExecutor()

        try {
            val future = executor.submit<CalculationResult<TestOutput>> {
                engine.calculate(
                    modelId = model.descriptor.id,
                    input = TestInput(10.0),
                    expectedOutputType = TestOutput::class
                )
            }

            assertTrue(
                started.await(5, TimeUnit.SECONDS),
                "Calculation did not start"
            )

            assertTrue(registry.unregister(model.descriptor.id))
            assertNull(registry.find(model.descriptor.id))

            release.countDown()

            val ongoingResult = future.get(5, TimeUnit.SECONDS)

            val success = assertIs<CalculationResult.Success<TestOutput>>(
                ongoingResult
            )

            assertEquals(20.0, success.value.value)

            val newResult = engine.calculate(
                modelId = model.descriptor.id,
                input = TestInput(10.0),
                expectedOutputType = TestOutput::class
            )

            val failure = assertIs<CalculationResult.Failure>(newResult)

            assertEquals(
                "CALCULATION_MODEL_NOT_FOUND",
                failure.diagnostics.single().code
            )
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }
}

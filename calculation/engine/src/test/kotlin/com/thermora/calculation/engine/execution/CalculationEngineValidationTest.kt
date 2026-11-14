package com.thermora.calculation.engine.execution

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import java.util.concurrent.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CalculationEngineValidationTest {

    private class Input : CalculationInput
    private class Output : CalculationOutput
    private class OtherInput : CalculationInput
    private class OtherOutput : CalculationOutput

    private class Model(
        val validation: (Input) -> ValidationReport = { ValidationReport.valid() },
        val calculation: (Input) -> CalculationResult<Output> = { CalculationResult.Success(Output()) }
    ) : CalculationModel<Input, Output> {
        override val descriptor = CalculationModelDescriptor(CalculationModelId("validation.test"), "Validation", "1")
        override val inputType = Input::class
        override val outputType = Output::class
        val calls = mutableListOf<String>()

        override fun validate(input: Input): ValidationReport {
            calls += "validate"
            return validation(input)
        }

        override fun calculate(input: Input): CalculationResult<Output> {
            calls += "calculate"
            return calculation(input)
        }
    }

    private fun engine(model: Model): CalculationEngine {
        val registry = CalculationModelRegistry()
        assertIs<CalculationResult.Success<Unit>>(registry.register(model))
        return CalculationEngine(registry)
    }

    private fun diagnostic(code: String, severity: DiagnosticSeverity) = Diagnostic(
        code, code, severity, "test", mapOf("detail" to code)
    )

    @Test
    fun `validation error prevents calculation and preserves entire report`() {
        val diagnostics = listOf(
            diagnostic("info", DiagnosticSeverity.INFO),
            diagnostic("error", DiagnosticSeverity.ERROR),
            diagnostic("warning", DiagnosticSeverity.WARNING)
        )
        val model = Model(validation = { ValidationReport(diagnostics) })

        val result = engine(model).calculate(model.descriptor.id, Input(), Output::class)

        assertEquals(diagnostics, assertIs<CalculationResult.Failure>(result).diagnostics)
        assertEquals(listOf("validate"), model.calls)
    }

    @Test
    fun `valid input is validated once before calculation with original input and output`() {
        val input = Input()
        val output = Output()
        val model = Model(
            validation = { received -> assertSame(input, received); ValidationReport.valid() },
            calculation = { received -> assertSame(input, received); CalculationResult.Success(output) }
        )

        val result = engine(model).calculate(model.descriptor.id, input, Output::class)

        assertSame(output, assertIs<CalculationResult.Success<Output>>(result).value)
        assertEquals(listOf("validate", "calculate"), model.calls)
    }

    @Test
    fun `warnings and information allow calculation and precede successful calculation diagnostics`() {
        val before = listOf(diagnostic("warning", DiagnosticSeverity.WARNING), diagnostic("info", DiagnosticSeverity.INFO))
        val after = listOf(diagnostic("calculation", DiagnosticSeverity.WARNING))
        val model = Model(
            validation = { ValidationReport(before) },
            calculation = { CalculationResult.Success(Output(), after) }
        )

        val result = engine(model).calculate(model.descriptor.id, Input(), Output::class)

        assertEquals(before + after, assertIs<CalculationResult.Success<Output>>(result).diagnostics)
        assertEquals(listOf("validate", "calculate"), model.calls)
    }

    @Test
    fun `validation diagnostics precede calculation failure diagnostics`() {
        val before = listOf(diagnostic("warning", DiagnosticSeverity.WARNING))
        val after = listOf(diagnostic("failure", DiagnosticSeverity.ERROR), diagnostic("details", DiagnosticSeverity.INFO))
        val model = Model(
            validation = { ValidationReport(before) },
            calculation = { CalculationResult.Failure(after) }
        )

        val result = engine(model).calculate(model.descriptor.id, Input(), Output::class)

        assertEquals(before + after, assertIs<CalculationResult.Failure>(result).diagnostics)
        assertEquals(listOf("validate", "calculate"), model.calls)
    }

    @Test
    fun `validation exception uses existing execution failure policy and prevents calculation`() {
        val model = Model(validation = { throw IllegalStateException("Validation failed") })

        val result = engine(model).calculate(model.descriptor.id, Input(), Output::class)

        assertEquals(
            listOf(Diagnostic("CALCULATION_EXECUTION_FAILED", "Validation failed", DiagnosticSeverity.ERROR, model.descriptor.id.value)),
            assertIs<CalculationResult.Failure>(result).diagnostics
        )
        assertEquals(listOf("validate"), model.calls)
    }

    @Test
    fun `calculation exception preserves validation diagnostics and existing execution failure`() {
        val before = diagnostic("warning", DiagnosticSeverity.WARNING)
        val model = Model(
            validation = { ValidationReport.of(before) },
            calculation = { throw IllegalStateException("Calculation failed") }
        )

        val result = engine(model).calculate(model.descriptor.id, Input(), Output::class)

        assertEquals(
            listOf(before, Diagnostic("CALCULATION_EXECUTION_FAILED", "Calculation failed", DiagnosticSeverity.ERROR, model.descriptor.id.value)),
            assertIs<CalculationResult.Failure>(result).diagnostics
        )
        assertEquals(listOf("validate", "calculate"), model.calls)
    }

    @Test
    fun `validation cancellation propagates original exception and cause`() {
        val cause = IllegalStateException("reason")
        val exception = CancellationException("Cancelled").apply { initCause(cause) }
        val model = Model(validation = { throw exception })
        val engine = engine(model)

        val thrown = assertFailsWith<CancellationException> {
            engine.calculate(model.descriptor.id, Input(), Output::class)
        }

        assertSame(exception, thrown)
        assertSame(cause, thrown.cause)
        assertEquals(listOf("validate"), model.calls)
    }

    @Test
    fun `validation interruption restores flag and propagates original exception`() {
        try {
            assertFalse(Thread.currentThread().isInterrupted)
            val cause = IllegalStateException("reason")
            val exception = InterruptedException("Interrupted").apply { initCause(cause) }
            val model = Model(validation = { throw exception })
            val engine = engine(model)

            val thrown = assertFailsWith<InterruptedException> {
                engine.calculate(model.descriptor.id, Input(), Output::class)
            }

            assertTrue(Thread.currentThread().isInterrupted)
            assertSame(exception, thrown)
            assertSame(cause, thrown.cause)
            assertEquals(listOf("validate"), model.calls)
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun `validation JVM error is not converted to failure`() {
        val error = AssertionError("Validation error")
        val model = Model(validation = { throw error })
        val engine = engine(model)

        assertSame(error, assertFailsWith<AssertionError> {
            engine.calculate(model.descriptor.id, Input(), Output::class)
        })
        assertEquals(listOf("validate"), model.calls)
    }

    @Test
    fun `type mismatches prevent validation and calculation`() {
        val model = Model()
        val engine = engine(model)

        val inputFailure = assertIs<CalculationResult.Failure>(engine.calculate(model.descriptor.id, OtherInput(), Output::class))
        val outputFailure = assertIs<CalculationResult.Failure>(engine.calculate(model.descriptor.id, Input(), OtherOutput::class))

        assertEquals("CALCULATION_INPUT_TYPE_MISMATCH", inputFailure.diagnostics.single().code)
        assertEquals("CALCULATION_OUTPUT_TYPE_MISMATCH", outputFailure.diagnostics.single().code)
        assertTrue(model.calls.isEmpty())
    }

    @Test
    fun `invalid runtime output preserves validation and calculation diagnostics`() {
        val before = diagnostic("warning", DiagnosticSeverity.WARNING)
        val after = diagnostic("info", DiagnosticSeverity.INFO)
        val model = Model(
            validation = { ValidationReport.of(before) },
            calculation = {
                @Suppress("UNCHECKED_CAST")
                (CalculationResult.Success(OtherOutput(), listOf(after)) as CalculationResult<Output>)
            }
        )

        val result = assertIs<CalculationResult.Failure>(engine(model).calculate(model.descriptor.id, Input(), Output::class))

        assertEquals(listOf(before, after), result.diagnostics.take(2))
        assertEquals("CALCULATION_INVALID_OUTPUT", result.diagnostics.last().code)
        assertEquals(listOf("validate", "calculate"), model.calls)
    }
    @Test
    fun `model mutations cannot erase validation diagnostics or nested details`() {
        val details = mutableMapOf("key" to "value")
        val warning = Diagnostic("warning", "warning", DiagnosticSeverity.WARNING, details = details)
        val diagnostics = mutableListOf(warning)
        val model = Model(
            validation = { ValidationReport(diagnostics) },
            calculation = {
                diagnostics.clear()
                details.clear()
                CalculationResult.Success(Output())
            }
        )

        val result = engine(model).calculate(model.descriptor.id, Input(), Output::class)
        val success = assertIs<CalculationResult.Success<Output>>(result)

        assertEquals(listOf(Diagnostic("warning", "warning", DiagnosticSeverity.WARNING,
            details = mapOf("key" to "value"))), success.diagnostics)
        assertEquals(listOf("validate", "calculate"), model.calls)
    }

}

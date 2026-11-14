package com.thermora.calculation.validation.validator

import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.calculation.api.validation.ValidationReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositeValidatorTest {

    @Test
    fun `empty validator returns valid report`() {
        val validator = CompositeValidator<Int>(emptyList())

        val report = validator.validate(10)

        assertTrue(report.isValid)
        assertTrue(report.diagnostics.isEmpty())
    }

    @Test
    fun `validator combines diagnostics from multiple rules`() {
        val warningRule = ValidationRule<Int> {
            ValidationReport.of(
                Diagnostic(
                    code = "WARNING",
                    message = "Warning",
                    severity = DiagnosticSeverity.WARNING
                )
            )
        }

        val errorRule = ValidationRule<Int> {
            ValidationReport.of(
                Diagnostic(
                    code = "ERROR",
                    message = "Error",
                    severity = DiagnosticSeverity.ERROR
                )
            )
        }

        val validator = CompositeValidator(
            listOf(warningRule, errorRule)
        )

        val report = validator.validate(10)

        assertEquals(2, report.diagnostics.size)
        assertEquals(
            listOf("WARNING", "ERROR"),
            report.diagnostics.map { it.code }
        )
        assertFalse(report.isValid)
        assertTrue(report.hasWarnings)
    }

    @Test
    fun `validator passes input to every rule`() {
        val received = mutableListOf<Int>()

        val first = ValidationRule<Int> { input ->
            received.add(input)
            ValidationReport.valid()
        }

        val second = ValidationRule<Int> { input ->
            received.add(input)
            ValidationReport.valid()
        }

        val validator = CompositeValidator(listOf(first, second))

        validator.validate(42)

        assertEquals(listOf(42, 42), received)
    }

    @Test
    fun `validator keeps snapshot of rules`() {
        val rules = mutableListOf<ValidationRule<Int>>(
            ValidationRule { ValidationReport.valid() }
        )

        val validator = CompositeValidator(rules)

        rules.add(
            ValidationRule {
                ValidationReport.of(
                    Diagnostic(
                        code = "LATE_RULE",
                        message = "Added after construction",
                        severity = DiagnosticSeverity.ERROR
                    )
                )
            }
        )

        val report = validator.validate(10)

        assertTrue(report.isValid)
        assertTrue(report.diagnostics.isEmpty())
    }
}


package com.thermora.calculation.api.validation

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationReportTest {

    @Test
    fun `empty report is valid`() {
        val report = ValidationReport.valid()

        assertTrue(report.isValid)
        assertFalse(report.hasWarnings)
        assertTrue(report.diagnostics.isEmpty())
    }

    @Test
    fun `warning does not invalidate report`() {
        val report = ValidationReport.of(
            Diagnostic(
                code = "TEST_WARNING",
                message = "Parameter is outside recommended range",
                severity = DiagnosticSeverity.WARNING
            )
        )

        assertTrue(report.isValid)
        assertTrue(report.hasWarnings)
    }

    @Test
    fun `error invalidates report`() {
        val report = ValidationReport.of(
            Diagnostic(
                code = "TEST_ERROR",
                message = "Parameter is invalid",
                severity = DiagnosticSeverity.ERROR
            )
        )

        assertFalse(report.isValid)
    }

    @Test
    fun `merge combines diagnostics`() {
        val first = ValidationReport.of(
            Diagnostic(
                code = "WARNING",
                message = "Warning",
                severity = DiagnosticSeverity.WARNING
            )
        )

        val second = ValidationReport.of(
            Diagnostic(
                code = "ERROR",
                message = "Error",
                severity = DiagnosticSeverity.ERROR
            )
        )

        val merged = first.merge(second)

        assertEquals(2, merged.diagnostics.size)
        assertFalse(merged.isValid)
        assertTrue(merged.hasWarnings)
    }
}


package com.thermora.calculation.api.validation

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity

data class ValidationReport(
    val diagnostics: List<Diagnostic> = emptyList()
) {
    val isValid: Boolean
        get() = diagnostics.none {
            it.severity == DiagnosticSeverity.ERROR
        }

    val hasWarnings: Boolean
        get() = diagnostics.any {
            it.severity == DiagnosticSeverity.WARNING
        }

    fun merge(other: ValidationReport): ValidationReport =
        ValidationReport(diagnostics + other.diagnostics)

    companion object {
        fun valid(): ValidationReport = ValidationReport()

        fun of(vararg diagnostics: Diagnostic): ValidationReport =
            ValidationReport(diagnostics.toList())
    }
}

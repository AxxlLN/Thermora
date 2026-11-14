
package com.thermora.calculation.api.validation

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.util.ArrayList
import java.util.Collections

class ValidationReport(
    diagnostics: List<Diagnostic> = emptyList()
) {
    val diagnostics: List<Diagnostic> = Collections.unmodifiableList(ArrayList(diagnostics))

    operator fun component1(): List<Diagnostic> = diagnostics

    fun copy(diagnostics: List<Diagnostic> = this.diagnostics): ValidationReport = ValidationReport(diagnostics)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValidationReport) return false
        return diagnostics == other.diagnostics
    }

    override fun hashCode(): Int = diagnostics.hashCode()

    override fun toString(): String = "ValidationReport(diagnostics=$diagnostics)"

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

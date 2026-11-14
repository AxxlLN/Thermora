package com.thermora.calculation.validation.rules

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity

class FiniteNumberRule<I : Any>(
    private val field: String,
    private val selector: (I) -> Double
) : ValidationRule<I> {

    override fun validate(input: I): ValidationReport {
        val value = selector(input)

        if (value.isFinite()) {
            return ValidationReport.valid()
        }

        return ValidationReport.of(
            Diagnostic(
                code = "NON_FINITE_NUMBER",
                message = "$field must be a finite number",
                severity = DiagnosticSeverity.ERROR,
                source = field,
                details = mapOf("value" to value.toString())
            )
        )
    }
}

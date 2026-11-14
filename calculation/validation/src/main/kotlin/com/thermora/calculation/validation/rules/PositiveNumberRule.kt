package com.thermora.calculation.validation.rules

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity

class PositiveNumberRule<I : Any>(
    private val field: String,
    private val selector: (I) -> Double
) : ValidationRule<I> {

    override fun validate(input: I): ValidationReport {
        val value = selector(input)

        if (value.isFinite() && value > 0.0) {
            return ValidationReport.valid()
        }

        return ValidationReport.of(
            Diagnostic(
                code = "NOT_POSITIVE_NUMBER",
                message = "$field must be a finite positive number",
                severity = DiagnosticSeverity.ERROR,
                source = field,
                details = mapOf("value" to value.toString())
            )
        )
    }
}

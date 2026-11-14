package com.thermora.calculation.validation.rules

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity

class RangeRule<I : Any>(
    private val field: String,
    private val min: Double,
    private val max: Double,
    private val selector: (I) -> Double
) : ValidationRule<I> {

    init {
        require(min.isFinite() && max.isFinite()) {
            "Range boundaries must be finite"
        }
        require(min <= max) {
            "Minimum boundary must not exceed maximum boundary"
        }
    }

    override fun validate(input: I): ValidationReport {
        val value = selector(input)

        if (value.isFinite() && value >= min && value <= max) {
            return ValidationReport.valid()
        }

        return ValidationReport.of(
            Diagnostic(
                code = "OUT_OF_RANGE",
                message = "$field must be between $min and $max",
                severity = DiagnosticSeverity.ERROR,
                source = field,
                details = mapOf(
                    "value" to value.toString(),
                    "min" to min.toString(),
                    "max" to max.toString()
                )
            )
        )
    }
}

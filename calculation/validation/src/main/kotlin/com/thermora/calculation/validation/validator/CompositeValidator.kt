package com.thermora.calculation.validation.validator

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule

class CompositeValidator<I : Any>(
    rules: List<ValidationRule<I>>
) : ValidationRule<I> {

    private val rules = rules.toList()

    override fun validate(input: I): ValidationReport {
        val diagnostics = rules.flatMap { rule ->
            rule.validate(input).diagnostics
        }

        return ValidationReport(diagnostics)
    }
}

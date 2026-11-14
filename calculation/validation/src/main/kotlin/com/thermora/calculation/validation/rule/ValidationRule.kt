package com.thermora.calculation.validation.rule

import com.thermora.calculation.api.validation.ValidationReport

fun interface ValidationRule<I : Any> {

    fun validate(input: I): ValidationReport
}

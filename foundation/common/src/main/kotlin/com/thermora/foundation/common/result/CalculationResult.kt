package com.thermora.foundation.common.result

import com.thermora.foundation.common.diagnostic.Diagnostic

sealed interface CalculationResult<out T> {

    data class Success<T>(
        val value: T,
        val diagnostics: List<Diagnostic> = emptyList()
    ) : CalculationResult<T>

    data class Failure(
        val diagnostics: List<Diagnostic>
    ) : CalculationResult<Nothing>
}
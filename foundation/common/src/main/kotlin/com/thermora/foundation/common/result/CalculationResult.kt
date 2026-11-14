package com.thermora.foundation.common.result

import com.thermora.foundation.common.diagnostic.Diagnostic
import java.util.ArrayList
import java.util.Collections

sealed interface CalculationResult<out T> {

    class Success<T>(
        val value: T,
        diagnostics: List<Diagnostic> = emptyList()
    ) : CalculationResult<T> {
        val diagnostics: List<Diagnostic> = Collections.unmodifiableList(ArrayList(diagnostics))

        operator fun component1(): T = value
        operator fun component2(): List<Diagnostic> = diagnostics

        fun copy(
            value: T = this.value,
            diagnostics: List<Diagnostic> = this.diagnostics
        ): Success<T> = Success(value, diagnostics)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success<*>) return false
            return value == other.value && diagnostics == other.diagnostics
        }

        override fun hashCode(): Int = 31 * (value?.hashCode() ?: 0) + diagnostics.hashCode()

        override fun toString(): String = "Success(value=$value, diagnostics=$diagnostics)"
    }

    class Failure(
        diagnostics: List<Diagnostic>
    ) : CalculationResult<Nothing> {
        val diagnostics: List<Diagnostic> = Collections.unmodifiableList(ArrayList(diagnostics))

        operator fun component1(): List<Diagnostic> = diagnostics

        fun copy(diagnostics: List<Diagnostic> = this.diagnostics): Failure = Failure(diagnostics)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Failure) return false
            return diagnostics == other.diagnostics
        }

        override fun hashCode(): Int = diagnostics.hashCode()

        override fun toString(): String = "Failure(diagnostics=$diagnostics)"
    }
}

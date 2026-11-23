package com.thermora.infrastructure.serialization.json

/**
 * Strict conversion of JSON numeric tokens to JVM Double.
 */
internal object StrictJsonNumber {

    const val MAX_NUMBER_LENGTH = 64

    fun parseDouble(raw: String): Double {
        if (raw.length > MAX_NUMBER_LENGTH) {
            throw JsonTokenReadFailure(JsonFailureKind.PAYLOAD, "JSON number exceeds maximum length")
        }

        if (!JSON_NUMBER.matches(raw)) {
            throw JsonTokenReadFailure(JsonFailureKind.PAYLOAD, "Invalid JSON number")
        }

        val result = raw.toDoubleOrNull()
            ?: throw JsonTokenReadFailure(JsonFailureKind.VALUE, "Invalid JSON double")

        if (!result.isFinite()) {
            throw JsonTokenReadFailure(JsonFailureKind.VALUE, "JSON number exceeds Double range")
        }

        if (result == 0.0 && raw.takeWhile { it != 'e' && it != 'E' }.any { it in '1'..'9' }) {
            throw JsonTokenReadFailure(JsonFailureKind.VALUE, "Non-zero JSON number underflows to zero")
        }

        return result
    }

    private val JSON_NUMBER = Regex(
        """-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"""
    )
}

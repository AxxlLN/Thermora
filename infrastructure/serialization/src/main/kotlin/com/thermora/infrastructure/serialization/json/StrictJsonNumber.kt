package com.thermora.infrastructure.serialization.json

/**
 * Strict conversion of JSON numeric tokens to JVM Double.
 */
internal object StrictJsonNumber {

    const val MAX_NUMBER_LENGTH = 64

    fun parseDouble(raw: String): Double {
        require(raw.length <= MAX_NUMBER_LENGTH) {
            "JSON number exceeds maximum length"
        }

        require(JSON_NUMBER.matches(raw)) {
            "Invalid JSON number"
        }

        val result = raw.toDoubleOrNull()
            ?: throw IllegalArgumentException(
                "Invalid JSON double"
            )

        require(result.isFinite()) {
            "JSON number exceeds Double range"
        }

        require(
            result != 0.0 || raw.takeWhile { it != 'e' && it != 'E' }.none { it in '1'..'9' }
        ) {
            "Non-zero JSON number underflows to zero"
        }

        return result
    }

    private val JSON_NUMBER = Regex(
        """-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"""
    )
}

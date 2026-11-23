package com.thermora.infrastructure.serialization.json

/**
 * Validates Unicode scalar values in JVM UTF-16 strings.
 */
internal object JsonUnicodeValidator {

    fun validate(value: String) {
        var index = 0

        while (index < value.length) {
            val char = value[index]

            when {
                Character.isHighSurrogate(char) -> {
                    require(index + 1 < value.length) {
                        "Unpaired high surrogate"
                    }

                    require(Character.isLowSurrogate(value[index + 1])) {
                        "Unpaired high surrogate"
                    }

                    index += 2
                }

                Character.isLowSurrogate(char) -> {
                    throw IllegalArgumentException(
                        "Unpaired low surrogate"
                    )
                }

                else -> index++
            }
        }
    }

    fun validateCodePointLength(
        value: String,
        maxCodePoints: Int
    ) {
        require(maxCodePoints >= 0) {
            "Maximum code point length must not be negative"
        }

        validate(value)

        val count = value.codePointCount(0, value.length)

        require(count <= maxCodePoints) {
            "String exceeds maximum Unicode code point length"
        }
    }
}

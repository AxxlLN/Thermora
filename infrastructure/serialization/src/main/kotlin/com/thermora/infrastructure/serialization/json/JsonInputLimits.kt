package com.thermora.infrastructure.serialization.json

internal object JsonInputLimits {

    const val MAX_JSON_BYTES = 1_048_576

    /**
     * Validates UTF-8 payload size without allocating a byte array.
     *
     * Rejects unpaired UTF-16 surrogates.
     */
    fun validate(json: String) {
        require(json.length <= MAX_JSON_BYTES) {
            "JSON input exceeds the maximum UTF-16 length"
        }

        var byteCount = 0
        var index = 0

        while (index < json.length) {
            val char = json[index]

            val bytes = when {
                char.code <= 0x7F -> 1

                char.code <= 0x7FF -> 2

                Character.isHighSurrogate(char) -> {
                    require(index + 1 < json.length) {
                        "Unpaired high surrogate in JSON input"
                    }

                    require(Character.isLowSurrogate(json[index + 1])) {
                        "Unpaired high surrogate in JSON input"
                    }

                    index++
                    4
                }

                Character.isLowSurrogate(char) -> {
                    throw IllegalArgumentException(
                        "Unpaired low surrogate in JSON input"
                    )
                }

                else -> 3
            }

            byteCount += bytes

            require(byteCount <= MAX_JSON_BYTES) {
                "JSON input exceeds the maximum UTF-8 size"
            }

            index++
        }
    }

    /**
     * Checks byte length before UTF-8 decoding.
     *
     * UTF-8 validity must be checked separately using
     * a decoder configured with CodingErrorAction.REPORT.
     */
    fun validate(bytes: ByteArray) {
        require(bytes.size <= MAX_JSON_BYTES) {
            "JSON input exceeds the maximum UTF-8 size"
        }
    }
}

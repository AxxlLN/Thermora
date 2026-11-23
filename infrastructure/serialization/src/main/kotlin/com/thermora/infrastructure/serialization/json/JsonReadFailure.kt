package com.thermora.infrastructure.serialization.json

/**
 * Classification of expected JSON decoding failures.
 *
 * PAYLOAD:
 *   Invalid JSON structure, missing fields, unexpected types,
 *   unknown fields, or resource limit violations.
 *
 * VALUE:
 *   Structurally valid JSON containing an invalid domain value.
 */
internal enum class JsonFailureKind {
    PAYLOAD,
    VALUE
}

internal fun jsonFieldPath(parent: String, name: String): String {
    fun identifierStart(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    if (name.isNotEmpty() && identifierStart(name[0]) && name.all { identifierStart(it) || it in '0'..'9' }) {
        return "$parent.$name"
    }
    val escaped = buildString {
        for (character in name) {
            when {
                character == '\\' -> append("\\\\")
                character == '\'' -> append("\\'")
                character.code < 0x20 || character.isSurrogate() -> append("\\u" + character.code.toString(16).padStart(4, '0'))
                else -> append(character)
            }
        }
    }
    return "$parent['$escaped']"
}

/**
 * Represents a controlled JSON decoding failure.
 *
 * The path points to the location of the invalid value.
 *
 * Examples:
 *   $
 *   $.ambient.temperature.kelvin
 *   $.heatTransfer.wallGaps[2].gap
 */
internal class JsonReadFailure(
    val kind: JsonFailureKind,
    val path: String,
    detail: String,
    cause: Throwable? = null
) : IllegalArgumentException(
    "$path: $detail",
    cause
) {

    init {
        require(path.startsWith("$")) {
            "JSON path must start with '$'"
        }

        require(detail.isNotBlank()) {
            "JSON failure detail must not be blank"
        }
    }

    companion object {

        fun payload(
            path: String,
            detail: String,
            cause: Throwable? = null
        ): JsonReadFailure =
            JsonReadFailure(
                kind = JsonFailureKind.PAYLOAD,
                path = path,
                detail = detail,
                cause = cause
            )

        fun value(
            path: String,
            detail: String,
            cause: Throwable? = null
        ): JsonReadFailure =
            JsonReadFailure(
                kind = JsonFailureKind.VALUE,
                path = path,
                detail = detail,
                cause = cause
            )
    }
}

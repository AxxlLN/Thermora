package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.exc.StreamConstraintsException
import com.fasterxml.jackson.core.exc.StreamReadException
import java.util.ArrayDeque

/**
 * Internal strict JSON reader used by Thermora codecs.
 *
 * Does not perform domain-specific validation.
 */
internal class StrictJsonReader private constructor(
    private val parser: JsonParser
) : AutoCloseable {

    private val arrayLimits = JsonArrayLimits()
    private val containers = ArrayDeque<Any>()
    private val valueBoundaries = ArrayDeque<ValueBoundary>()

    private class ValueBoundary(val container: Any?, val path: String, var completed: Boolean)

    private var rootStarted = false
    private var rootCompleted = false
    private var failed = false
    private var closed = false
    private var endOfInput = false

    companion object {

        fun <T> read(
            json: String,
            block: (StrictJsonReader) -> T
        ): T {
            try {
                payloadValidation { JsonInputLimits.validate(json) }
            } catch (e: JsonTokenReadFailure) {
                throw JsonReadFailure(e.kind, "$", e.message ?: "Invalid JSON input", e)
            }

            return StrictJsonFactory.instance
                .createParser(json)
                .use { parser ->

                    StrictJsonReader(parser).use { reader ->
                        try {
                            val result = block(reader)
                            reader.checkUsable()

                            reader.readToken {
                                requirePayload(reader.rootStarted, "JSON root value was not consumed")
                                requirePayload(reader.rootCompleted, "JSON root value was not fully consumed")
                                requirePayload(reader.nextToken() == null, "Unexpected content after JSON root value")
                            }

                            result
                        } catch (e: JsonTokenReadFailure) {
                            throw JsonReadFailure(e.kind, reader.currentPath("$"), e.message ?: "Invalid JSON token", e)
                        }
                    }
                }
        }
    }

    fun nextToken(): JsonToken? = readToken {
        valueBoundaries.firstOrNull { it.completed }?.let {
            throw JsonReadFailure.payload(it.path, "Field handler advanced beyond its JSON value")
        }
        if (endOfInput) return@readToken null

        val token = parser.nextToken()
        if (token == null) {
            endOfInput = true
            return@readToken null
        }

        requirePayload(!rootCompleted, "Unexpected content after JSON root value")

        payloadValidation { arrayLimits.onToken(token) }
        when (token) {
            JsonToken.FIELD_NAME -> payloadValidation { JsonUnicodeValidator.validate(parser.currentName()) }
            JsonToken.VALUE_STRING -> payloadValidation { JsonUnicodeValidator.validate(parser.text) }
            JsonToken.VALUE_NUMBER_INT,
            JsonToken.VALUE_NUMBER_FLOAT -> StrictJsonNumber.parseDouble(parser.text)
            else -> Unit
        }
        when (token) {
            JsonToken.START_OBJECT, JsonToken.START_ARRAY -> containers.addLast(Any())
            JsonToken.END_OBJECT, JsonToken.END_ARRAY -> {
                val completedContainer = containers.removeLast()
                valueBoundaries.forEach {
                    if (it.container === completedContainer) it.completed = true
                }
            }
            else -> Unit
        }
        rootStarted = true

        if (parser.parsingContext.inRoot()) {
            rootCompleted = true
        }

        token
    }

    fun currentToken(): JsonToken? {
        checkUsable()
        return parser.currentToken()
    }

    fun expect(token: JsonToken) = readToken {
        requirePayload(parser.currentToken() == token, "Expected $token, got ${parser.currentToken()}")
    }


    fun readFieldName(): String = readToken {
        expect(JsonToken.FIELD_NAME)

        val value = parser.currentName()
        payloadValidation { JsonUnicodeValidator.validate(value) }

        value
    }

    fun readString(): String = readToken {
        expect(JsonToken.VALUE_STRING)

        val value = parser.text
        payloadValidation { JsonUnicodeValidator.validate(value) }

        value
    }

    fun readBoolean(): Boolean = readToken {
        when (parser.currentToken()) {
            JsonToken.VALUE_TRUE -> true
            JsonToken.VALUE_FALSE -> false
            else -> throw JsonTokenReadFailure(JsonFailureKind.PAYLOAD, "Expected JSON boolean")
        }
    }

    fun readInt(): Int = readToken {
        expect(JsonToken.VALUE_NUMBER_INT)

        val raw = parser.text

        requirePayload(raw.length <= StrictJsonNumber.MAX_NUMBER_LENGTH, "Integer token exceeds maximum length")

        raw.toIntOrNull()
            ?: throw JsonTokenReadFailure(JsonFailureKind.VALUE, "Out-of-range JSON integer")
    }

    fun readDouble(): Double = readToken {
        requirePayload(
            parser.currentToken() == JsonToken.VALUE_NUMBER_INT ||
                    parser.currentToken() == JsonToken.VALUE_NUMBER_FLOAT,
            "Expected JSON number"
        )

        StrictJsonNumber.parseDouble(parser.text)
    }

    fun readNull() {
        expect(JsonToken.VALUE_NULL)
    }

    override fun close() {
        closed = true
        parser.close()
    }

    fun <T> withReadFailures(path: String, action: () -> T): T {
        checkUsable()
        try {
            return action()
        } catch (e: JsonReadFailure) {
            failed = true
            throw e
        } catch (e: JsonTokenReadFailure) {
            failed = true
            throw JsonReadFailure(e.kind, currentPath(path), e.message ?: "Invalid JSON token", e)
        } catch (e: StreamReadException) {
            failed = true
            throw JsonReadFailure.payload(currentPath(path), "Invalid JSON syntax", e)
        } catch (e: StreamConstraintsException) {
            failed = true
            throw JsonReadFailure.payload(currentPath(path), "JSON resource limit exceeded", e)
        }
    }

    fun consumeValue(path: String, action: () -> Unit) = readToken {
        val token = parser.currentToken()
            ?: throw JsonTokenReadFailure(JsonFailureKind.PAYLOAD, "Expected JSON value")
        requirePayload(token.isStructStart || token.isScalarValue, "Expected JSON value")
        val boundary = ValueBoundary(if (token.isStructStart) containers.peekLast() else null, path, !token.isStructStart)
        valueBoundaries.addLast(boundary)
        try {
            action()
            checkUsable()
            if (!boundary.completed) {
                throw JsonReadFailure.payload(path, "Field handler did not fully consume its JSON value")
            }
        } finally {
            valueBoundaries.removeLast()
        }
    }

    private fun currentPath(fallback: String): String {
        val contexts = generateSequence(parser.parsingContext) { it.parent }.toList().asReversed()
        var path = "$"
        for (context in contexts) {
            if (context.inObject() && context.currentName != null) {
                path = jsonFieldPath(path, context.currentName)
            } else if (context.inArray() && context.hasCurrentIndex()) {
                path += "[${context.currentIndex}]"
            }
        }
        return if (path == "$") fallback else path
    }

    private inline fun <T> readToken(action: () -> T): T {
        checkUsable()
        try {
            return action()
        } catch (e: Exception) {
            failed = true
            throw e
        }
    }

    private fun checkUsable() {
        check(!closed) { "JSON reader is closed" }
        check(!failed) { "JSON reader cannot be used after a parsing or validation failure" }
    }
}

private fun requirePayload(condition: Boolean, detail: String) {
    if (!condition) throw JsonTokenReadFailure(JsonFailureKind.PAYLOAD, detail)
}

private inline fun <T> payloadValidation(action: () -> T): T = try {
    action()
} catch (e: IllegalArgumentException) {
    throw JsonTokenReadFailure(JsonFailureKind.PAYLOAD, e.message ?: "Invalid JSON payload", e)
}

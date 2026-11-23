package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

/**
 * Internal strict JSON reader used by Thermora codecs.
 *
 * Does not perform domain-specific validation.
 */
internal class StrictJsonReader private constructor(
    private val parser: JsonParser
) : AutoCloseable {

    private val arrayLimits = JsonArrayLimits()

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
            JsonInputLimits.validate(json)

            return StrictJsonFactory.instance
                .createParser(json)
                .use { parser ->

                    StrictJsonReader(parser).use { reader ->
                        val result = block(reader)
                        reader.checkUsable()

                        require(reader.rootStarted) {
                            "JSON root value was not consumed"
                        }

                        require(reader.rootCompleted) {
                            "JSON root value was not fully consumed"
                        }

                        require(reader.nextToken() == null) {
                            "Unexpected content after JSON root value"
                        }

                        result
                    }
                }
        }
    }

    fun nextToken(): JsonToken? {
        checkUsable()
        if (endOfInput) return null

        try {
            val token = parser.nextToken()
            if (token == null) {
                endOfInput = true
                return null
            }

            require(!rootCompleted) {
                "Unexpected content after JSON root value"
            }

            arrayLimits.onToken(token)
            when (token) {
                JsonToken.FIELD_NAME -> JsonUnicodeValidator.validate(parser.currentName())
                JsonToken.VALUE_STRING -> JsonUnicodeValidator.validate(parser.text)
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.VALUE_NUMBER_FLOAT -> StrictJsonNumber.parseDouble(parser.text)
                else -> Unit
            }
            rootStarted = true

            if (parser.parsingContext.inRoot()) {
                rootCompleted = true
            }

            return token
        } catch (e: Exception) {
            failed = true
            throw e
        }
    }

    fun expect(token: JsonToken) {
        checkUsable()
        require(parser.currentToken() == token) {
            "Expected $token, got ${parser.currentToken()}"
        }
    }


    fun readFieldName(): String {
        expect(JsonToken.FIELD_NAME)

        val value = parser.currentName()
        JsonUnicodeValidator.validate(value)

        return value
    }

    fun readString(): String {
        expect(JsonToken.VALUE_STRING)

        val value = parser.text
        JsonUnicodeValidator.validate(value)

        return value
    }

    fun readBoolean(): Boolean {
        checkUsable()
        return when (parser.currentToken()) {
            JsonToken.VALUE_TRUE -> true
            JsonToken.VALUE_FALSE -> false
            else -> throw IllegalArgumentException(
                "Expected JSON boolean"
            )
        }
    }

    fun readInt(): Int {
        expect(JsonToken.VALUE_NUMBER_INT)

        val raw = parser.text

        require(raw.length <= StrictJsonNumber.MAX_NUMBER_LENGTH) {
            "Integer token exceeds maximum length"
        }

        return raw.toIntOrNull()
            ?: throw IllegalArgumentException(
                "Invalid or out-of-range JSON integer"
            )
    }

    fun readDouble(): Double {
        checkUsable()
        require(
            parser.currentToken() == JsonToken.VALUE_NUMBER_INT ||
                    parser.currentToken() == JsonToken.VALUE_NUMBER_FLOAT
        ) {
            "Expected JSON number"
        }

        return StrictJsonNumber.parseDouble(parser.text)
    }

    fun readNull() {
        expect(JsonToken.VALUE_NULL)
    }

    override fun close() {
        closed = true
        parser.close()
    }

    private fun checkUsable() {
        check(!closed) { "JSON reader is closed" }
        check(!failed) { "JSON reader cannot be used after a parsing or validation failure" }
    }
}

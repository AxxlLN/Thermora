package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import java.io.Writer
import java.util.ArrayDeque

/**
 * Internal JSON writer for Thermora serialization.
 *
 * Uses Jackson Streaming API without reflection or DTO mapping.
 */
internal class StrictJsonWriter private constructor(
    private val generator: JsonGenerator
) {
    private val arrayLimits = JsonArrayLimits { throw JsonWriteLimitFailure("JSON array exceeds maximum element count") }
    private val pendingFields = ArrayDeque<Boolean>()
    private var failed = false
    private var finished = false

    companion object {

        fun write(
            block: (StrictJsonWriter) -> Unit
        ): String {
            val output = BoundedJsonOutput()

            StrictJsonFactory.instance
                .createGenerator(output)
                .use { generator ->
                    val writer = StrictJsonWriter(generator)
                    block(writer)
                    writer.finish()
                }

            val json = output.content()
            JsonInputLimits.validate(json)

            return json
        }
    }

    fun startObject() = writeValue(JsonToken.START_OBJECT) {
        generator.writeStartObject()
        pendingFields.addLast(false)
    }

    fun endObject() = write {
        require(pendingFields.peekLast() != true) { "JSON field has no value" }
        generator.writeEndObject()
        pendingFields.removeLast()
        arrayLimits.onToken(JsonToken.END_OBJECT)
    }

    fun startArray() = writeValue(JsonToken.START_ARRAY) {
        generator.writeStartArray()
    }

    fun endArray() = write {
        generator.writeEndArray()
        arrayLimits.onToken(JsonToken.END_ARRAY)
    }

    fun fieldName(name: String) = write {
        JsonUnicodeValidator.validate(name)
        requireJsonWriteLimit(name.length <= StrictJsonFactory.MAX_NAME_LENGTH, "JSON field name exceeds maximum length")
        generator.writeFieldName(name)
        pendingFields.removeLast()
        pendingFields.addLast(true)
    }

    fun string(value: String) = writeValue(JsonToken.VALUE_STRING) {
        JsonUnicodeValidator.validate(value)
        requireJsonWriteLimit(value.length <= StrictJsonFactory.MAX_STRING_LENGTH, "JSON string exceeds maximum length")
        generator.writeString(value)
    }


    fun boolean(value: Boolean) = writeValue(if (value) JsonToken.VALUE_TRUE else JsonToken.VALUE_FALSE) {
        generator.writeBoolean(value)
    }

    fun int(value: Int) = writeValue(JsonToken.VALUE_NUMBER_INT) {
        generator.writeNumber(value)
    }

    fun double(value: Double) = writeValue(JsonToken.VALUE_NUMBER_FLOAT) {
        require(value.isFinite()) {
            "Non-finite Double values are not allowed in JSON"
        }

        generator.writeNumber(value)
    }

    fun nullValue() = writeValue(JsonToken.VALUE_NULL) {
        generator.writeNull()
    }

    private inline fun writeValue(token: JsonToken, action: () -> Unit) = write {
        require(!generator.outputContext.inRoot() || generator.outputContext.entryCount == 0) {
            "Only one JSON root value is allowed"
        }
        arrayLimits.onToken(token)
        if (generator.outputContext.inObject()) {
            pendingFields.removeLast()
            pendingFields.addLast(false)
        }
        action()
    }

    private inline fun write(action: () -> Unit) {
        check(!failed && !finished) { "JSON writer is no longer usable" }
        try {
            action()
        } catch (e: Exception) {
            failed = true
            throw e
        }
    }

    private fun finish() = write {
        require(generator.outputContext.inRoot() && generator.outputContext.entryCount == 1) {
            "Exactly one complete JSON root value must be written"
        }
        finished = true
    }

    private class BoundedJsonOutput : Writer() {
        private val output = StringBuilder()
        private var byteCount = 0
        private var pendingHighSurrogate = false

        override fun write(buffer: CharArray, offset: Int, length: Int) {
            append(length) { buffer[offset + it] }
        }

        override fun write(value: String, offset: Int, length: Int) {
            append(length) { value[offset + it] }
        }

        private inline fun append(length: Int, characterAt: (Int) -> Char) {
            requireJsonWriteLimit(output.length.toLong() + length <= JsonInputLimits.MAX_JSON_BYTES,
                "JSON output exceeds maximum UTF-16 length")
            var bytes = byteCount
            var pending = pendingHighSurrogate
            repeat(length) { index ->
                val character = characterAt(index)
                if (pending) {
                    require(character.isLowSurrogate()) { "Unpaired high surrogate in JSON output" }
                    pending = false
                } else {
                    bytes += when {
                        character.isHighSurrogate() -> { pending = true; 4 }
                        character.isLowSurrogate() -> throw IllegalArgumentException("Unpaired low surrogate in JSON output")
                        character.code <= 0x7f -> 1
                        character.code <= 0x7ff -> 2
                        else -> 3
                    }
                }
                requireJsonWriteLimit(bytes <= JsonInputLimits.MAX_JSON_BYTES, "JSON output exceeds maximum UTF-8 size")
            }
            repeat(length) { output.append(characterAt(it)) }
            byteCount = bytes
            pendingHighSurrogate = pending
        }

        fun content(): String {
            require(!pendingHighSurrogate) { "Unpaired high surrogate in JSON output" }
            return output.toString()
        }

        override fun flush() = Unit

        override fun close() = Unit
    }
}

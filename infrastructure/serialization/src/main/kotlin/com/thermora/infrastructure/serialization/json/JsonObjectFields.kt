package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonToken

/**
 * Strict field dispatcher for JSON objects.
 *
 * The reader must initially point to START_OBJECT.
 * Each field handler must fully consume its value.
 * On success, the reader remains at END_OBJECT.
 */
internal class JsonObjectFields(
    private val reader: StrictJsonReader,
    private val path: String
) {

    private data class Field(
        val required: Boolean,
        val handler: (StrictJsonReader, String) -> Unit
    )

    private val fields = LinkedHashMap<String, Field>()

    fun required(
        name: String,
        handler: (StrictJsonReader, String) -> Unit
    ): JsonObjectFields {
        register(name, required = true, handler)
        return this
    }

    fun optional(
        name: String,
        handler: (StrictJsonReader, String) -> Unit
    ): JsonObjectFields {
        register(name, required = false, handler)
        return this
    }

    fun read() {
        if (fields.isEmpty()) {
            throw IllegalStateException(
                "No JSON fields have been registered"
            )
        }

        reader.withReadFailures(path) { readFields() }
    }

    private fun readFields() {
        reader.expect(JsonToken.START_OBJECT)

        val seen = HashSet<String>()

        while (true) {
            val token = reader.nextToken()
                ?: throw JsonReadFailure.payload(
                    path,
                    "Unexpected end of JSON object"
                )

            if (token == JsonToken.END_OBJECT) {
                break
            }

            if (token != JsonToken.FIELD_NAME) {
                throw JsonReadFailure.payload(
                    path,
                    "Expected JSON field name"
                )
            }

            val name = reader.readFieldName()
            val fieldPath = jsonFieldPath(path, name)

            val field = fields[name]
                ?: throw JsonReadFailure.payload(
                    fieldPath,
                    "Unknown JSON field"
                )

            if (!seen.add(name)) {
                throw JsonReadFailure.payload(
                    fieldPath,
                    "Duplicate JSON field"
                )
            }

            reader.withReadFailures(fieldPath) {
                reader.nextToken()
                    ?: throw JsonReadFailure.payload(fieldPath, "Missing JSON field value")
                reader.consumeValue(fieldPath) { field.handler(reader, fieldPath) }
            }
        }

        for ((name, field) in fields) {
            if (field.required && name !in seen) {
                throw JsonReadFailure.payload(
                    jsonFieldPath(path, name),
                    "Missing required JSON field"
                )
            }
        }
    }

    private fun register(
        name: String,
        required: Boolean,
        handler: (StrictJsonReader, String) -> Unit
    ) {
        require(name.isNotBlank()) {
            "JSON field name must not be blank"
        }

        require(name !in fields) {
            "JSON field already registered: $name"
        }

        fields[name] = Field(required, handler)
    }
}

package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsonObjectFieldsTest {

    @Test
    fun `reads required fields regardless of order`() {
        var name = ""
        var count = 0

        StrictJsonReader.read(
            """{"count":3,"name":"Thermora"}"""
        ) { reader ->
            reader.nextToken()

            JsonObjectFields(reader, "$")
                .required("name") { r, _ ->
                    name = r.readString()
                }
                .required("count") { r, _ ->
                    count = r.readInt()
                }
                .read()
        }

        assertEquals("Thermora", name)
        assertEquals(3, count)
    }

    @Test
    fun `rejects missing required field`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("""{"name":"Thermora"}""") { reader ->
                reader.nextToken()

                JsonObjectFields(reader, "$")
                    .required("name") { r, _ ->
                        r.readString()
                    }
                    .required("count") { r, _ ->
                        r.readInt()
                    }
                    .read()
            }
        }

        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("$.count", failure.path)
    }

    @Test
    fun `rejects unknown field`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("""{"unknown":1}""") { reader ->
                reader.nextToken()

                JsonObjectFields(reader, "$")
                    .required("name") { r, _ ->
                        r.readString()
                    }
                    .read()
            }
        }

        assertEquals("$.unknown", failure.path)
    }

    @Test
    fun `allows explicitly nullable field`() {
        var isNull = false

        StrictJsonReader.read("""{"value":null}""") { reader ->
            reader.nextToken()

            JsonObjectFields(reader, "$")
                .required("value") { r, _ ->
                    r.readNull()
                    isNull = true
                }
                .read()
        }

        assertTrue(isNull)
    }

    @Test
    fun `distinguishes absent field from explicit null`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("{}") { reader ->
                reader.nextToken()

                JsonObjectFields(reader, "$")
                    .required("value") { r, _ ->
                        r.readNull()
                    }
                    .read()
            }
        }

        assertEquals("$.value", failure.path)
    }

    @Test
    fun `supports nested objects`() {
        var temperature = 0

        StrictJsonReader.read(
            """{"ambient":{"temperature":313}}"""
        ) { reader ->
            reader.nextToken()

            JsonObjectFields(reader, "$")
                .required("ambient") { r, path ->
                    JsonObjectFields(r, path)
                        .required("temperature") { nested, _ ->
                            temperature = nested.readInt()
                        }
                        .read()
                }
                .read()
        }

        assertEquals(313, temperature)
    }

    @Test
    fun `reader remains at end object after success`() {
        StrictJsonReader.read("""{"value":5}""") { reader ->
            reader.nextToken()

            JsonObjectFields(reader, "$")
                .required("value") { r, _ ->
                    r.readInt()
                }
                .read()

            reader.expect(JsonToken.END_OBJECT)
        }
    }

    @Test
    fun `rejects wrong root token`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("[1]") { reader ->
                reader.nextToken()

                JsonObjectFields(reader, "$")
                    .required("value") { r, _ ->
                        r.readInt()
                    }
                    .read()
            }
        }

        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("$", failure.path)
    }

    @Test
    fun `rejects duplicate field registration`() {
        StrictJsonReader.read("{}") { reader ->
            reader.nextToken()

            val fields = JsonObjectFields(reader, "$")
                .required("value") { _, _ -> }

            assertFailsWith<IllegalArgumentException> {
                fields.required("value") { _, _ -> }
            }

            reader.nextToken()
            reader.expect(JsonToken.END_OBJECT)
        }
    }
}

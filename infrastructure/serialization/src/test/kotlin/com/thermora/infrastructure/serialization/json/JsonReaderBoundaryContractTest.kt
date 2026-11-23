package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class JsonReaderBoundaryContractTest {
    @Test fun `token failures carry kind message and original cause`() {
        val cause = IllegalArgumentException("source")
        val failure = JsonTokenReadFailure(JsonFailureKind.PAYLOAD, "detail", cause)
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("detail", failure.message)
        assertSame(cause, failure.cause)
    }

    @Test fun `numeric token classification does not depend on messages`() {
        for (token in listOf("+1", "01", "NaN", "0x10", "-0." + "1".repeat(62))) {
            assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonTokenReadFailure> { StrictJsonNumber.parseDouble(token) }.kind)
        }
        for (token in listOf("1e999", "-1e999", "1e-999", "-1e-999")) {
            assertEquals(JsonFailureKind.VALUE, assertFailsWith<JsonTokenReadFailure> { StrictJsonNumber.parseDouble(token) }.kind)
        }
        val token = "-0." + "1".repeat(61)
        assertEquals(64, token.length)
        assertEquals(token.toDouble().toRawBits(), StrictJsonNumber.parseDouble(token).toRawBits())
    }

    @Test fun `all typed getter failures poison the reader`() {
        val getters: List<(StrictJsonReader) -> Unit> = listOf(
            { it.readInt() }, { it.readDouble() }, { it.readString() }, { it.readBoolean() },
            { it.readFieldName() }, { it.readNull() }, { it.expect(JsonToken.START_OBJECT) }
        )
        for (getter in getters) {
            assertFailsWith<IllegalStateException> {
                StrictJsonReader.read("[]") { r ->
                    r.nextToken()
                    val error = assertFailsWith<JsonTokenReadFailure> { getter(r) }
                    assertEquals(JsonFailureKind.PAYLOAD, error.kind)
                    r.nextToken()
                }
            }
        }
    }

    @Test fun `input size and raw Unicode failures are PAYLOAD at root`() {
        for (json in listOf(" ".repeat(JsonInputLimits.MAX_JSON_BYTES + 1), "\"\uD800\"")) {
            val failure = assertFailsWith<JsonReadFailure> { StrictJsonReader.read(json) { it.nextToken() } }
            assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
            assertEquals("$", failure.path)
            assertIs<JsonTokenReadFailure>(failure.cause)
        }
    }

    @Test fun `standalone scalar range failure has root path`() {
        val failure = assertFailsWith<JsonReadFailure> { StrictJsonReader.read("1e999") { it.nextToken() } }
        assertEquals(JsonFailureKind.VALUE, failure.kind)
        assertEquals("$", failure.path)
    }

    @Test fun `well formed nested values preserve sibling field processing`() {
        val values = ArrayList<Int>()
        StrictJsonReader.read("{\"items\":[{\"n\":1},{\"n\":2}],\"tail\":3}") { r ->
            r.nextToken()
            JsonObjectFields(r, "$")
                .required("items") { reader, path ->
                    reader.expect(JsonToken.START_ARRAY)
                    var index = 0
                    while (reader.nextToken() != JsonToken.END_ARRAY) {
                        JsonObjectFields(reader, "$path[${index++}]")
                            .required("n") { item, _ -> values += item.readInt() }.read()
                    }
                }
                .required("tail") { reader, _ -> values += reader.readInt() }
                .read()
        }
        assertEquals(listOf(1, 2, 3), values)
    }

    @Test fun `nested arrays retain exact paths for element failures`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("{\"items\":[{\"n\":1},{\"n\":true}]}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("items") { reader, path ->
                    var index = 0
                    while (reader.nextToken() != JsonToken.END_ARRAY) {
                        JsonObjectFields(reader, "$path[${index++}]")
                            .required("n") { item, _ -> item.readInt() }.read()
                    }
                }.read()
            }
        }
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("$.items[1].n", failure.path)
    }

    @Test fun `sibling empty containers are accepted by their own handlers`() {
        StrictJsonReader.read("{\"a\":[],\"b\":[],\"c\":{},\"d\":{}}") { r ->
            r.nextToken()
            JsonObjectFields(r, "$")
                .required("a") { item, _ -> assertEquals(JsonToken.END_ARRAY, item.nextToken()) }
                .required("b") { item, _ -> assertEquals(JsonToken.END_ARRAY, item.nextToken()) }
                .required("c") { item, _ -> assertEquals(JsonToken.END_OBJECT, item.nextToken()) }
                .required("d") { item, _ -> assertEquals(JsonToken.END_OBJECT, item.nextToken()) }
                .read()
        }
    }

    @Test fun `an untouched array is not a fully consumed value`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("{\"a\":[]}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a") { _, _ -> }.read()
            }
        }
        assertEquals("$.a", failure.path)
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
    }

    @Test fun `catching a boundary failure without advancing still cannot return success`() {
        assertFailsWith<IllegalStateException> {
            StrictJsonReader.read("{\"a\":null}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a") { item, _ ->
                    try { item.nextToken() } catch (_: JsonReadFailure) { }
                }.read()
            }
        }
    }

    @Test fun `optional missing field differs from optional explicit null`() {
        var calls = 0
        for (json in listOf("{}", "{\"a\":null}")) {
            StrictJsonReader.read(json) { r ->
                r.nextToken()
                JsonObjectFields(r, "$").optional("a") { item, _ -> item.readNull(); calls++ }.read()
            }
        }
        assertEquals(1, calls)
    }

    @Test fun `special field names use unambiguous bracket paths`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("{\"a.b\":true}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a.b") { item, _ -> item.readInt() }.read()
            }
        }
        assertEquals("$['a.b']", failure.path)
        assertEquals("$['a\\'b']", jsonFieldPath("$", "a'b"))
    }

    @Test fun `converted Jackson syntax failure preserves the exception cause`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("{\"a\":1,\"a\":2}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a") { item, _ -> item.readInt() }.read()
            }
        }
        assertIs<JsonParseException>(failure.cause)
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
    }

    @Test fun `depth limit remains PAYLOAD during field traversal`() {
        val json = "{\"a\":" + "[".repeat(32) + "0" + "]".repeat(32) + "}"
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read(json) { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a") { item, _ ->
                    repeat(32) { item.nextToken() }
                }.read()
            }
        }
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
    }

    @Test fun `unpaired escaped surrogate remains PAYLOAD at its field`() {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("{\"a\":\"\\uD800\"}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a") { _, _ -> }.read()
            }
        }
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("$.a", failure.path)
    }
    @Test fun `handler generation errors are not classified as JSON read errors`() {
        val expected = com.fasterxml.jackson.core.JsonGenerationException(
            "Handler writer bug", null as com.fasterxml.jackson.core.JsonGenerator?
        )
        val actual = assertFailsWith<com.fasterxml.jackson.core.JsonGenerationException> {
            StrictJsonReader.read("{\"a\":1}") { r ->
                r.nextToken()
                JsonObjectFields(r, "$").required("a") { _, _ -> throw expected }.read()
            }
        }
        assertSame(expected, actual)
    }

}

package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonToken
import java.util.concurrent.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JsonReaderHardeningTest {
    private fun field(json: String, handler: (StrictJsonReader, String) -> Unit) {
        StrictJsonReader.read(json) { reader ->
            reader.nextToken()
            JsonObjectFields(reader, "$").required("value", handler).read()
        }
    }

    private fun failure(json: String, kind: JsonFailureKind, handler: (StrictJsonReader, String) -> Unit) {
        val error = assertFailsWith<JsonReadFailure> { field(json, handler) }
        assertEquals(kind, error.kind)
        assertEquals("$.value", error.path)
    }

    @Test fun `integer type failure is PAYLOAD at the field`() {
        failure("{\"value\":\"1\"}", JsonFailureKind.PAYLOAD) { r, _ -> r.readInt() }
    }

    @Test fun `double type failure is PAYLOAD at the field`() {
        failure("{\"value\":true}", JsonFailureKind.PAYLOAD) { r, _ -> r.readDouble() }
    }

    @Test fun `integer range failure is VALUE at the field`() {
        failure("{\"value\":2147483648}", JsonFailureKind.VALUE) { r, _ -> r.readInt() }
    }

    @Test fun `fractional and exponent integer forms are PAYLOAD`() {
        for (token in listOf("1.0", "1e0")) {
            failure("{\"value\":$token}", JsonFailureKind.PAYLOAD) { r, _ -> r.readInt() }
        }
    }

    @Test fun `overflow is VALUE even before the handler starts`() {
        failure("{\"value\":1e999}", JsonFailureKind.VALUE) { _, _ -> }
    }

    @Test fun `nonzero underflow is VALUE even before the handler starts`() {
        failure("{\"value\":1e-999}", JsonFailureKind.VALUE) { _, _ -> }
    }

    @Test fun `numeric length failure is PAYLOAD`() {
        val token = "-0." + "1".repeat(62)
        failure("{\"value\":$token}", JsonFailureKind.PAYLOAD) { _, _ -> }
    }

    @Test fun `typed reads preserve negative zero and subnormal raw bits`() {
        for (value in listOf(-0.0, Double.MIN_VALUE, 2 * Double.MIN_VALUE, java.lang.Double.MIN_NORMAL, Double.MAX_VALUE)) {
            field("{\"value\":$value}") { r, _ -> assertEquals(value.toRawBits(), r.readDouble().toRawBits()) }
        }
    }

    @Test fun `array handler must reach its own END_ARRAY`() {
        failure("{\"value\":[1,2,3]}", JsonFailureKind.PAYLOAD) { r, _ -> r.nextToken(); r.readInt() }
    }

    @Test fun `object handler must reach its own END_OBJECT`() {
        failure("{\"value\":{\"inner\":{}}}", JsonFailureKind.PAYLOAD) { r, _ ->
            r.nextToken()
            r.nextToken()
            r.nextToken()
            r.expect(JsonToken.END_OBJECT)
        }
    }

    @Test fun `nested array END does not complete the containing array`() {
        failure("{\"value\":[[]]}", JsonFailureKind.PAYLOAD) { r, _ ->
            r.nextToken()
            r.nextToken()
            r.expect(JsonToken.END_ARRAY)
        }
    }

    private fun overread(json: String, handler: (StrictJsonReader, String) -> Unit) {
        val failure = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read(json) { reader ->
                reader.nextToken()
                JsonObjectFields(reader, "$")
                    .required("value", handler)
                    .optional("other") { _, _ -> }
                    .read()
            }
        }
        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("$.value", failure.path)
    }

    @Test fun `scalar handler cannot consume the following field`() {
        overread("{\"value\":1,\"other\":2}") { r, _ -> r.readInt(); r.nextToken(); r.nextToken() }
    }

    @Test fun `array handler cannot finish on a sibling END_ARRAY`() {
        overread("{\"value\":[],\"other\":[]}") { r, _ -> repeat(4) { r.nextToken() } }
    }

    @Test fun `object handler cannot finish on a sibling END_OBJECT`() {
        overread("{\"value\":{},\"other\":{}}") { r, _ -> repeat(4) { r.nextToken() } }
    }

    @Test fun `empty scalar handler cannot advance through EOF`() {
        failure("{\"value\":null}", JsonFailureKind.PAYLOAD) { r, _ ->
            r.readNull()
            while (r.nextToken() != null) { }
        }
    }

    @Test fun `nested numeric failure keeps its complete path`() {
        val error = assertFailsWith<JsonReadFailure> {
            field("{\"value\":{\"temperature\":\"hot\"}}") { r, path ->
                JsonObjectFields(r, path).required("temperature") { nested, _ -> nested.readDouble() }.read()
            }
        }
        assertEquals("$.value.temperature", error.path)
        assertEquals(JsonFailureKind.PAYLOAD, error.kind)
    }

    @Test fun `array resource failure includes the exact offending index`() {
        val json = "{\"value\":[" + List(257) { "0" }.joinToString() + "]}"
        val error = assertFailsWith<JsonReadFailure> {
            field(json) { r, _ -> while (r.nextToken() != JsonToken.END_ARRAY) { } }
        }
        assertEquals(JsonFailureKind.PAYLOAD, error.kind)
        assertEquals("$.value[256]", error.path)
    }

    @Test fun `Jackson duplicate aliases are converted with the offending path`() {
        val error = assertFailsWith<JsonReadFailure> {
            field("{\"value\":1,\"\\u0076alue\":2}") { r, _ -> r.readInt() }
        }
        assertEquals(JsonFailureKind.PAYLOAD, error.kind)
        assertEquals("$.value", error.path)
    }

    @Test fun `nested syntax error has a typed failure and cause`() {
        val error = assertFailsWith<JsonReadFailure> {
            field("{\"value\":{\"bad\":}}") { r, path ->
                JsonObjectFields(r, path).required("bad") { nested, _ -> nested.readInt() }.read()
            }
        }
        assertEquals("$.value.bad", error.path)
        assertEquals(JsonFailureKind.PAYLOAD, error.kind)
        kotlin.test.assertNotNull(error.cause)
    }

    @Test fun `programming errors and cancellation retain their original identity`() {
        for (error in listOf(IllegalArgumentException("handler bug"), IllegalStateException("state bug"), CancellationException("cancel"))) {
            val actual = assertFailsWith<Exception> { field("{\"value\":1}") { _, _ -> throw error } }
            assertSame(error, actual)
        }
        val fatal = AssertionError("fatal")
        assertSame(fatal, assertFailsWith<AssertionError> { field("{\"value\":1}") { _, _ -> throw fatal } })
    }

    @Test fun `enum and domain failures preserve explicit VALUE and original cause`() {
        val cause = IllegalArgumentException("domain invariant")
        val expected = JsonReadFailure.value("$.value", "Unsupported enum", cause)
        assertSame(expected, assertFailsWith<JsonReadFailure> { field("{\"value\":\"unknown\"}") { _, _ -> throw expected } })
        assertSame(cause, expected.cause)
    }

    @Test fun `position failure poisons the reader even when the handler catches it`() {
        assertFailsWith<IllegalStateException> {
            field("{\"value\":[],\"other\":1}") { r, _ ->
                r.nextToken()
                try { r.nextToken() } catch (_: JsonReadFailure) { }
                r.nextToken()
            }
        }
    }

    @Test fun `caught field failure cannot be turned into reader success`() {
        assertFailsWith<IllegalStateException> {
            StrictJsonReader.read("{}") { r ->
                r.nextToken()
                try { JsonObjectFields(r, "$").required("value") { _, _ -> }.read() }
                catch (_: JsonReadFailure) { }
                r.nextToken()
            }
        }
    }

    @Test fun `explicit null unknown and missing remain distinct`() {
        field("{\"value\":null}") { r, _ -> r.readNull() }
        failure("{}", JsonFailureKind.PAYLOAD) { r, _ -> r.readNull() }
        val error = assertFailsWith<JsonReadFailure> { field("{\"other\":null}") { _, _ -> } }
        assertEquals("$.other", error.path)
    }
}

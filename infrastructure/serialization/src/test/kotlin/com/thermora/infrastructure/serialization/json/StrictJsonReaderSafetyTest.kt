package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class StrictJsonReaderSafetyTest {
    private fun consume(json: String) = StrictJsonReader.read(json) { reader ->
        while (reader.nextToken() != null) { }
    }

    @Test
    fun `token traversal rejects unpaired escaped surrogate values`() {
        for (json in listOf("\"\\uD800\"", "[\"\\uDC00\"]", "{\"a\":\"\\uD800\"}")) {
            assertFailsWith<IllegalArgumentException>(json) { consume(json) }
        }
    }

    @Test
    fun `token traversal rejects unpaired escaped surrogate keys`() {
        assertFailsWith<IllegalArgumentException> { consume("{\"\\uD800\":1}") }
    }

    @Test
    fun `token traversal rejects overflowing numbers without typed reads`() {
        assertFailsWith<IllegalArgumentException> { consume("[1e999]") }
    }

    @Test
    fun `token traversal rejects underflowing numbers without typed reads`() {
        assertFailsWith<IllegalArgumentException> { consume("[1e-999]") }
    }

    @Test
    fun `token traversal rejects signed numeric token of 65 characters`() {
        val token = "-0." + "1".repeat(62)
        assertEquals(65, token.length)
        assertFailsWith<IllegalArgumentException> { consume("[$token]") }
    }

    @Test
    fun `all token traversal rejects a second root`() {
        for (json in listOf("{} {}", "[] []", "1 2", "null {}", "{} [1]")) {
            assertFailsWith<IllegalArgumentException>(json) { consume(json) }
        }
    }

    @Test
    fun `closing reader cannot hide trailing content`() {
        assertFailsWith<IllegalStateException> {
            StrictJsonReader.read("{} {}") { reader ->
                reader.nextToken()
                reader.nextToken()
                reader.close()
            }
        }
    }

    @Test
    fun `reader cannot resume after a caught Jackson parsing error`() {
        assertFailsWith<IllegalStateException> {
            StrictJsonReader.read("{\"a\":1,\"a\":2}") { reader ->
                repeat(12) {
                    try {
                        reader.nextToken()
                    } catch (_: JsonParseException) { }
                }
            }
        }
    }

    @Test
    fun `caught validation error cannot be turned into success`() {
        assertFailsWith<IllegalStateException> {
            StrictJsonReader.read("[" + List(257) { "0" }.joinToString() + "]") { reader ->
                repeat(260) {
                    try {
                        reader.nextToken()
                    } catch (_: IllegalArgumentException) { }
                }
            }
        }
    }

    @Test
    fun `EOF remains stable across repeated reads`() {
        for (json in listOf("{}", "[]", "0", "null", "\"text\"")) {
            StrictJsonReader.read(json) { reader ->
                while (reader.nextToken() != null) { }
                repeat(3) { assertNull(reader.nextToken()) }
            }
        }
    }

    @Test
    fun `valid nested Unicode and arrays can be traversed without getters`() {
        consume("{\"😀\":[\"\\uD83D\\uDE00\",{\"é\":\"e\\u0301\"},[null,true,-0.0]]}")
    }

    @Test
    fun `reader enforces numeric token boundary and preserves subnormals`() {
        val token = "-0." + "1".repeat(61)
        assertEquals(64, token.length)
        consume(token)
        for (value in listOf(-0.0, Double.MIN_VALUE, 2 * Double.MIN_VALUE, java.lang.Double.MIN_NORMAL, Double.MAX_VALUE)) {
            val actual = StrictJsonReader.read(value.toString()) { reader ->
                assertEquals(JsonToken.VALUE_NUMBER_FLOAT, reader.nextToken())
                reader.readDouble()
            }
            assertEquals(value.toRawBits(), actual.toRawBits())
        }
    }
}

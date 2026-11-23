package com.thermora.infrastructure.serialization.json

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import com.fasterxml.jackson.core.JsonToken

class JsonArrayLimitsTest {

    private fun consume(json: String) {
        StrictJsonReader.read(json) { reader ->
            while (reader.nextToken() != null) {
                // Consume all tokens.
            }
        }
    }

    @Test
    fun `should accept empty array`() {
        consume("[]")
    }

    @Test
    fun `should accept array with 256 elements`() {
        val json = List(256) { "1" }.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        )

        consume(json)
    }

    @Test
    fun `should reject array with 257 elements`() {
        val json = List(257) { "1" }.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        )

        assertFailsWith<IllegalArgumentException> {
            consume(json)
        }
    }

    @Test
    fun `should count nested arrays independently`() {
        val inner = List(256) { "1" }.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        )

        consume("[$inner,$inner]")
    }

    @Test
    fun `should reject oversized nested array`() {
        val inner = List(257) { "1" }.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        )

        assertFailsWith<IllegalArgumentException> {
            consume("""{"values":$inner}""")
        }
    }

    @Test
    fun `should count objects as array elements`() {
        val json = List(257) { """{"x":1}""" }
            .joinToString(",", "[", "]")

        assertFailsWith<IllegalArgumentException> {
            consume(json)
        }
    }
}

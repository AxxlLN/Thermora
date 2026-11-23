package com.thermora.infrastructure.serialization.json

import kotlin.test.Test
import kotlin.test.assertFailsWith

class JsonInputLimitsTest {

    @Test
    fun `should accept valid JSON`() {
        JsonInputLimits.validate("""{"value":42}""")
    }

    @Test
    fun `should accept exactly one MiB`() {
        val json = " ".repeat(JsonInputLimits.MAX_JSON_BYTES)

        JsonInputLimits.validate(json)
    }

    @Test
    fun `should reject more than one MiB`() {
        val json = " ".repeat(JsonInputLimits.MAX_JSON_BYTES + 1)

        assertFailsWith<IllegalArgumentException> {
            JsonInputLimits.validate(json)
        }
    }

    @Test
    fun `should count UTF8 bytes instead of characters`() {
        val json = "é".repeat(524_289)

        assertFailsWith<IllegalArgumentException> {
            JsonInputLimits.validate(json)
        }
    }

    @Test
    fun `should accept supplementary Unicode characters`() {
        JsonInputLimits.validate("😀")
    }

    @Test
    fun `should reject unpaired high surrogate`() {
        assertFailsWith<IllegalArgumentException> {
            JsonInputLimits.validate("\uD800")
        }
    }

    @Test
    fun `should reject unpaired low surrogate`() {
        assertFailsWith<IllegalArgumentException> {
            JsonInputLimits.validate("\uDC00")
        }
    }

    @Test
    fun `should accept byte array at size limit`() {
        val bytes = ByteArray(JsonInputLimits.MAX_JSON_BYTES)

        JsonInputLimits.validate(bytes)
    }

    @Test
    fun `should reject oversized byte array`() {
        val bytes = ByteArray(JsonInputLimits.MAX_JSON_BYTES + 1)

        assertFailsWith<IllegalArgumentException> {
            JsonInputLimits.validate(bytes)
        }
    }
}

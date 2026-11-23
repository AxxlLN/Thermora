package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonUnicodeValidatorTest {

    @Test
    fun `should accept Cyrillic text`() {
        JsonUnicodeValidator.validate("Тепловой расчёт")
    }

    @Test
    fun `should accept supplementary characters`() {
        JsonUnicodeValidator.validate("😀")
    }

    @Test
    fun `should reject unpaired high surrogate`() {
        assertFailsWith<IllegalArgumentException> {
            JsonUnicodeValidator.validate("\uD800")
        }
    }

    @Test
    fun `should reject unpaired low surrogate`() {
        assertFailsWith<IllegalArgumentException> {
            JsonUnicodeValidator.validate("\uDC00")
        }
    }

    @Test
    fun `should count Unicode code points`() {
        JsonUnicodeValidator.validateCodePointLength(
            "😀😀",
            2
        )

        assertFailsWith<IllegalArgumentException> {
            JsonUnicodeValidator.validateCodePointLength(
                "😀😀",
                1
            )
        }
    }

    @Test
    fun `should reject escaped unpaired surrogate in JSON string`() {
        val json = "\"\\uD800\""

        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(json) { reader ->
                reader.nextToken()
                reader.readString()
            }
        }
    }

    @Test
    fun `should reject escaped unpaired surrogate in field name`() {
        val json = "{\"\\uD800\":1}"

        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(json) { reader ->
                reader.nextToken()
                reader.nextToken()
                reader.readFieldName()
            }
        }
    }

    @Test
    fun `should preserve Unicode roundtrip`() {
        val original = "Температура °C — ΔT = 25 😀"

        val json = StrictJsonWriter.write { writer ->
            writer.string(original)
        }

        val restored = StrictJsonReader.read(json) { reader ->
            reader.nextToken()
            reader.readString()
        }

        assertEquals(original, restored)
    }

    @Test
    fun `should reject invalid Unicode in writer`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.string("\uD800")
            }
        }
    }

    @Test
    fun `should accept exactly 128 code points`() {
        JsonUnicodeValidator.validateCodePointLength(
            "😀".repeat(128),
            128
        )
    }

    @Test
    fun `should reject 129 code points`() {
        assertFailsWith<IllegalArgumentException> {
            JsonUnicodeValidator.validateCodePointLength(
                "😀".repeat(129),
                128
            )
        }
    }
}

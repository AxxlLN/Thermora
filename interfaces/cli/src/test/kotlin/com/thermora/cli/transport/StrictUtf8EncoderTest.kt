package com.thermora.cli.transport

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class StrictUtf8EncoderTest {

    @Test
    fun `should encode ASCII text`() {
        assertSuccess("Thermora")
    }

    @Test
    fun `should encode Unicode text`() {
        assertSuccess("Тепловой расчёт 温度")
    }

    @Test
    fun `should encode supplementary Unicode characters`() {
        assertSuccess("🌡️")
    }

    @Test
    fun `should encode empty text`() {
        assertSuccess("")
    }

    @Test
    fun `should preserve whitespace`() {
        assertSuccess(" \t\r\n")
    }

    @Test
    fun `should preserve numeric representation`() {
        assertSuccess(
            """{"value":-0.0,"other":1.00e+10}"""
        )
    }

    @Test
    fun `should preserve valid replacement character`() {
        assertSuccess("\uFFFD")
    }

    @Test
    fun `should preserve BOM character at encoding level`() {
        assertSuccess("\uFEFF")
    }

    @Test
    fun `should reject isolated high surrogate`() {

        assertEquals(
            StrictUtf8Encoder.Result.InvalidUnicode,
            StrictUtf8Encoder.encode("\uD800")
        )
    }

    @Test
    fun `should reject isolated low surrogate`() {

        assertEquals(
            StrictUtf8Encoder.Result.InvalidUnicode,
            StrictUtf8Encoder.encode("\uDC00")
        )
    }

    @Test
    fun `should reject high surrogate followed by ASCII`() {

        assertEquals(
            StrictUtf8Encoder.Result.InvalidUnicode,
            StrictUtf8Encoder.encode("\uD800a")
        )
    }

    @Test
    fun `should reject reversed surrogate pair`() {

        assertEquals(
            StrictUtf8Encoder.Result.InvalidUnicode,
            StrictUtf8Encoder.encode("\uDC00\uD800")
        )
    }

    @Test
    fun `should accept valid surrogate pair`() {
        assertSuccess("\uD83D\uDE00")
    }

    @Test
    fun `should accept ASCII exactly at byte limit`() {

        val result = StrictUtf8Encoder.encode(
            text = "abcd",
            maxBytes = 4
        )

        val success =
            assertIs<StrictUtf8Encoder.Result.Success>(result)

        assertEquals(4, success.bytes.size)
    }

    @Test
    fun `should reject ASCII above byte limit`() {

        assertEquals(
            StrictUtf8Encoder.Result.TooLarge,
            StrictUtf8Encoder.encode(
                text = "abcde",
                maxBytes = 4
            )
        )
    }

    @Test
    fun `should accept multibyte text exactly at limit`() {

        val result = StrictUtf8Encoder.encode(
            text = "ЯЯ",
            maxBytes = 4
        )

        val success =
            assertIs<StrictUtf8Encoder.Result.Success>(result)

        assertEquals(4, success.bytes.size)
    }

    @Test
    fun `should reject multibyte text above limit`() {

        assertEquals(
            StrictUtf8Encoder.Result.TooLarge,
            StrictUtf8Encoder.encode(
                text = "ЯЯ",
                maxBytes = 3
            )
        )
    }

    @Test
    fun `should accept empty text with zero limit`() {

        val result = StrictUtf8Encoder.encode(
            text = "",
            maxBytes = 0
        )

        val success =
            assertIs<StrictUtf8Encoder.Result.Success>(result)

        assertEquals(0, success.bytes.size)
    }

    @Test
    fun `should reject nonempty text with zero limit`() {

        assertEquals(
            StrictUtf8Encoder.Result.TooLarge,
            StrictUtf8Encoder.encode(
                text = "a",
                maxBytes = 0
            )
        )
    }

    @Test
    fun `should reject negative limit`() {

        assertFailsWith<IllegalArgumentException> {

            StrictUtf8Encoder.encode(
                text = "",
                maxBytes = -1
            )
        }
    }

    @Test
    fun `should reject limit above protocol maximum`() {

        assertFailsWith<IllegalArgumentException> {

            StrictUtf8Encoder.encode(
                text = "",
                maxBytes =
                    TransportLimits.MAX_RESPONSE_BYTES + 1
            )
        }
    }

    @Test
    fun `should round trip through strict decoder`() {

        val text = """{"message":"Температура 🌡️"}"""

        val encoded =
            assertIs<StrictUtf8Encoder.Result.Success>(
                StrictUtf8Encoder.encode(text)
            )

        val decoded =
            assertIs<InputReadResult.Success>(
                StrictUtf8Decoder.decode(encoded.bytes)
            )

        assertEquals(text, decoded.text)
    }

    private fun assertSuccess(text: String) {

        val result = StrictUtf8Encoder.encode(text)

        val success =
            assertIs<StrictUtf8Encoder.Result.Success>(result)

        assertContentEquals(
            text.encodeToByteArray(),
            success.bytes
        )
    }
}
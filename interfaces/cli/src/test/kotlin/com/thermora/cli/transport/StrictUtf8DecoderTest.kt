package com.thermora.cli.transport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StrictUtf8DecoderTest {

    @Test
    fun `should decode ASCII text`() {
        assertSuccess(
            """{"protocolVersion":1}"""
        )
    }

    @Test
    fun `should decode Unicode text`() {
        assertSuccess(
            """{"message":"Тепловой расчёт 温度"}"""
        )
    }

    @Test
    fun `should decode supplementary Unicode characters`() {
        assertSuccess(
            """{"message":"🌡️"}"""
        )
    }

    @Test
    fun `should preserve surrounding whitespace`() {
        assertSuccess(
            " \t{\"value\":1}\r\n"
        )
    }

    @Test
    fun `should preserve numeric representation`() {
        assertSuccess(
            """{"value":-0.0,"other":1.00e+10}"""
        )
    }

    @Test
    fun `should reject empty input`() {
        assertFailure(
            byteArrayOf(),
            InputReadError.EMPTY_REQUEST
        )
    }

    @Test
    fun `should reject JSON whitespace only`() {
        assertFailure(
            " \t\r\n".encodeToByteArray(),
            InputReadError.EMPTY_REQUEST
        )
    }

    @Test
    fun `should not classify non JSON whitespace as empty`() {
        assertSuccess("\u00A0")
    }

    @Test
    fun `should reject leading UTF-8 BOM`() {
        assertFailure(
            "\uFEFF{}".encodeToByteArray(),
            InputReadError.UNEXPECTED_BOM
        )
    }

    @Test
    fun `should reject BOM only input`() {
        assertFailure(
            "\uFEFF".encodeToByteArray(),
            InputReadError.UNEXPECTED_BOM
        )
    }

    @Test
    fun `should preserve BOM inside text`() {
        assertSuccess(
            "{\"value\":\"\uFEFF\"}"
        )
    }

    @Test
    fun `should reject invalid continuation byte`() {
        assertFailure(
            byteArrayOf(
                0xC3.toByte(),
                0x28
            ),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should reject isolated continuation byte`() {
        assertFailure(
            byteArrayOf(0x80.toByte()),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should reject truncated UTF-8 sequence`() {
        assertFailure(
            byteArrayOf(
                0xE2.toByte(),
                0x82.toByte()
            ),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should reject overlong UTF-8 encoding`() {
        assertFailure(
            byteArrayOf(
                0xC0.toByte(),
                0xAF.toByte()
            ),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should reject encoded surrogate`() {
        assertFailure(
            byteArrayOf(
                0xED.toByte(),
                0xA0.toByte(),
                0x80.toByte()
            ),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should reject code point above Unicode maximum`() {
        assertFailure(
            byteArrayOf(
                0xF4.toByte(),
                0x90.toByte(),
                0x80.toByte(),
                0x80.toByte()
            ),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should preserve valid replacement character`() {
        assertSuccess("\uFFFD")
    }

    private fun assertSuccess(text: String) {

        val result = StrictUtf8Decoder.decode(
            text.encodeToByteArray()
        )

        val success = assertIs<InputReadResult.Success>(
            result
        )

        assertEquals(text, success.text)
    }

    private fun assertFailure(
        bytes: ByteArray,
        expectedError: InputReadError
    ) {

        val result = StrictUtf8Decoder.decode(bytes)

        val failure = assertIs<InputReadResult.Failure>(
            result
        )

        assertEquals(expectedError, failure.error)
    }
}
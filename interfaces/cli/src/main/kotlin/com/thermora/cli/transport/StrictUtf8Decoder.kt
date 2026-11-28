package com.thermora.cli.transport

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Decodes request bytes using strict UTF-8 rules.
 *
 * Malformed sequences are rejected instead of being
 * replaced with the Unicode replacement character.
 */
internal object StrictUtf8Decoder {

    private const val BOM: Char = '\uFEFF'

    fun decode(bytes: ByteArray): InputReadResult {

        val decoder = StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)

        val text = try {
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            return InputReadResult.Failure(
                error = InputReadError.INVALID_UTF8,
                message = "Request contains invalid UTF-8"
            )
        }

        if (text.startsWith(BOM)) {
            return InputReadResult.Failure(
                error = InputReadError.UNEXPECTED_BOM,
                message = "UTF-8 BOM is not allowed"
            )
        }

        if (text.all(::isJsonWhitespace)) {
            return InputReadResult.Failure(
                error = InputReadError.EMPTY_REQUEST,
                message = "Request is empty"
            )
        }

        return InputReadResult.Success(text)
    }

    private fun isJsonWhitespace(char: Char): Boolean =
        when (char) {
            ' ', '\t', '\n', '\r' -> true
            else -> false
        }
}
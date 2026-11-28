package com.thermora.cli.transport

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Encodes text using strict UTF-8 rules.
 *
 * Invalid UTF-16 surrogate sequences are rejected.
 * Encoding is bounded by the supplied byte limit.
 */
internal object StrictUtf8Encoder {

    sealed interface Result {

        data class Success(
            val bytes: ByteArray
        ) : Result

        data object InvalidUnicode : Result

        data object TooLarge : Result
    }

    fun encode(
        text: String,
        maxBytes: Int = TransportLimits.MAX_RESPONSE_BYTES
    ): Result {

        require(
            maxBytes in 0..TransportLimits.MAX_RESPONSE_BYTES
        ) {
            "Encoding byte limit is outside the supported range"
        }

        if (text.length > maxBytes) {
            return Result.TooLarge
        }

        val encoder = StandardCharsets.UTF_8
            .newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)

        val input = CharBuffer.wrap(text)

        val capacity = minOf(
            maxBytes,
            text.length * 3
        )

        val output = ByteBuffer.allocate(capacity)

        val encodingResult = encoder.encode(
            input,
            output,
            true
        )

        when {
            encodingResult.isError ->
                return Result.InvalidUnicode

            encodingResult.isOverflow ->
                return Result.TooLarge
        }

        val flushResult = encoder.flush(output)

        when {
            flushResult.isError ->
                return Result.InvalidUnicode

            flushResult.isOverflow ->
                return Result.TooLarge
        }

        output.flip()

        val bytes = ByteArray(output.remaining())

        output.get(bytes)

        return Result.Success(bytes)
    }
}
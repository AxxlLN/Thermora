package com.thermora.cli.transport

import java.io.IOException
import java.io.InterruptedIOException
import java.io.OutputStream
import java.io.PrintStream

/**
 * Writes a complete UTF-8 CLI response followed by LF.
 *
 * Encoding and size validation are completed before
 * the first write to the output stream.
 *
 * The caller retains ownership of the stream.
 */
internal class BoundedOutputWriter(
    private val maxResponseBytes: Int =
        TransportLimits.MAX_RESPONSE_BYTES
) {

    init {
        require(
            maxResponseBytes in
                    1..TransportLimits.MAX_RESPONSE_BYTES
        ) {
            "Response byte limit is outside the supported range"
        }
    }

    fun write(
        output: OutputStream,
        text: String
    ): OutputWriteResult {

        ensureNotInterrupted()

        if (text.startsWith('\uFEFF')) {
            return OutputWriteResult.Failure(
                error = OutputWriteError.UNEXPECTED_BOM,
                message = "UTF-8 BOM is not allowed"
            )
        }

        if (text.all(::isJsonWhitespace)) {
            return OutputWriteResult.Failure(
                error = OutputWriteError.EMPTY_RESPONSE,
                message = "Response is empty"
            )
        }

        // Reserve one byte for the terminating LF.
        val encoded = StrictUtf8Encoder.encode(
            text = text,
            maxBytes = maxResponseBytes - 1
        )

        val bytes = when (encoded) {

            is StrictUtf8Encoder.Result.Success ->
                encoded.bytes

            StrictUtf8Encoder.Result.InvalidUnicode ->
                return OutputWriteResult.Failure(
                    error = OutputWriteError.INVALID_UTF8,
                    message = "Response contains invalid Unicode"
                )

            StrictUtf8Encoder.Result.TooLarge ->
                return responseTooLarge()
        }

        val response = bytes.copyOf(bytes.size + 1)

        response[response.lastIndex] = LF

        ensureNotInterrupted()

        try {

            output.write(response)

            ensureNotInterrupted()

            output.flush()

            ensureNotInterrupted()

            if (output is PrintStream && output.checkError()) {

                ensureNotInterrupted()

                return outputWriteFailed()
            }

            ensureNotInterrupted()

        } catch (exception: InterruptedIOException) {

            throw exception

        } catch (_: IOException) {

            ensureNotInterrupted()

            return outputWriteFailed()
        }

        return OutputWriteResult.Success(
            bytesWritten = response.size
        )
    }

    private fun responseTooLarge(): OutputWriteResult.Failure =
        OutputWriteResult.Failure(
            error = OutputWriteError.RESPONSE_TOO_LARGE,
            message = "Response exceeds $maxResponseBytes bytes"
        )

    private fun outputWriteFailed(): OutputWriteResult.Failure =
        OutputWriteResult.Failure(
            error = OutputWriteError.OUTPUT_WRITE_FAILED,
            message = "Failed to write response to output stream",
            outputMayBePartial = true
        )

    private fun ensureNotInterrupted() {

        if (Thread.currentThread().isInterrupted) {
            throw InterruptedException(
                "CLI response writing was interrupted"
            )
        }
    }

    private fun isJsonWhitespace(char: Char): Boolean =
        when (char) {
            ' ', '\t', '\r', '\n' -> true
            else -> false
        }

    private companion object {

        const val LF: Byte = 0x0A
    }
}
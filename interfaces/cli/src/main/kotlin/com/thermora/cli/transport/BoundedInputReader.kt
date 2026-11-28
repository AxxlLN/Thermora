package com.thermora.cli.transport

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException

/**
 * Reads a complete CLI request from an input stream.
 *
 * The request size is limited in bytes before UTF-8
 * decoding takes place.
 *
 * The caller retains ownership of the input stream.
 */
internal class BoundedInputReader(
    private val maxRequestBytes: Int =
        TransportLimits.MAX_REQUEST_BYTES
) {

    init {
        require(
            maxRequestBytes in
                    1..TransportLimits.MAX_REQUEST_BYTES
        ) {
            "Request byte limit is outside the supported range"
        }
    }

    fun read(input: InputStream): InputReadResult {

        val buffer = ByteArray(
            minOf(
                TransportLimits.READ_BUFFER_BYTES,
                maxRequestBytes
            )
        )

        val output = ByteArrayOutputStream(buffer.size)

        try {
            while (true) {

                ensureNotInterrupted()

                val remaining = maxRequestBytes - output.size()

                if (remaining == 0) {

                    val extraByte = input.read()

                    ensureNotInterrupted()

                    if (extraByte != -1) {
                        return requestTooLarge()
                    }

                    break
                }

                val count = input.read(
                    buffer,
                    0,
                    minOf(buffer.size, remaining)
                )

                ensureNotInterrupted()

                when {
                    count == -1 -> break

                    count == 0 -> {

                        val nextByte = input.read()

                        ensureNotInterrupted()

                        if (nextByte == -1) {
                            break
                        }

                        output.write(nextByte)
                    }

                    else -> output.write(buffer, 0, count)
                }
            }
        } catch (exception: InterruptedIOException) {
            throw exception
        } catch (_: IOException) {

            ensureNotInterrupted()

            return InputReadResult.Failure(
                error = InputReadError.INPUT_READ_FAILED,
                message = "Failed to read request from input stream"
            )
        }

        ensureNotInterrupted()

        return StrictUtf8Decoder.decode(
            output.toByteArray()
        )
    }

    private fun requestTooLarge(): InputReadResult.Failure =
        InputReadResult.Failure(
            error = InputReadError.REQUEST_TOO_LARGE,
            message = "Request exceeds $maxRequestBytes bytes"
        )

    private fun ensureNotInterrupted() {
        if (Thread.currentThread().isInterrupted) {
            throw InterruptedException(
                "CLI request reading was interrupted"
            )
        }
    }
}
package com.thermora.cli.transport

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.CancellationException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BoundedOutputWriterTest {

    @Test
    fun `should write response followed by LF`() {

        val output = ByteArrayOutputStream()

        val result = BoundedOutputWriter().write(
            output,
            "{}"
        )

        assertSuccess(result, 3)

        assertContentEquals(
            "{}\n".encodeToByteArray(),
            output.toByteArray()
        )
    }

    @Test
    fun `should write Unicode response as UTF-8`() {

        val output = ByteArrayOutputStream()

        val text = """{"message":"Температура 🌡️"}"""

        val expected = "$text\n".encodeToByteArray()

        assertSuccess(
            BoundedOutputWriter().write(output, text),
            expected.size
        )

        assertContentEquals(
            expected,
            output.toByteArray()
        )
    }

    @Test
    fun `should preserve original response text`() {

        val output = ByteArrayOutputStream()

        val text = " \t{\"value\":-0.0}\r"

        val expected = "$text\n".encodeToByteArray()

        assertSuccess(
            BoundedOutputWriter().write(output, text),
            expected.size
        )

        assertContentEquals(
            expected,
            output.toByteArray()
        )
    }

    @Test
    fun `should accept response below limit`() {

        val output = ByteArrayOutputStream()

        assertSuccess(
            BoundedOutputWriter(5).write(output, "{}"),
            3
        )
    }

    @Test
    fun `should accept response exactly at limit`() {

        val output = ByteArrayOutputStream()

        assertSuccess(
            BoundedOutputWriter(3).write(output, "{}"),
            3
        )

        assertEquals(3, output.size())
    }

    @Test
    fun `should count terminating LF in response limit`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter(2).write(output, "{}"),
            OutputWriteError.RESPONSE_TOO_LARGE
        )

        assertUntouched(output)
    }

    @Test
    fun `should enforce byte limit for Unicode response`() {

        val output = ByteArrayOutputStream()

        assertSuccess(
            BoundedOutputWriter(5).write(output, "ЯЯ"),
            5
        )

        assertEquals(5, output.size())
    }

    @Test
    fun `should reject Unicode response above byte limit`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter(4).write(output, "ЯЯ"),
            OutputWriteError.RESPONSE_TOO_LARGE
        )

        assertUntouched(output)
    }

    @Test
    fun `should accept response at production limit`() {

        val output = ByteArrayOutputStream()

        val text = "a".repeat(
            TransportLimits.MAX_RESPONSE_BYTES - 1
        )

        assertSuccess(
            BoundedOutputWriter().write(output, text),
            TransportLimits.MAX_RESPONSE_BYTES
        )

        assertEquals(
            TransportLimits.MAX_RESPONSE_BYTES,
            output.size()
        )
    }

    @Test
    fun `should reject response above production limit`() {

        val output = TrackingOutputStream()

        val text = "a".repeat(
            TransportLimits.MAX_RESPONSE_BYTES
        )

        assertFailure(
            BoundedOutputWriter().write(output, text),
            OutputWriteError.RESPONSE_TOO_LARGE
        )

        assertUntouched(output)
    }

    @Test
    fun `should reject empty response`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter().write(output, ""),
            OutputWriteError.EMPTY_RESPONSE
        )

        assertUntouched(output)
    }

    @Test
    fun `should reject whitespace only response`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter().write(output, " \t\r\n"),
            OutputWriteError.EMPTY_RESPONSE
        )

        assertUntouched(output)
    }

    @Test
    fun `should reject leading BOM`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter().write(output, "\uFEFF{}"),
            OutputWriteError.UNEXPECTED_BOM
        )

        assertUntouched(output)
    }

    @Test
    fun `should preserve BOM inside response text`() {

        val output = ByteArrayOutputStream()

        val text = "{\"value\":\"\uFEFF\"}"

        val expected = "$text\n".encodeToByteArray()

        assertSuccess(
            BoundedOutputWriter().write(output, text),
            expected.size
        )

        assertContentEquals(
            expected,
            output.toByteArray()
        )
    }

    @Test
    fun `should reject invalid high surrogate before writing`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter().write(output, "\uD800"),
            OutputWriteError.INVALID_UTF8
        )

        assertUntouched(output)
    }

    @Test
    fun `should reject invalid low surrogate before writing`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter().write(output, "\uDC00"),
            OutputWriteError.INVALID_UTF8
        )

        assertUntouched(output)
    }

    @Test
    fun `should write response in one bulk write`() {

        val output = TrackingOutputStream()

        assertSuccess(
            BoundedOutputWriter().write(output, "{}"),
            3
        )

        assertEquals(1, output.writeCalls)
    }

    @Test
    fun `should flush output after writing`() {

        val output = TrackingOutputStream()

        assertSuccess(
            BoundedOutputWriter().write(output, "{}"),
            3
        )

        assertEquals(1, output.flushCalls)
    }

    @Test
    fun `should not close output after successful write`() {

        val output = TrackingOutputStream()

        assertSuccess(
            BoundedOutputWriter().write(output, "{}"),
            3
        )

        assertFalse(output.closed)
    }

    @Test
    fun `should not close output after preparation failure`() {

        val output = TrackingOutputStream()

        assertFailure(
            BoundedOutputWriter().write(output, ""),
            OutputWriteError.EMPTY_RESPONSE
        )

        assertFalse(output.closed)
    }

    @Test
    fun `should map write IOException to output failure`() {

        val output = object : OutputStream() {

            override fun write(value: Int) {
                throw IOException("Sensitive internal details")
            }
        }

        val failure = assertFailure(
            BoundedOutputWriter().write(output, "{}"),
            OutputWriteError.OUTPUT_WRITE_FAILED,
            partial = true
        )

        assertFalse(
            failure.message.contains("Sensitive")
        )
    }

    @Test
    fun `should map flush IOException to output failure`() {

        val output = object : ByteArrayOutputStream() {

            override fun flush() {
                throw IOException("Flush failed")
            }
        }

        assertFailure(
            BoundedOutputWriter().write(output, "{}"),
            OutputWriteError.OUTPUT_WRITE_FAILED,
            partial = true
        )

        assertEquals(3, output.size())
    }

    @Test
    fun `should detect partial write failure without retry`() {

        val received = ByteArrayOutputStream()

        var writeCalls = 0

        val output = object : OutputStream() {

            override fun write(value: Int) {
                received.write(value)
            }

            override fun write(
                bytes: ByteArray,
                offset: Int,
                length: Int
            ) {

                writeCalls++

                received.write(bytes[offset].toInt())

                throw IOException("Partial write")
            }
        }

        assertFailure(
            BoundedOutputWriter().write(output, "{}"),
            OutputWriteError.OUTPUT_WRITE_FAILED,
            partial = true
        )

        assertEquals(1, writeCalls)
        assertEquals(1, received.size())
    }

    @Test
    fun `should detect PrintStream write errors`() {

        val underlying = object : OutputStream() {

            override fun write(value: Int) {
                throw IOException("Write failed")
            }
        }

        val output = PrintStream(underlying)

        assertFailure(
            BoundedOutputWriter().write(output, "{}"),
            OutputWriteError.OUTPUT_WRITE_FAILED,
            partial = true
        )
    }

    @Test
    fun `should detect PrintStream flush errors`() {

        val underlying = object : ByteArrayOutputStream() {

            override fun flush() {
                throw IOException("Flush failed")
            }
        }

        val output = PrintStream(underlying)

        assertFailure(
            BoundedOutputWriter().write(output, "{}"),
            OutputWriteError.OUTPUT_WRITE_FAILED,
            partial = true
        )
    }

    @Test
    fun `should support successful PrintStream output`() {

        val underlying = ByteArrayOutputStream()

        val output = PrintStream(underlying)

        assertSuccess(
            BoundedOutputWriter().write(output, "{}"),
            3
        )

        assertContentEquals(
            "{}\n".encodeToByteArray(),
            underlying.toByteArray()
        )
    }

    @Test
    fun `should propagate interrupted IO exception`() {

        val exception = InterruptedIOException(
            "Interrupted"
        )

        val output = failingStream(exception)

        val thrown =
            assertFailsWith<InterruptedIOException> {

                BoundedOutputWriter().write(output, "{}")
            }

        assertSame(exception, thrown)
    }

    @Test
    fun `should preserve thread interruption status`() {

        val output = TrackingOutputStream()

        try {

            Thread.currentThread().interrupt()

            assertFailsWith<InterruptedException> {

                BoundedOutputWriter().write(output, "{}")
            }

            assertTrue(
                Thread.currentThread().isInterrupted
            )

            assertUntouched(output)

        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun `should propagate cancellation exception`() {

        val exception = CancellationException(
            "Cancelled"
        )

        val thrown =
            assertFailsWith<CancellationException> {

                BoundedOutputWriter().write(
                    failingStream(exception),
                    "{}"
                )
            }

        assertSame(exception, thrown)
    }

    @Test
    fun `should propagate unexpected runtime exception`() {

        val exception = IllegalStateException(
            "Unexpected failure"
        )

        val thrown =
            assertFailsWith<IllegalStateException> {

                BoundedOutputWriter().write(
                    failingStream(exception),
                    "{}"
                )
            }

        assertSame(exception, thrown)
    }

    @Test
    fun `should reject zero limit`() {

        assertFailsWith<IllegalArgumentException> {
            BoundedOutputWriter(0)
        }
    }

    @Test
    fun `should reject negative limit`() {

        assertFailsWith<IllegalArgumentException> {
            BoundedOutputWriter(-1)
        }
    }

    @Test
    fun `should reject limit above protocol maximum`() {

        assertFailsWith<IllegalArgumentException> {

            BoundedOutputWriter(
                TransportLimits.MAX_RESPONSE_BYTES + 1
            )
        }
    }

    @Test
    fun `should support repeated writer usage`() {

        val writer = BoundedOutputWriter(4)

        val first = ByteArrayOutputStream()
        val second = TrackingOutputStream()
        val third = ByteArrayOutputStream()

        assertSuccess(
            writer.write(first, "{}"),
            3
        )

        assertFailure(
            writer.write(second, "abcd"),
            OutputWriteError.RESPONSE_TOO_LARGE
        )

        assertUntouched(second)

        assertSuccess(
            writer.write(third, "[]"),
            3
        )
    }

    private fun assertSuccess(
        result: OutputWriteResult,
        expectedBytes: Int
    ) {

        val success =
            assertIs<OutputWriteResult.Success>(result)

        assertEquals(
            expectedBytes,
            success.bytesWritten
        )
    }

    private fun assertFailure(
        result: OutputWriteResult,
        expectedError: OutputWriteError,
        partial: Boolean = false
    ): OutputWriteResult.Failure {

        val failure =
            assertIs<OutputWriteResult.Failure>(result)

        assertEquals(
            expectedError,
            failure.error
        )

        assertEquals(
            partial,
            failure.outputMayBePartial
        )

        return failure
    }

    private fun assertUntouched(
        output: TrackingOutputStream
    ) {

        assertEquals(0, output.writeCalls)
        assertEquals(0, output.flushCalls)
        assertEquals(0, output.bytes.size())
        assertFalse(output.closed)
    }

    private fun failingStream(
        exception: Exception
    ): OutputStream = object : OutputStream() {

        override fun write(value: Int) {
            throw exception
        }
    }

    private class TrackingOutputStream : OutputStream() {

        val bytes = ByteArrayOutputStream()

        var writeCalls: Int = 0
            private set

        var flushCalls: Int = 0
            private set

        var closed: Boolean = false
            private set

        override fun write(value: Int) {

            writeCalls++

            bytes.write(value)
        }

        override fun write(
            data: ByteArray,
            offset: Int,
            length: Int
        ) {

            writeCalls++

            bytes.write(
                data,
                offset,
                length
            )
        }

        override fun flush() {
            flushCalls++
        }

        override fun close() {
            closed = true
        }
    }
}
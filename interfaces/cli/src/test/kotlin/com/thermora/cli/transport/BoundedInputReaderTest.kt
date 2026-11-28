package com.thermora.cli.transport

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.util.concurrent.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BoundedInputReaderTest {

    @Test
    fun `should read valid request`() {

        val reader = BoundedInputReader()

        val text = """{"protocolVersion":1}"""

        assertSuccess(
            reader.read(text.byteInputStream()),
            text
        )
    }

    @Test
    fun `should reject empty stream`() {

        assertFailure(
            BoundedInputReader().read(
                ByteArrayInputStream(byteArrayOf())
            ),
            InputReadError.EMPTY_REQUEST
        )
    }

    @Test
    fun `should reject whitespace only request`() {

        assertFailure(
            BoundedInputReader().read(
                " \t\r\n".byteInputStream()
            ),
            InputReadError.EMPTY_REQUEST
        )
    }

    @Test
    fun `should accept request below limit`() {

        assertSuccess(
            BoundedInputReader(5).read(
                "abcd".byteInputStream()
            ),
            "abcd"
        )
    }

    @Test
    fun `should accept request exactly at limit`() {

        assertSuccess(
            BoundedInputReader(4).read(
                "abcd".byteInputStream()
            ),
            "abcd"
        )
    }

    @Test
    fun `should reject request above limit`() {

        assertFailure(
            BoundedInputReader(4).read(
                "abcde".byteInputStream()
            ),
            InputReadError.REQUEST_TOO_LARGE
        )
    }

    @Test
    fun `should enforce byte limit rather than character limit`() {

        val text = "ЯЯ"

        assertEquals(
            4,
            text.encodeToByteArray().size
        )

        assertSuccess(
            BoundedInputReader(4).read(
                text.byteInputStream()
            ),
            text
        )

        assertFailure(
            BoundedInputReader(3).read(
                text.byteInputStream()
            ),
            InputReadError.REQUEST_TOO_LARGE
        )
    }

    @Test
    fun `should accept request at production limit`() {

        val bytes = ByteArray(
            TransportLimits.MAX_REQUEST_BYTES
        ) {
            'a'.code.toByte()
        }

        val result = BoundedInputReader().read(
            ByteArrayInputStream(bytes)
        )

        val success =
            assertIs<InputReadResult.Success>(result)

        assertEquals(
            TransportLimits.MAX_REQUEST_BYTES,
            success.text.length
        )
    }

    @Test
    fun `should reject request above production limit`() {

        val bytes = ByteArray(
            TransportLimits.MAX_REQUEST_BYTES + 1
        ) {
            'a'.code.toByte()
        }

        assertFailure(
            BoundedInputReader().read(
                ByteArrayInputStream(bytes)
            ),
            InputReadError.REQUEST_TOO_LARGE
        )
    }

    @Test
    fun `should stop reading after first excessive byte`() {

        val stream = TrackingInputStream(
            ByteArray(100) {
                'a'.code.toByte()
            }
        )

        assertFailure(
            BoundedInputReader(10).read(stream),
            InputReadError.REQUEST_TOO_LARGE
        )

        assertEquals(11, stream.bytesRead)
        assertFalse(stream.closed)
    }

    @Test
    fun `should support fragmented reads`() {

        val stream = TrackingInputStream(
            data = "abcdef".encodeToByteArray(),
            maxChunkSize = 1
        )

        assertSuccess(
            BoundedInputReader(6).read(stream),
            "abcdef"
        )
    }

    @Test
    fun `should decode fragmented multibyte characters`() {

        val text = "Температура 🌡️"

        val stream = TrackingInputStream(
            data = text.encodeToByteArray(),
            maxChunkSize = 1
        )

        assertSuccess(
            BoundedInputReader().read(stream),
            text
        )
    }

    @Test
    fun `should handle zero length bulk reads`() {

        val stream = object : ByteArrayInputStream(
            "abc".encodeToByteArray()
        ) {

            override fun read(
                bytes: ByteArray,
                offset: Int,
                length: Int
            ): Int = 0
        }

        assertSuccess(
            BoundedInputReader(3).read(stream),
            "abc"
        )
    }

    @Test
    fun `should reject malformed UTF-8`() {

        val stream = ByteArrayInputStream(
            byteArrayOf(
                0xC3.toByte(),
                0x28
            )
        )

        assertFailure(
            BoundedInputReader().read(stream),
            InputReadError.INVALID_UTF8
        )
    }

    @Test
    fun `should reject leading BOM`() {

        assertFailure(
            BoundedInputReader().read(
                "\uFEFF{}".byteInputStream()
            ),
            InputReadError.UNEXPECTED_BOM
        )
    }

    @Test
    fun `should prioritize size limit over UTF-8 validation`() {

        val stream = ByteArrayInputStream(
            byteArrayOf(
                0xFF.toByte(),
                0xFF.toByte(),
                0xFF.toByte()
            )
        )

        assertFailure(
            BoundedInputReader(2).read(stream),
            InputReadError.REQUEST_TOO_LARGE
        )
    }

    @Test
    fun `should map IOException to input read failure`() {

        val stream = failingStream(
            IOException("Sensitive implementation details")
        )

        val result = BoundedInputReader().read(stream)

        val failure =
            assertIs<InputReadResult.Failure>(result)

        assertEquals(
            InputReadError.INPUT_READ_FAILED,
            failure.error
        )

        assertFalse(
            failure.message.contains("Sensitive")
        )
    }

    @Test
    fun `should propagate interrupted IO exception`() {

        val exception = InterruptedIOException(
            "Interrupted"
        )

        val thrown =
            assertFailsWith<InterruptedIOException> {

                BoundedInputReader().read(
                    failingStream(exception)
                )
            }

        assertSame(exception, thrown)
    }

    @Test
    fun `should preserve thread interruption status`() {

        val stream = TrackingInputStream(
            "abc".encodeToByteArray()
        )

        try {

            Thread.currentThread().interrupt()

            assertFailsWith<InterruptedException> {
                BoundedInputReader().read(stream)
            }

            assertTrue(
                Thread.currentThread().isInterrupted
            )

            assertEquals(0, stream.bytesRead)

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

                BoundedInputReader().read(
                    failingStream(exception)
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

                BoundedInputReader().read(
                    failingStream(exception)
                )
            }

        assertSame(exception, thrown)
    }

    @Test
    fun `should not close stream after successful read`() {

        val stream = TrackingInputStream(
            "abc".encodeToByteArray()
        )

        assertSuccess(
            BoundedInputReader().read(stream),
            "abc"
        )

        assertFalse(stream.closed)
    }

    @Test
    fun `should not close stream after decoding failure`() {

        val stream = TrackingInputStream(
            byteArrayOf(0xFF.toByte())
        )

        assertFailure(
            BoundedInputReader().read(stream),
            InputReadError.INVALID_UTF8
        )

        assertFalse(stream.closed)
    }

    @Test
    fun `should reject zero limit`() {

        assertFailsWith<IllegalArgumentException> {
            BoundedInputReader(0)
        }
    }

    @Test
    fun `should reject negative limit`() {

        assertFailsWith<IllegalArgumentException> {
            BoundedInputReader(-1)
        }
    }

    @Test
    fun `should reject limit above protocol maximum`() {

        assertFailsWith<IllegalArgumentException> {

            BoundedInputReader(
                TransportLimits.MAX_REQUEST_BYTES + 1
            )
        }
    }

    @Test
    fun `should support repeated reader usage`() {

        val reader = BoundedInputReader(4)

        assertSuccess(
            reader.read("abcd".byteInputStream()),
            "abcd"
        )

        assertFailure(
            reader.read("abcde".byteInputStream()),
            InputReadError.REQUEST_TOO_LARGE
        )

        assertSuccess(
            reader.read("xy".byteInputStream()),
            "xy"
        )
    }

    private fun assertSuccess(
        result: InputReadResult,
        expectedText: String
    ) {

        val success =
            assertIs<InputReadResult.Success>(result)

        assertEquals(expectedText, success.text)
    }

    private fun assertFailure(
        result: InputReadResult,
        expectedError: InputReadError
    ) {

        val failure =
            assertIs<InputReadResult.Failure>(result)

        assertEquals(expectedError, failure.error)
    }

    private fun failingStream(
        exception: Exception
    ): InputStream = object : InputStream() {

        override fun read(): Int {
            throw exception
        }

        override fun read(
            bytes: ByteArray,
            offset: Int,
            length: Int
        ): Int {
            throw exception
        }
    }

    private class TrackingInputStream(
        private val data: ByteArray,
        private val maxChunkSize: Int = Int.MAX_VALUE
    ) : InputStream() {

        var bytesRead: Int = 0
            private set

        var closed: Boolean = false
            private set

        override fun read(): Int {

            if (bytesRead >= data.size) {
                return -1
            }

            return data[bytesRead++].toInt() and 0xFF
        }

        override fun read(
            bytes: ByteArray,
            offset: Int,
            length: Int
        ): Int {

            if (length == 0) {
                return 0
            }

            if (bytesRead >= data.size) {
                return -1
            }

            val count = minOf(
                length,
                maxChunkSize,
                data.size - bytesRead
            )

            data.copyInto(
                destination = bytes,
                destinationOffset = offset,
                startIndex = bytesRead,
                endIndex = bytesRead + count
            )

            bytesRead += count

            return count
        }

        override fun close() {
            closed = true
        }
    }
}
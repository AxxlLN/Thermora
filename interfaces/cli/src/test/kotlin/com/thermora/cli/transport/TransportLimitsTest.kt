package com.thermora.cli.transport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransportLimitsTest {

    @Test
    fun `request limit should be two mebibytes`() {
        assertEquals(
            2_097_152,
            TransportLimits.MAX_REQUEST_BYTES
        )
    }

    @Test
    fun `response limit should be two mebibytes`() {
        assertEquals(
            2_097_152,
            TransportLimits.MAX_RESPONSE_BYTES
        )
    }

    @Test
    fun `read buffer should be eight kibibytes`() {
        assertEquals(
            8192,
            TransportLimits.READ_BUFFER_BYTES
        )
    }

    @Test
    fun `read buffer should not exceed request limit`() {
        assertTrue(
            TransportLimits.READ_BUFFER_BYTES <=
                    TransportLimits.MAX_REQUEST_BYTES
        )
    }
}
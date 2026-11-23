package com.thermora.infrastructure.serialization.json

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JsonReadFailureTest {

    @Test
    fun `payload failure preserves kind and path`() {
        val failure = JsonReadFailure.payload(
            path = "$.ambient.temperature",
            detail = "Missing required field"
        )

        assertEquals(JsonFailureKind.PAYLOAD, failure.kind)
        assertEquals("$.ambient.temperature", failure.path)
        assertTrue(failure.message!!.contains("Missing required field"))
    }

    @Test
    fun `value failure preserves kind and path`() {
        val failure = JsonReadFailure.value(
            path = "$.source.power",
            detail = "Invalid physical dimension"
        )

        assertEquals(JsonFailureKind.VALUE, failure.kind)
        assertEquals("$.source.power", failure.path)
    }

    @Test
    fun `failure preserves original cause`() {
        val cause = IllegalArgumentException("Invalid quantity")

        val failure = JsonReadFailure.value(
            path = "$.source.power",
            detail = "Cannot construct quantity",
            cause = cause
        )

        assertSame(cause, failure.cause)
    }

    @Test
    fun `root path is supported`() {
        val failure = JsonReadFailure.payload(
            path = "$",
            detail = "Root must be an object"
        )

        assertEquals("$", failure.path)
    }

    @Test
    fun `path must start with root symbol`() {
        assertFailsWith<IllegalArgumentException> {
            JsonReadFailure.payload(
                path = "ambient.temperature",
                detail = "Invalid path"
            )
        }
    }

    @Test
    fun `empty detail is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            JsonReadFailure.value(
                path = "$",
                detail = ""
            )
        }
    }

    @Test
    fun `failure is an IllegalArgumentException`() {
        val failure = JsonReadFailure.payload(
            path = "$",
            detail = "Invalid JSON"
        )

        assertTrue(failure is IllegalArgumentException)
    }
}

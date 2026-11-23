package com.thermora.infrastructure.serialization.json

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StrictJsonNumberTest {

    @Test
    fun `should parse ordinary double`() {
        assertEquals(
            123.456,
            StrictJsonNumber.parseDouble("123.456")
        )
    }

    @Test
    fun `should preserve negative zero`() {
        val result = StrictJsonNumber.parseDouble("-0.0")

        assertEquals(
            (-0.0).toRawBits(),
            result.toRawBits()
        )
    }

    @Test
    fun `should accept minimum positive double`() {
        val result = StrictJsonNumber.parseDouble(
            Double.MIN_VALUE.toString()
        )

        assertEquals(
            Double.MIN_VALUE.toRawBits(),
            result.toRawBits()
        )
    }

    @Test
    fun `should accept maximum finite double`() {
        val result = StrictJsonNumber.parseDouble(
            Double.MAX_VALUE.toString()
        )

        assertEquals(
            Double.MAX_VALUE.toRawBits(),
            result.toRawBits()
        )
    }

    @Test
    fun `should reject positive overflow`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("1e9999")
        }
    }

    @Test
    fun `should reject negative overflow`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("-1e9999")
        }
    }

    @Test
    fun `should reject positive underflow`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("1e-9999")
        }
    }

    @Test
    fun `should reject negative underflow`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("-1e-9999")
        }
    }

    @Test
    fun `should reject NaN`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("NaN")
        }
    }

    @Test
    fun `should reject Infinity`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("Infinity")
        }
    }

    @Test
    fun `should reject leading zeros`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("001")
        }
    }

    @Test
    fun `should reject leading plus sign`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("+1.0")
        }
    }

    @Test
    fun `should reject number longer than limit`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonNumber.parseDouble("1".repeat(65))
        }
    }

    @Test
    fun `should accept number at length limit`() {
        val raw = "0." + "0".repeat(61) + "1"

        assertEquals(64, raw.length)

        StrictJsonNumber.parseDouble(raw)
    }

    @Test
    fun `should reject string token in reader`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(
                """{"value":"1.5"}"""
            ) { reader ->
                reader.nextToken()
                reader.nextToken()
                reader.nextToken()

                reader.readDouble()
            }
        }
    }
}

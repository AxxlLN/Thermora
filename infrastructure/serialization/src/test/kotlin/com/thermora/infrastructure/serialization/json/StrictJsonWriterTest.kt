package com.thermora.infrastructure.serialization.json

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StrictJsonWriterTest {

    @Test
    fun `should write valid JSON object`() {
        val json = StrictJsonWriter.write { writer ->
            writer.startObject()
            writer.fieldName("name")
            writer.string("Thermora")
            writer.fieldName("value")
            writer.double(42.5)
            writer.endObject()
        }

        assertEquals(
            """{"name":"Thermora","value":42.5}""",
            json
        )
    }

    @Test
    fun `should write JSON array`() {
        val json = StrictJsonWriter.write { writer ->
            writer.startArray()
            writer.int(1)
            writer.int(2)
            writer.int(3)
            writer.endArray()
        }

        assertEquals("[1,2,3]", json)
    }

    @Test
    fun `should reject NaN`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.double(Double.NaN)
            }
        }
    }

    @Test
    fun `should reject positive infinity`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.double(Double.POSITIVE_INFINITY)
            }
        }
    }

    @Test
    fun `should reject negative infinity`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.double(Double.NEGATIVE_INFINITY)
            }
        }
    }

    @Test
    fun `should preserve negative zero`() {
        val json = StrictJsonWriter.write { writer ->
            writer.double(-0.0)
        }

        val restored = StrictJsonReader.read(json) { reader ->
            reader.nextToken()
            reader.readDouble()
        }

        assertEquals(
            (-0.0).toRawBits(),
            restored.toRawBits()
        )
    }

    @Test
    fun `should roundtrip double boundaries`() {
        val values = listOf(
            0.0,
            -0.0,
            1.0,
            -1.0,
            Double.MIN_VALUE,
            java.lang.Double.MIN_NORMAL,
            Double.MAX_VALUE,
            -Double.MAX_VALUE
        )

        for (value in values) {
            val json = StrictJsonWriter.write { writer ->
                writer.double(value)
            }

            val restored = StrictJsonReader.read(json) { reader ->
                reader.nextToken()
                reader.readDouble()
            }

            assertEquals(
                value.toRawBits(),
                restored.toRawBits(),
                "Round-trip failed for $value"
            )
        }
    }

    @Test
    fun `should roundtrip random finite doubles`() {
        val random = Random(42)
        var checked = 0

        repeat(20_000) {
            val value = Double.fromBits(random.nextLong())

            if (!value.isFinite()) {
                return@repeat
            }

            val json = StrictJsonWriter.write { writer ->
                writer.double(value)
            }

            val restored = StrictJsonReader.read(json) { reader ->
                reader.nextToken()
                reader.readDouble()
            }

            assertEquals(
                value.toRawBits(),
                restored.toRawBits()
            )

            checked++
        }

        assertTrue(checked > 19_000)
    }

    @Test
    fun `should escape string characters`() {
        val json = StrictJsonWriter.write { writer ->
            writer.string("line1\nline2\t\"quoted\"")
        }

        val restored = StrictJsonReader.read(json) { reader ->
            reader.nextToken()
            reader.readString()
        }

        assertEquals(
            "line1\nline2\t\"quoted\"",
            restored
        )
    }

    @Test
    fun `should reject oversized output`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.string("x".repeat(JsonInputLimits.MAX_JSON_BYTES))
            }
        }
    }
}

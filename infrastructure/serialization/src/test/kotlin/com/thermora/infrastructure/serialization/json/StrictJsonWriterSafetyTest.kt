package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.exc.StreamConstraintsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StrictJsonWriterSafetyTest {
    @Test
    fun `writer rejects duplicate object fields`() {
        assertFailsWith<JsonGenerationException> {
            StrictJsonWriter.write { writer ->
                writer.startObject()
                writer.fieldName("value")
                writer.int(1)
                writer.fieldName("value")
                writer.int(2)
                writer.endObject()
            }
        }
    }

    @Test
    fun `writer rejects nested duplicate object fields`() {
        assertFailsWith<JsonGenerationException> {
            StrictJsonWriter.write { writer ->
                writer.startArray()
                writer.startObject()
                writer.fieldName("😀")
                writer.nullValue()
                writer.fieldName("😀")
                writer.nullValue()
                writer.endObject()
                writer.endArray()
            }
        }
    }

    @Test
    fun `writer rejects empty output`() {
        assertFailsWith<IllegalArgumentException> { StrictJsonWriter.write { } }
    }

    @Test
    fun `writer rejects unclosed object instead of auto closing it`() {
        assertFailsWith<IllegalArgumentException> { StrictJsonWriter.write { it.startObject() } }
    }

    @Test
    fun `writer rejects unclosed array instead of auto closing it`() {
        assertFailsWith<IllegalArgumentException> { StrictJsonWriter.write { it.startArray() } }
    }

    @Test
    fun `writer rejects dangling field without a value`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.startObject()
                writer.fieldName("missing")
                writer.endObject()
            }
        }
    }

    @Test
    fun `writer rejects multiple root values`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer -> writer.int(1); writer.int(2) }
        }
    }

    @Test
    fun `writer rejects depth 33`() {
        assertFailsWith<StreamConstraintsException> {
            StrictJsonWriter.write { writer ->
                repeat(33) { writer.startArray() }
                repeat(33) { writer.endArray() }
            }
        }
    }

    @Test
    fun `writer rejects array element 257 before writing its contents`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.startArray()
                repeat(257) { writer.startObject(); writer.endObject() }
                writer.endArray()
            }
        }
    }

    @Test
    fun `writer rejects oversized nested array`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.startObject()
                writer.fieldName("array")
                writer.startArray()
                repeat(257) { writer.int(it) }
                writer.endArray()
                writer.endObject()
            }
        }
    }

    @Test
    fun `writer rejects strings that cannot be read by the strict reader`() {
        assertFailsWith<IllegalArgumentException> { StrictJsonWriter.write { it.string("x".repeat(257)) } }
    }

    @Test
    fun `writer rejects field names that cannot be read by the strict reader`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.startObject()
                writer.fieldName("x".repeat(257))
                writer.nullValue()
                writer.endObject()
            }
        }
    }

    @Test
    fun `caught writer failure cannot return a successful document`() {
        assertFailsWith<IllegalStateException> {
            StrictJsonWriter.write { writer ->
                try { writer.double(Double.NaN) } catch (_: IllegalArgumentException) { }
                writer.int(1)
            }
        }
    }

    @Test
    fun `output limit is enforced during writing before the block completes`() {
        var completed = false
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                repeat(8) { writer.startArray() }
                repeat(128) {
                    writer.startArray()
                    repeat(128) { writer.string("é".repeat(256)) }
                    writer.endArray()
                }
                repeat(8) { writer.endArray() }
                completed = true
            }
        }
        assertEquals(false, completed)
    }
}

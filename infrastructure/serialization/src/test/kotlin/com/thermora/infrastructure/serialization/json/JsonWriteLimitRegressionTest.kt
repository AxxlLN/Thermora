package com.thermora.infrastructure.serialization.json

import kotlin.test.*

/** Fixed JSON punctuation overhead: 4096 strings in 256 arrays of 16, inside one array. */
internal fun writeBudgetFixture(writer: StrictJsonWriter, bytes: Int, multibyte: Boolean = false) {
    var remaining = bytes - 12801
    writer.startArray()
    repeat(256) {
        writer.startArray()
        repeat(16) {
            val count = minOf(256, if (multibyte) remaining / 2 else remaining)
            var text = (if (multibyte) "é" else "x").repeat(count)
            remaining -= count * if (multibyte) 2 else 1
            if (multibyte && remaining == 1 && text.length < 256) { text += "a"; remaining-- }
            writer.string(text)
        }
        writer.endArray()
    }
    writer.endArray()
    check(remaining == 0)
}

class JsonWriteLimitRegressionTest {
    @Test fun `budget accepts below and exactly limit rejects one byte over at final flush`() {
        for (multibyte in listOf(false, true)) {
            for (size in listOf(JsonInputLimits.MAX_JSON_BYTES - 1, JsonInputLimits.MAX_JSON_BYTES)) {
                val json = StrictJsonWriter.write { writeBudgetFixture(it, size, multibyte) }
                assertEquals(size, json.toByteArray(Charsets.UTF_8).size)
                StrictJsonReader.read(json) { r -> while (r.nextToken() != null) { } }
            }
            var mapperReturned = false
            assertFailsWith<JsonWriteLimitFailure> {
                StrictJsonWriter.write { w ->
                    writeBudgetFixture(w, JsonInputLimits.MAX_JSON_BYTES + 1, multibyte)
                    mapperReturned = true
                }
            }
            assertTrue(mapperReturned, "The overflow must occur after the mapper, during final flush")
        }
    }

    @Test fun `direct string name and array resource limits use the typed signal`() {
        assertFailsWith<JsonWriteLimitFailure> { StrictJsonWriter.write { it.string("x".repeat(257)) } }
        assertFailsWith<JsonWriteLimitFailure> { StrictJsonWriter.write { it.startObject(); it.fieldName("x".repeat(257)) } }
        assertFailsWith<JsonWriteLimitFailure> { StrictJsonWriter.write { w -> w.startArray(); repeat(257) { w.int(0) }; w.endArray() } }
    }

    @Test fun `cleanup resource failure does not replace the primary callback failure`() {
        val original = IllegalArgumentException("mapper bug")
        val caught = assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { w ->
                writeBudgetFixture(w, JsonInputLimits.MAX_JSON_BYTES + 1)
                throw original
            }
        }
        assertSame(original, caught)
        assertTrue(caught.suppressed.any { it is JsonWriteLimitFailure })
    }
}

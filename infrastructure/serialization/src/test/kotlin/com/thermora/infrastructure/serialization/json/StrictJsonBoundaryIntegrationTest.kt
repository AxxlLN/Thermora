package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonToken
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StrictJsonBoundaryIntegrationTest {
    private fun consume(json: String) = StrictJsonReader.read(json) { reader ->
        while (reader.nextToken() != null) { }
    }

    @Test
    fun `writer accepts depth 32 and arrays of 256 elements`() {
        val json = StrictJsonWriter.write { writer ->
            repeat(32) { writer.startArray() }
            repeat(256) { writer.nullValue() }
            repeat(32) { writer.endArray() }
        }
        consume(json)
    }

    @Test
    fun `nested array counts are independent during writing`() {
        val json = StrictJsonWriter.write { writer ->
            writer.startArray()
            repeat(2) {
                writer.startArray()
                repeat(256) { writer.int(it) }
                writer.endArray()
            }
            writer.endArray()
        }
        consume(json)
    }

    @Test
    fun `duplicate detection is scoped to each object`() {
        val json = StrictJsonWriter.write { writer ->
            writer.startObject()
            writer.fieldName("value")
            writer.startObject()
            writer.fieldName("value")
            writer.int(1)
            writer.endObject()
            writer.fieldName("other")
            writer.startObject()
            writer.fieldName("value")
            writer.int(2)
            writer.endObject()
            writer.endObject()
        }
        assertEquals("{\"value\":{\"value\":1},\"other\":{\"value\":2}}", json)
        consume(json)
    }

    @Test
    fun `writer rejects field without value after an earlier completed field`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.startObject()
                writer.fieldName("a")
                writer.int(1)
                writer.fieldName("b")
                writer.endObject()
            }
        }
    }

    @Test
    fun `writer and reader agree on 256 UTF16 unit strings and names`() {
        for (text in listOf("x".repeat(256), "😀".repeat(128))) {
            val json = StrictJsonWriter.write { writer ->
                writer.startObject()
                writer.fieldName(text)
                writer.string(text)
                writer.endObject()
            }
            StrictJsonReader.read(json) { reader ->
                assertEquals(JsonToken.START_OBJECT, reader.nextToken())
                assertEquals(JsonToken.FIELD_NAME, reader.nextToken())
                assertEquals(text, reader.readFieldName())
                assertEquals(JsonToken.VALUE_STRING, reader.nextToken())
                assertEquals(text, reader.readString())
                assertEquals(JsonToken.END_OBJECT, reader.nextToken())
            }
        }
    }

    @Test
    fun `UTF8 accounting preserves supplementary pairs across generator buffers`() {
        val text = "x" + "😀".repeat(127)
        val json = StrictJsonWriter.write { writer ->
            writer.startArray()
            repeat(256) { writer.string(text) }
            writer.endArray()
        }
        StrictJsonReader.read(json) { reader ->
            assertEquals(JsonToken.START_ARRAY, reader.nextToken())
            repeat(256) {
                assertEquals(JsonToken.VALUE_STRING, reader.nextToken())
                assertEquals(text, reader.readString())
            }
            assertEquals(JsonToken.END_ARRAY, reader.nextToken())
        }
        assertEquals(2 + 255 + 256 * (2 + 1 + 127 * 4), json.toByteArray(Charsets.UTF_8).size)
    }

    private fun outputOfSize(bytes: Int, multibyte: Boolean): String {
        var remaining = bytes - 12801
        val values = List(4096) {
            val count = minOf(256, if (multibyte) remaining / 2 else remaining)
            var text = (if (multibyte) "é" else "x").repeat(count)
            remaining -= count * if (multibyte) 2 else 1
            if (multibyte && remaining == 1 && text.length < 256) {
                text += "a"
                remaining--
            }
            text
        }
        assertEquals(0, remaining)
        val groups = values.chunked(16)
        val expected = groups.joinToString(",", "[", "]") { group ->
            group.joinToString(",", "[", "]") { "\"$it\"" }
        }
        assertEquals(bytes, expected.toByteArray(Charsets.UTF_8).size)
        return StrictJsonWriter.write { writer ->
            writer.startArray()
            for (group in groups) {
                writer.startArray()
                group.forEach { writer.string(it) }
                writer.endArray()
            }
            writer.endArray()
        }.also { assertEquals(expected, it) }
    }

    @Test
    fun `ASCII output accepts exactly one MiB and rejects one byte more`() {
        consume(outputOfSize(JsonInputLimits.MAX_JSON_BYTES, false))
        assertFailsWith<IllegalArgumentException> { outputOfSize(JsonInputLimits.MAX_JSON_BYTES + 1, false) }
    }

    @Test
    fun `multibyte output accepts exactly one MiB and rejects one byte more`() {
        consume(outputOfSize(JsonInputLimits.MAX_JSON_BYTES, true))
        assertFailsWith<IllegalArgumentException> { outputOfSize(JsonInputLimits.MAX_JSON_BYTES + 1, true) }
    }

    @Test
    fun `writer refuses all incomplete nesting and misplaced fields`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonWriter.write { writer ->
                writer.startArray()
                writer.startObject()
                writer.endObject()
            }
        }
        assertFailsWith<JsonGenerationException> { StrictJsonWriter.write { it.fieldName("a") } }
        assertFailsWith<JsonGenerationException> {
            StrictJsonWriter.write { writer -> writer.startArray(); writer.endObject() }
        }
    }

    @Test
    fun `Jackson syntax failures are preserved and do not corrupt the shared factory`() {
        for (json in listOf("[1,]", "{\"a\":", "\"unterminated", "/*x*/1", "+1", "01", "0x10")) {
            assertFailsWith<JsonParseException>(json) { consume(json) }
            consume("{\"valid\":[true,null,1]}")
        }
    }

    @Test
    fun `closed reader cannot be reused through typed getters`() {
        lateinit var escaped: StrictJsonReader
        StrictJsonReader.read("1") { reader ->
            escaped = reader
            reader.nextToken()
        }
        assertFailsWith<IllegalStateException> { escaped.nextToken() }
        assertFailsWith<IllegalStateException> { escaped.readInt() }
        escaped.close()
    }

    @Test
    fun `shared factory isolates concurrent readers and writers`() {
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        try {
            val futures = (0 until 64).map { index ->
                executor.submit(Callable {
                    assertTrue(start.await(10, TimeUnit.SECONDS))
                    val json = StrictJsonWriter.write { writer ->
                        writer.startObject()
                        writer.fieldName("index")
                        writer.int(index)
                        writer.fieldName("text")
                        writer.string("Тепло 😀 $index")
                        writer.endObject()
                    }
                    StrictJsonReader.read(json) { reader ->
                        reader.nextToken()
                        reader.nextToken()
                        assertEquals("index", reader.readFieldName())
                        reader.nextToken()
                        assertEquals(index, reader.readInt())
                        reader.nextToken()
                        assertEquals("text", reader.readFieldName())
                        reader.nextToken()
                        assertEquals("Тепло 😀 $index", reader.readString())
                        reader.nextToken()
                    }
                    assertFailsWith<JsonParseException> { consume("{\"a\":1,\"a\":2}") }
                    index
                })
            }
            start.countDown()
            assertEquals((0 until 64).toList(), futures.map { it.get(15, TimeUnit.SECONDS) })
        } finally {
            start.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))
        }
    }
}

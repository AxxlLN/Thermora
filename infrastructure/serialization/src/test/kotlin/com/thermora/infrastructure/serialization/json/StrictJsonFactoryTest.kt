package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StrictJsonFactoryTest {

    @Test
    fun `should parse valid JSON object`() {
        val json = """{"value":42}"""

        StrictJsonFactory.instance.createParser(json).use { parser ->
            assertEquals(
                com.fasterxml.jackson.core.JsonToken.START_OBJECT,
                parser.nextToken()
            )

            assertEquals(
                com.fasterxml.jackson.core.JsonToken.FIELD_NAME,
                parser.nextToken()
            )

            assertEquals("value", parser.currentName())

            assertEquals(
                com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT,
                parser.nextToken()
            )

            assertEquals(42, parser.intValue)

            assertEquals(
                com.fasterxml.jackson.core.JsonToken.END_OBJECT,
                parser.nextToken()
            )

            assertEquals(null, parser.nextToken())
        }
    }

    @Test
    fun `should reject duplicate JSON keys`() {
        val json = """{"value":1,"value":2}"""

        assertFailsWith<JsonParseException> {
            StrictJsonFactory.instance.createParser(json).use { parser ->
                while (parser.nextToken() != null) {
                    // Read the entire JSON document.
                }
            }
        }
    }

    @Test
    fun `should reject escaped duplicate JSON keys`() {
        val json = """{"value":1,"\u0076alue":2}"""

        assertFailsWith<JsonParseException> {
            StrictJsonFactory.instance.createParser(json).use { parser ->
                while (parser.nextToken() != null) {
                    // Read the entire JSON document.
                }
            }
        }
    }
}
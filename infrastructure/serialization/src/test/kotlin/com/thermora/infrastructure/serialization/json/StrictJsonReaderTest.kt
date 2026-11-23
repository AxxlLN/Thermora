package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StrictJsonReaderTest {

    @Test
    fun `should read integer field`() {
        val result = StrictJsonReader.read(
            """{"value":42}"""
        ) { reader ->

            reader.nextToken()
            reader.expect(JsonToken.START_OBJECT)

            reader.nextToken()
            assertEquals("value", reader.readFieldName())

            reader.nextToken()
            val value = reader.readInt()

            reader.nextToken()
            reader.expect(JsonToken.END_OBJECT)

            value
        }

        assertEquals(42, result)
    }

    @Test
    fun `should read string field`() {
        val result = StrictJsonReader.read(
            """{"name":"Thermora"}"""
        ) { reader ->

            reader.nextToken()
            reader.expect(JsonToken.START_OBJECT)

            reader.nextToken()
            reader.readFieldName()

            reader.nextToken()
            val value = reader.readString()

            reader.nextToken()
            reader.expect(JsonToken.END_OBJECT)

            value
        }

        assertEquals("Thermora", result)
    }

    @Test
    fun `should reject string instead of integer`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(
                """{"value":"42"}"""
            ) { reader ->

                reader.nextToken()
                reader.nextToken()
                reader.nextToken()

                reader.readInt()
            }
        }
    }

    @Test
    fun `should reject integer overflow`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(
                """{"value":2147483648}"""
            ) { reader ->

                reader.nextToken()
                reader.nextToken()
                reader.nextToken()

                reader.readInt()
            }
        }
    }

    @Test
    fun `should reject trailing JSON content`() {
        assertFailsWith<Exception> {
            StrictJsonReader.read(
                """{"value":42} {"extra":1}"""
            ) { reader ->

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    // Consume first object.
                }
            }
        }
    }


    @Test
    fun `should reject empty JSON document`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read("") { reader ->
                reader.nextToken()
            }
        }
    }

    @Test
    fun `should reject unconsumed root object`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(
                """{"a":1,"b":2}"""
            ) { reader ->
                reader.nextToken()
                reader.nextToken()
                reader.nextToken()
            }
        }
    }

    @Test
    fun `should reject root object not started`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(
                """{"a":1}"""
            ) { _ ->
                // Deliberately consume nothing.
            }
        }
    }

    @Test
    fun `should accept fully consumed root array`() {
        val result = StrictJsonReader.read("[1,2]") { reader ->
            reader.nextToken()
            reader.expect(JsonToken.START_ARRAY)

            reader.nextToken()
            val first = reader.readInt()

            reader.nextToken()
            val second = reader.readInt()

            reader.nextToken()
            reader.expect(JsonToken.END_ARRAY)

            first + second
        }

        assertEquals(3, result)
    }


    @Test
    fun `should accept document consumed until EOF`() {
        val count = StrictJsonReader.read(
            """{"values":[1,2,3]}"""
        ) { reader ->
            var tokens = 0

            while (reader.nextToken() != null) {
                tokens++
            }

            tokens
        }

        assertEquals(8, count)
    }

    @Test
    fun `should reject partially consumed nested object`() {
        assertFailsWith<IllegalArgumentException> {
            StrictJsonReader.read(
                """{"outer":{"value":42}}"""
            ) { reader ->
                reader.nextToken() // START_OBJECT
                reader.nextToken() // FIELD_NAME
                reader.nextToken() // START_OBJECT
                reader.nextToken() // FIELD_NAME
                reader.nextToken() // VALUE_NUMBER_INT
            }
        }
    }

}

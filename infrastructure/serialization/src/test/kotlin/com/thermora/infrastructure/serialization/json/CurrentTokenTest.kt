package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class CurrentTokenTest {
    @Test fun `all token kinds are observed without advancing`() {
        val expected = listOf(JsonToken.START_OBJECT,JsonToken.FIELD_NAME,JsonToken.START_ARRAY,JsonToken.VALUE_NULL,JsonToken.VALUE_STRING,JsonToken.VALUE_NUMBER_INT,JsonToken.VALUE_NUMBER_FLOAT,JsonToken.VALUE_TRUE,JsonToken.VALUE_FALSE,JsonToken.START_OBJECT,JsonToken.END_OBJECT,JsonToken.END_ARRAY,JsonToken.END_OBJECT)
        lateinit var escaped: StrictJsonReader
        StrictJsonReader.read("""{"a":[null,"text",1,1.5,true,false,{}]}""") { r ->
            escaped = r
            assertNull(r.currentToken())
            for (token in expected) {
                assertEquals(token,r.nextToken())
                repeat(3) { assertEquals(token,r.currentToken()) }
            }
            assertEquals(JsonToken.END_OBJECT,r.currentToken())
            assertNull(r.nextToken())
            repeat(3) { assertNull(r.currentToken()) }
        }
        assertFailsWith<IllegalStateException> { escaped.currentToken() }
    }
    @Test fun `typed syntax and resource failures remain terminal`() {
        for (json in listOf("[]","[1,]","[" + List(257){"0"}.joinToString() + "]","[1e999]","[\"\\uD800\"]")) {
            assertFailsWith<IllegalStateException> {
                StrictJsonReader.read(json) { r ->
                    try {
                        if (json == "[]") { r.nextToken(); r.readInt() }
                        else while(r.nextToken()!=null) { }
                    } catch (_: Exception) { }
                    r.currentToken()
                }
            }
        }
    }
    @Test fun `nullable object and required null preserve boundaries`() {
        for (value in listOf("null","{\"n\":1}")) {
            StrictJsonReader.read("{\"a\":$value,\"tail\":2}") { r ->
                r.nextToken()
                JsonObjectFields(r,"$").required("a") { child,path ->
                    if(child.currentToken()==JsonToken.VALUE_NULL) child.readNull()
                    else JsonObjectFields(child,path).required("n") { n,_ -> assertEquals(1,n.readInt()) }.read()
                }.required("tail") { child,_ -> assertEquals(2,child.readInt()) }.read()
            }
        }
        for(json in listOf("{}","{\"a\":true}","{\"a\":{\"n\":false}}")) {
            val error=assertFailsWith<JsonReadFailure> {
                StrictJsonReader.read(json) { r -> r.nextToken(); JsonObjectFields(r,"$").required("a") { child,path ->
                    if(child.currentToken()==JsonToken.VALUE_NULL) child.readNull()
                    else JsonObjectFields(child,path).required("n") { n,_ -> n.readInt() }.read()
                }.read() }
            }
            assertEquals(JsonFailureKind.PAYLOAD,error.kind)
            assertEquals(if(json.contains("false")) "$.a.n" else "$.a",error.path)
        }
    }
}

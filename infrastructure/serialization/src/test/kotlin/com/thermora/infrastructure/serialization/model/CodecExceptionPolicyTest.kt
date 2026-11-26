package com.thermora.infrastructure.serialization.model

import com.thermora.application.ports.snapshot.*
import com.thermora.infrastructure.serialization.json.*
import java.util.concurrent.CancellationException
import kotlin.test.*

class CodecExceptionPolicyTest {
    @Test fun `adapter preserves programming cancellation and fatal exceptions`() {
        for(error in listOf(IllegalArgumentException("bug"),IllegalStateException("bug"),CancellationException("cancel"),AssertionError("fatal"))) {
            val codec=JsonSnapshotCodec(SnapshotSchema("test",1),String::class,{ _,_ -> throw error },{ _,_ -> throw error })
            assertSame(error,assertFails { codec.decode(SnapshotPayload(codec.schema,"{}")) })
            assertSame(error,assertFails { codec.encode("x") })
        }
    }
    @Test fun `unfinished object array and absent root remain programming failures`() {
        val writes: List<(StrictJsonWriter, String) -> Unit> = listOf(
            { w, _ -> w.startObject() },
            { w, _ -> w.startArray() },
            { _, _ -> }
        )
        for (write in writes) {
            val codec = JsonSnapshotCodec(SnapshotSchema("programming-test", 1), String::class, { _, _ -> "unused" }, write)
            val error = assertFailsWith<IllegalArgumentException> { codec.encode("x") }
            assertTrue(error.message.orEmpty().contains("complete JSON root"))
        }
    }

    @Test fun `misplaced fields and writer lifecycle failures propagate`() {
        val codec = JsonSnapshotCodec(SnapshotSchema("structure-test", 1), String::class, { _, _ -> "unused" }, { w, _ -> w.fieldName("outside") })
        assertFailsWith<com.fasterxml.jackson.core.JsonGenerationException> { codec.encode("x") }
        val lifecycle = JsonSnapshotCodec(SnapshotSchema("lifecycle-test", 1), String::class, { _, _ -> "unused" }, { w, _ ->
            assertFailsWith<IllegalArgumentException> { w.double(Double.NaN) }
            w.startObject()
        })
        assertFailsWith<IllegalStateException> { lifecycle.encode("x") }
    }

    @Test fun `final flush resource failure is typed and does not corrupt the next encode`() {
        var mapperReturned = false
        val codec = JsonSnapshotCodec(SnapshotSchema("budget-test", 1), String::class, { _, _ -> "unused" }, { w, value ->
            com.thermora.infrastructure.serialization.json.writeBudgetFixture(w,
                JsonInputLimits.MAX_JSON_BYTES + if (value == "large") 1 else 0)
            mapperReturned = true
        })
        val failure = assertIs<com.thermora.application.ports.codec.SnapshotCodecResult.Failure>(codec.encode("large"))
        assertTrue(mapperReturned)
        assertEquals(com.thermora.application.ports.codec.SnapshotCodecError.INVALID_VALUE, failure.error)
        assertIs<com.thermora.application.ports.codec.SnapshotCodecResult.Success<*>>(codec.encode("fits"))
    }

    @Test fun `resource failure during cleanup never replaces cancellation or fatal failure`() {
        for (original in listOf(IllegalArgumentException("bug"), IllegalStateException("bug"), CancellationException("cancel"), AssertionError("fatal"))) {
            val codec = JsonSnapshotCodec(SnapshotSchema("cleanup-test", 1), String::class, { _, _ -> "unused" }, { w, _ ->
                writeBudgetFixture(w, JsonInputLimits.MAX_JSON_BYTES + 1)
                throw original
            })
            val caught = assertFails { codec.encode("x") }
            assertSame(original, caught)
            assertTrue(caught.suppressed.any { it is JsonWriteLimitFailure })
        }
    }

    @Test fun `known string array and depth limits are wire failures`() {
        val writes: List<(StrictJsonWriter, String) -> Unit> = listOf(
            { w, _ -> w.string("x".repeat(257)) },
            { w, _ -> w.startArray(); repeat(257) { w.int(0) }; w.endArray() },
            { w, _ -> repeat(33) { w.startArray() } }
        )
        for (write in writes) {
            val codec = JsonSnapshotCodec(SnapshotSchema("resource-test", 1), String::class, { _, _ -> "unused" }, write)
            val error = assertIs<com.thermora.application.ports.codec.SnapshotCodecResult.Failure>(codec.encode("x"))
            assertEquals(com.thermora.application.ports.codec.SnapshotCodecError.INVALID_VALUE, error.error)
        }
    }

    @Test fun `adapter enforces depth32 before any model-specific mapping`() {
        val codec=JsonSnapshotCodec(SnapshotSchema("depth-test",1),String::class,{ r,_ ->
            r.expect(com.fasterxml.jackson.core.JsonToken.START_OBJECT)
            while(r.nextToken()!=null) { }
            "read"
        },{ w,_ -> w.startObject();w.endObject() })
        fun content(arrays:Int)="{\"nested\":"+"[".repeat(arrays)+"0"+"]".repeat(arrays)+"}"
        assertIs<com.thermora.application.ports.codec.SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,content(31))))
        val error=assertIs<com.thermora.application.ports.codec.SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,content(32))))
        assertEquals(com.thermora.application.ports.codec.SnapshotCodecError.INVALID_PAYLOAD,error.error)
    }

}

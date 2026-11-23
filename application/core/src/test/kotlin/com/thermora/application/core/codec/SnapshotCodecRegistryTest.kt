package com.thermora.application.core.codec

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.codec.SnapshotCodecError
import com.thermora.application.ports.codec.SnapshotCodecResult
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.application.ports.testing.IntegerSnapshotCodec
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

class SnapshotCodecRegistryTest {
    private val one = IntegerSnapshotCodec(SnapshotSchema("integer", 1))
    private val two = IntegerSnapshotCodec(SnapshotSchema("integer", 2), "v2:")

    @Test fun `empty registry returns no schemas or codecs`() {
        val registry = SnapshotCodecRegistry()
        assertTrue(registry.availableSchemas().isEmpty())
        assertNull(registry.find(one.schema))
        assertNull(registry.find(one.schema, Int::class))
    }

    @Test fun `one codec can be found using equivalent schema and exact type`() {
        val registry = SnapshotCodecRegistry(listOf(one))
        val schema = SnapshotSchema("integer", 1)
        assertSame(one, registry.find(schema))
        val typed: SnapshotCodec<Int> = assertNotNull(registry.find(schema, Int::class))
        assertSame(one, typed)
        assertEquals(SnapshotCodecResult.Success(7), typed.decode(SnapshotPayload(schema, "7")))
        assertEquals(listOf(schema), registry.availableSchemas())
    }

    @Test fun `typed search rejects unrelated and super types without an unchecked result`() {
        val registry = SnapshotCodecRegistry(listOf(one))
        assertNull(registry.find(one.schema, String::class))
        assertNull(registry.find(one.schema, Number::class))
        assertNull(registry.find(one.schema, Any::class))
        assertNotNull(registry.find(one.schema, Int::class))
    }

    @Test fun `versions coexist and lookup never falls back to the latest version`() {
        val registry = SnapshotCodecRegistry(listOf(two, one))
        assertSame(one, registry.find(one.schema, Int::class))
        assertSame(two, registry.find(two.schema, Int::class))
        assertNull(registry.find(SnapshotSchema("integer", 3)))
        assertNull(registry.find(SnapshotSchema("integer", Int.MAX_VALUE), Int::class))
        assertNull(registry.find(SnapshotSchema("unknown", 1)))
        assertNull(registry.find(SnapshotSchema("unknown", 1), Int::class))
        assertEquals(SnapshotCodecError.UNSUPPORTED_SCHEMA,
            assertIs<SnapshotCodecResult.Failure>(one.decode(SnapshotPayload(two.schema, "v2:7"))).error)
    }

    @Test fun `same schema version is rejected for the same or a different codec instance`() {
        assertFailsWith<IllegalArgumentException> { SnapshotCodecRegistry(listOf(one, one)) }
        val error = assertFailsWith<IllegalArgumentException> {
            SnapshotCodecRegistry(listOf(one, IntegerSnapshotCodec(SnapshotSchema("integer", 1))))
        }
        assertTrue(error.message.orEmpty().contains("integer"))
    }

    @Test fun `duplicate schema cannot be assigned to another value type`() {
        val text = object : SnapshotCodec<String> {
            override val schema = one.schema
            override val valueType = String::class
            override fun encode(value: String) = SnapshotCodecResult.Success(SnapshotPayload(schema, value))
            override fun decode(payload: SnapshotPayload) = SnapshotCodecResult.Success(payload.content)
        }
        assertFailsWith<IllegalArgumentException> { SnapshotCodecRegistry(listOf(one, text)) }
    }

    @Test fun `available schemas are sorted by ID and numeric version independently of registration order`() {
        val ten = IntegerSnapshotCodec(SnapshotSchema("integer", 10))
        val first = IntegerSnapshotCodec(SnapshotSchema("a", 1))
        val last = IntegerSnapshotCodec(SnapshotSchema("z", 1))
        val expected = listOf(first.schema, one.schema, two.schema, ten.schema, last.schema)
        assertEquals(expected, SnapshotCodecRegistry(listOf(last, ten, two, first, one)).availableSchemas())
        assertEquals(expected, SnapshotCodecRegistry(listOf(one, first, last, two, ten)).availableSchemas())
    }

    @Test fun `source collection mutation and returned list casts cannot change registrations`() {
        val source = mutableListOf<SnapshotCodec<*>>(one)
        val registry = SnapshotCodecRegistry(source)
        source.clear()
        source.add(two)
        assertEquals(listOf(one.schema), registry.availableSchemas())
        assertSame(one, registry.find(one.schema))
        assertNull(registry.find(two.schema))
        assertFailsWith<UnsupportedOperationException> { (registry.availableSchemas() as MutableList).clear() }
        assertSame(one, registry.find(one.schema))
    }

    @Test fun `construction and lookup do not execute encode or decode`() {
        var schemaReads = 0
        var typeReads = 0
        val codec = object : SnapshotCodec<Int> {
            override val schema: SnapshotSchema get() { schemaReads++; return one.schema }
            override val valueType get() = Int::class.also { typeReads++ }
            override fun encode(value: Int): SnapshotCodecResult<SnapshotPayload> = error("Must not execute")
            override fun decode(payload: SnapshotPayload): SnapshotCodecResult<Int> = error("Must not execute")
        }
        val registry = SnapshotCodecRegistry(listOf(codec))
        repeat(5) {
            assertSame(codec, registry.find(one.schema))
            assertSame(codec, registry.find(one.schema, Int::class))
            registry.availableSchemas()
        }
        assertEquals(1, schemaReads)
        assertEquals(1, typeReads)
    }

    @Test fun `codec failure is returned unchanged without mixing repository or calculation results`() {
        val failure = SnapshotCodecResult.Failure(SnapshotCodecError.INTERNAL_ERROR, "Specific encoder failure")
        val codec = object : SnapshotCodec<Int> by one {
            override fun encode(value: Int): SnapshotCodecResult<SnapshotPayload> = failure
            override fun decode(payload: SnapshotPayload): SnapshotCodecResult<Int> = failure
        }
        val found = assertNotNull(SnapshotCodecRegistry(listOf(codec)).find(codec.schema, Int::class))
        assertSame(failure, found.encode(7))
        assertSame(failure, found.decode(SnapshotPayload(codec.schema, "7")))
    }

    @Test fun `registry does not wrap cancellation interruption programming exceptions or JVM errors`() {
        val cause = IllegalStateException("Original reason")
        val cancellation = CancellationException("Cancelled").apply { initCause(cause) }
        for (exception in listOf(cancellation, InterruptedException("Interrupted"), cause, AssertionError("Critical"))) {
            val codec = object : SnapshotCodec<Int> by one {
                override fun encode(value: Int): SnapshotCodecResult<SnapshotPayload> = throw exception
            }
            val found = assertNotNull(SnapshotCodecRegistry(listOf(codec)).find(codec.schema, Int::class))
            val thrown = assertFails { found.encode(7) }
            assertSame(exception, thrown)
        }
        assertSame(cause, cancellation.cause)
    }

    @Test fun `concurrent lookups retain exact versions types and deterministic schemas`() {
        val registry = SnapshotCodecRegistry(listOf(two, one))
        val ready = CountDownLatch(16)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(16)
        try {
            val tasks = (0 until 16).map { executor.submit(Callable {
                ready.countDown()
                check(start.await(30, TimeUnit.SECONDS))
                repeat(100) {
                    assertSame(one, registry.find(one.schema, Int::class))
                    assertSame(two, registry.find(two.schema))
                    assertNull(registry.find(one.schema, String::class))
                    assertEquals(listOf(one.schema, two.schema), registry.availableSchemas())
                }
            }) }
            assertTrue(ready.await(30, TimeUnit.SECONDS))
            start.countDown()
            tasks.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            start.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS))
        }
    }

    @Test fun `heterogeneous codec registrations retain their independent exact type tokens`() {
        val text = object : SnapshotCodec<String> {
            override val schema = SnapshotSchema("stable.text", 1)
            override val valueType = String::class
            override fun encode(value: String): SnapshotCodecResult<SnapshotPayload> =
                if (value.isBlank()) SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE, "Blank text")
                else SnapshotCodecResult.Success(SnapshotPayload(schema, value))
            override fun decode(payload: SnapshotPayload): SnapshotCodecResult<String> = when {
                payload.schema.id != schema.id -> SnapshotCodecResult.Failure(SnapshotCodecError.SCHEMA_MISMATCH, "Different schema")
                payload.schema.version != schema.version -> SnapshotCodecResult.Failure(SnapshotCodecError.UNSUPPORTED_SCHEMA, "Different version")
                else -> SnapshotCodecResult.Success(payload.content)
            }
        }
        val registry = SnapshotCodecRegistry(listOf(one, text))
        val numberCodec = assertNotNull(registry.find(one.schema, Int::class))
        val textCodec = assertNotNull(registry.find(text.schema, String::class))
        assertEquals(SnapshotCodecResult.Success(SnapshotPayload(one.schema, "7")), numberCodec.encode(7))
        assertEquals(SnapshotCodecResult.Success("text"), textCodec.decode(SnapshotPayload(text.schema, "text")))
        assertNull(registry.find(one.schema, String::class))
        assertNull(registry.find(text.schema, Int::class))
    }
}

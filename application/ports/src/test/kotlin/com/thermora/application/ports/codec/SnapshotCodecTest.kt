package com.thermora.application.ports.codec

import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.application.ports.testing.IntegerSnapshotCodec
import kotlin.test.*

class SnapshotCodecTest {
    private val codec = IntegerSnapshotCodec()

    @Test fun `encode produces the declared schema and independent expected content`() {
        val payload = assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(42)).value
        assertEquals(SnapshotPayload(SnapshotSchema("test.positive-integer", 1), "42"), payload)
        assertEquals(Int::class, codec.valueType)
    }

    @Test fun `decode restores an independent payload`() {
        val payload = SnapshotPayload(SnapshotSchema("test.positive-integer", 1), "37")
        assertEquals(SnapshotCodecResult.Success(37), codec.decode(payload))
    }

    @Test fun `round trip preserves valid boundary values`() {
        for (value in listOf(1, 42, Int.MAX_VALUE)) {
            val payload = assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(value)).value
            assertEquals(SnapshotCodecResult.Success(value), codec.decode(payload))
        }
    }

    @Test fun `different schema ID is a mismatch even if content is malformed`() {
        val result = assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(SnapshotSchema("other", 1), "malformed")))
        assertEquals(SnapshotCodecError.SCHEMA_MISMATCH, result.error)
        assertEquals("Different schema ID", result.message)
    }

    @Test fun `unknown older and newer versions do not silently select another schema`() {
        val versionTwo = IntegerSnapshotCodec(SnapshotSchema(codec.schema.id, 2), "v2:")
        for (version in listOf(1, 3, Int.MAX_VALUE)) {
            val result = assertIs<SnapshotCodecResult.Failure>(versionTwo.decode(SnapshotPayload(SnapshotSchema(codec.schema.id, version), "v2:17")))
            assertEquals(SnapshotCodecError.UNSUPPORTED_SCHEMA, result.error)
        }
        assertEquals(SnapshotCodecResult.Success(17), versionTwo.decode(SnapshotPayload(versionTwo.schema, "v2:17")))
    }

    @Test fun `malformed unrepresentable and wrongly prefixed payloads are ordinary failures`() {
        for (content in listOf("not-a-number", "2147483648", " 17 ", "1.5")) {
            val result = assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema, content)))
            assertEquals(SnapshotCodecError.INVALID_PAYLOAD, result.error)
        }
        val prefixed = IntegerSnapshotCodec(prefix = "value:")
        assertEquals(SnapshotCodecError.INVALID_PAYLOAD,
            assertIs<SnapshotCodecResult.Failure>(prefixed.decode(SnapshotPayload(prefixed.schema, "17"))).error)
    }

    @Test fun `invalid domain values are distinct from malformed payloads`() {
        for (value in listOf(0, -1, Int.MIN_VALUE)) {
            assertEquals(SnapshotCodecError.INVALID_VALUE, assertIs<SnapshotCodecResult.Failure>(codec.encode(value)).error)
            assertEquals(SnapshotCodecError.INVALID_VALUE,
                assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema, value.toString()))).error)
        }
    }

    @Test fun `decode never rewrites source content or attaches JVM names to the schema`() {
        val schema = SnapshotSchema("stable.application-format", 4)
        val implementation = IntegerSnapshotCodec(schema)
        val payload = SnapshotPayload(schema, "+00017")
        assertEquals(SnapshotCodecResult.Success(17), implementation.decode(payload))
        assertSame(schema, payload.schema)
        assertEquals("+00017", payload.content)
        assertEquals("stable.application-format", implementation.schema.id)
        assertNotEquals(implementation::class.qualifiedName, implementation.schema.id)
        assertNotEquals(Int::class.qualifiedName, implementation.schema.id)
    }

    @Test fun `result variants preserve value semantics copy components and error details`() {
        val success = SnapshotCodecResult.Success("value")
        val (value) = success
        assertEquals("value", value)
        assertEquals("value", success.value)
        assertEquals(success, success.copy())
        assertEquals(success.hashCode(), success.copy().hashCode())
        assertEquals("Success(value=value)", success.toString())
        for (error in SnapshotCodecError.entries) {
            val failure = SnapshotCodecResult.Failure(error, "context for $error")
            val (code, message) = failure
            val covariant: SnapshotCodecResult<SnapshotPayload> = failure
            assertSame(failure, covariant)
            assertEquals(error, code)
            assertEquals(error, failure.error)
            assertEquals("context for $error", message)
            assertEquals(message, failure.message)
            assertEquals(failure, failure.copy())
            assertEquals(failure.hashCode(), failure.copy().hashCode())
            assertEquals("Failure(error=$error, message=$message)", failure.toString())
        }
    }

    private data class SampleInput(val value: Int) : com.thermora.calculation.api.model.CalculationInput
    private data class SampleOutput(val value: Int) : com.thermora.calculation.api.model.CalculationOutput

    private class ObjectCodec<T : Any>(
        override val schema: SnapshotSchema,
        override val valueType: kotlin.reflect.KClass<T>,
        private val create: (Int) -> T,
        private val extract: (T) -> Int
    ) : SnapshotCodec<T> {
        private val delegate = IntegerSnapshotCodec(schema)
        override fun encode(value: T) = delegate.encode(extract(value))
        override fun decode(payload: SnapshotPayload): SnapshotCodecResult<T> = when (val result = delegate.decode(payload)) {
            is SnapshotCodecResult.Success -> SnapshotCodecResult.Success(create(result.value))
            is SnapshotCodecResult.Failure -> result
        }
    }

    @Test fun `the same independent API supports typed calculation inputs and outputs`() {
        val inputs = ObjectCodec(SnapshotSchema("sample.input", 1), SampleInput::class, ::SampleInput) { it.value }
        val outputs = ObjectCodec(SnapshotSchema("sample.output", 2), SampleOutput::class, ::SampleOutput) { it.value }
        assertEquals(SnapshotCodecResult.Success(SampleInput(17)), inputs.decode(SnapshotPayload(inputs.schema, "17")))
        assertEquals(SnapshotCodecResult.Success(SnapshotPayload(inputs.schema, "17")), inputs.encode(SampleInput(17)))
        assertEquals(SnapshotCodecResult.Success(SampleOutput(34)), outputs.decode(SnapshotPayload(outputs.schema, "34")))
        assertEquals(SnapshotCodecResult.Success(SnapshotPayload(outputs.schema, "34")), outputs.encode(SampleOutput(34)))
        assertEquals(SampleInput::class, inputs.valueType)
        assertEquals(SampleOutput::class, outputs.valueType)
    }
}

package com.thermora.infrastructure.persistence.memory

import com.thermora.application.core.codec.SnapshotCodecRegistry
import com.thermora.application.ports.codec.SnapshotCodecError
import com.thermora.application.ports.codec.SnapshotCodecResult
import com.thermora.application.ports.record.*
import com.thermora.application.ports.snapshot.*
import com.thermora.application.ports.testing.IntegerSnapshotCodec
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant
import kotlin.test.*

class CodecHistoryIntegrationTest {
    private val inputCodec = IntegerSnapshotCodec(SnapshotSchema("sample.input", 2), "input:")
    private val outputCodec = IntegerSnapshotCodec(SnapshotSchema("sample.output", 7), "output:")
    private val oldInputCodec = IntegerSnapshotCodec(SnapshotSchema("sample.input", 1))
    private val registry = SnapshotCodecRegistry(listOf(inputCodec, outputCodec, oldInputCodec))

    private fun payload(codec: IntegerSnapshotCodec, value: Int): SnapshotPayload =
        assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(value)).value

    private fun record(output: SnapshotPayload? = payload(outputCodec, 84)) = CalculationRecord(
        CalculationRecordId("codec-history"), 3,
        CalculationSnapshot(CalculationModelId("uninstalled-model"), "independent-model-build",
            payload(inputCodec, 42), output,
            if (output == null) CalculationStatus.FAILURE else CalculationStatus.SUCCESS,
            Instant.EPOCH, Instant.EPOCH,
            listOf(SnapshotDiagnostic.from(Diagnostic("ORIGINAL", "Preserved", DiagnosticSeverity.WARNING)))),
        Instant.EPOCH
    )

    @Test fun `encode store read and decode retain separate input output record and model versions`() {
        val repository: CalculationRecordRepository = InMemoryCalculationRecordRepository()
        val original = record()
        assertIs<RepositoryResult.Success<Unit>>(repository.insert(original))
        val stored = assertNotNull(assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(original.id)).value)
        val input = assertNotNull(registry.find(stored.snapshot.input.schema, Int::class))
        val outputPayload = assertNotNull(stored.snapshot.output)
        val output = assertNotNull(registry.find(outputPayload.schema, Int::class))
        assertEquals(SnapshotCodecResult.Success(42), input.decode(stored.snapshot.input))
        assertEquals(SnapshotCodecResult.Success(84), output.decode(outputPayload))
        assertEquals(3, stored.schemaVersion)
        assertEquals("independent-model-build", stored.snapshot.modelVersion)
        assertEquals(2, stored.snapshot.input.schema.version)
        assertEquals(7, outputPayload.schema.version)
        assertEquals(original, stored)
        assertEquals("input:42", stored.snapshot.input.content)
        assertEquals("output:84", outputPayload.content)
        assertEquals(listOf("ORIGINAL"), stored.snapshot.diagnostics.map { it.code })
    }

    @Test fun `failure history can restore input without output codec or installed model`() {
        val repository = InMemoryCalculationRecordRepository()
        val original = record(null)
        assertIs<RepositoryResult.Success<Unit>>(repository.insert(original))
        val stored = assertNotNull(assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(original.id)).value)
        val inputOnly = SnapshotCodecRegistry(listOf(inputCodec))
        assertEquals(CalculationStatus.FAILURE, stored.snapshot.status)
        assertNull(stored.snapshot.output)
        assertEquals(SnapshotCodecResult.Success(42),
            assertNotNull(inputOnly.find(stored.snapshot.input.schema, Int::class)).decode(stored.snapshot.input))
    }

    @Test fun `missing codec version does not prevent reading or silently migrate the stored payload`() {
        val repository = InMemoryCalculationRecordRepository()
        val original = record()
        repository.insert(original)
        val stored = assertNotNull(assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(original.id)).value)
        val oldRegistry = SnapshotCodecRegistry(listOf(oldInputCodec))
        assertNull(oldRegistry.find(stored.snapshot.input.schema, Int::class))
        assertEquals(SnapshotCodecError.UNSUPPORTED_SCHEMA,
            assertIs<SnapshotCodecResult.Failure>(oldInputCodec.decode(stored.snapshot.input)).error)
        assertEquals(original.snapshot.input, stored.snapshot.input)
    }

    @Test fun `payload decoding failure remains separate from successful repository lookup`() {
        val repository = InMemoryCalculationRecordRepository()
        val original = record(SnapshotPayload(outputCodec.schema, "not-an-output"))
        repository.insert(original)
        val lookup = assertIs<RepositoryResult.Success<CalculationRecord?>>(repository.findById(original.id))
        val storedPayload = assertNotNull(assertNotNull(lookup.value).snapshot.output)
        val codec = assertNotNull(registry.find(storedPayload.schema, Int::class))
        assertEquals(SnapshotCodecError.INVALID_PAYLOAD,
            assertIs<SnapshotCodecResult.Failure>(codec.decode(storedPayload)).error)
        assertEquals("not-an-output", storedPayload.content)
    }
}

package com.thermora.application.ports.snapshot

import com.thermora.application.ports.testing.RecordFixtures
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant
import kotlin.test.*

class SnapshotContractsTest {
    @Test fun `schema rejects blank IDs and nonpositive versions without inventing a version grammar`() {
        for (id in listOf("", " ", "\t\n")) {
            assertFailsWith<IllegalArgumentException> { SnapshotSchema(id, 1) }
        }
        for (version in listOf(Int.MIN_VALUE, -1, 0)) {
            assertFailsWith<IllegalArgumentException> { SnapshotSchema("schema", version) }
        }
        for (version in listOf(1, Int.MAX_VALUE)) {
            val schema = SnapshotSchema("vendor.schema/v1", version)
            val (id, restoredVersion) = schema
            assertEquals("vendor.schema/v1", schema.id)
            assertEquals("vendor.schema/v1", id)
            assertEquals(version, restoredVersion)
            assertEquals(schema, schema.copy())
            assertEquals(schema.hashCode(), schema.copy().hashCode())
            assertEquals("SnapshotSchema(id=vendor.schema/v1, version=$version)", schema.toString())
        }
    }

    @Test fun `payload preserves opaque content exactly and rejects blank content including copy`() {
        val schema = SnapshotSchema("not-json", 1)
        for (content in listOf("", " ", "\n\t")) {
            assertFailsWith<IllegalArgumentException> { SnapshotPayload(schema, content) }
        }
        val payload = SnapshotPayload(schema, " \nnot JSON\n ")
        val (savedSchema, content) = payload
        assertSame(schema, savedSchema)
        assertEquals(" \nnot JSON\n ", payload.content)
        assertEquals(" \nnot JSON\n ", content)
        assertEquals(payload, payload.copy())
        assertEquals(payload.hashCode(), payload.copy().hashCode())
        assertTrue(payload.toString().contains("not JSON"))
        assertFailsWith<IllegalArgumentException> { payload.copy(content = " ") }
    }

    @Test fun `snapshot rejects blank model version but permits opaque build identifiers`() {
        for (version in listOf("", " ", "\n")) {
            assertFailsWith<IllegalArgumentException> { RecordFixtures.snapshot(modelVersion = version) }
        }
        assertEquals("git:123+local", RecordFixtures.snapshot(modelVersion = "git:123+local").modelVersion)
    }

    @Test fun `all status output combinations obey the invariant`() {
        val output = RecordFixtures.snapshot().output
        assertNotNull(RecordFixtures.snapshot(status = CalculationStatus.SUCCESS, output = output).output)
        assertNull(RecordFixtures.snapshot(status = CalculationStatus.FAILURE, output = null).output)
        assertFailsWith<IllegalArgumentException> { RecordFixtures.snapshot(status = CalculationStatus.SUCCESS, output = null) }
        assertFailsWith<IllegalArgumentException> { RecordFixtures.snapshot(status = CalculationStatus.FAILURE, output = output) }
    }

    @Test fun `timestamps support equal extreme values and reject reversed intervals`() {
        for (time in listOf(Instant.MIN, Instant.EPOCH, Instant.MAX)) {
            val s = RecordFixtures.snapshot(startedAt = time, completedAt = time)
            assertEquals(time, s.startedAt)
            assertEquals(time, s.completedAt)
        }
        val widest = RecordFixtures.snapshot(startedAt = Instant.MIN, completedAt = Instant.MAX)
        assertEquals(Instant.MIN, widest.startedAt)
        assertFailsWith<IllegalArgumentException> { RecordFixtures.snapshot(startedAt = Instant.MAX, completedAt = Instant.MIN) }
    }

    @Test fun `snapshot equality includes every field with consistent hash and rendering`() {
        val original = RecordFixtures.snapshot()
        val equivalent = RecordFixtures.snapshot()
        assertTrue(original.equals(original))
        assertEquals(original, equivalent)
        assertEquals(equivalent, original)
        assertEquals(original.hashCode(), equivalent.hashCode())
        assertFalse(original.equals(null))
        assertFalse(original.equals("snapshot"))
        val variants = listOf(
            RecordFixtures.snapshot(modelId = CalculationModelId("other")),
            RecordFixtures.snapshot(modelVersion = "other"),
            RecordFixtures.snapshot(input = SnapshotPayload(SnapshotSchema("input", 2), "changed")),
            RecordFixtures.snapshot(output = SnapshotPayload(SnapshotSchema("output", 3), "changed")),
            RecordFixtures.snapshot(output = null, status = CalculationStatus.FAILURE),
            RecordFixtures.snapshot(startedAt = Instant.MIN),
            RecordFixtures.snapshot(completedAt = Instant.MAX),
            RecordFixtures.snapshot(diagnostics = emptyList())
        )
        variants.forEach { assertNotEquals(original, it) }
        val failed = RecordFixtures.snapshot(output = null, status = CalculationStatus.FAILURE)
        assertEquals(failed, RecordFixtures.snapshot(output = null, status = CalculationStatus.FAILURE))
        assertEquals(failed.hashCode(), RecordFixtures.snapshot(output = null, status = CalculationStatus.FAILURE).hashCode())
        assertEquals("CalculationSnapshot(modelId=${original.modelId}, modelVersion=${original.modelVersion}, input=${original.input}, output=${original.output}, status=${original.status}, startedAt=${original.startedAt}, completedAt=${original.completedAt}, diagnostics=${original.diagnostics})", original.toString())
    }

    @Test fun `snapshot diagnostics preserve order duplicates and independent nested details`() {
        val details = linkedMapOf("first" to "1", "second" to "2")
        val first = RecordFixtures.diagnostic(details = details)
        val diagnostics = mutableListOf(first, RecordFixtures.diagnostic("SECOND"), first)
        val snapshot = RecordFixtures.snapshot(diagnostics = diagnostics)
        val hash = snapshot.hashCode()
        details.clear()
        diagnostics.clear()
        assertEquals(listOf("WARNING", "SECOND", "WARNING"), snapshot.diagnostics.map { it.code })
        assertEquals(listOf("first", "second"), snapshot.diagnostics.first().details.keys.toList())
        assertEquals(hash, snapshot.hashCode())
        assertFailsWith<UnsupportedOperationException> { (snapshot.diagnostics as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (snapshot.diagnostics.first().details as MutableMap).clear() }
        assertSame(snapshot.input, snapshot.input)
    }

    @Test fun `diagnostic round trips preserve every severity null source and insertion order`() {
        for (severity in DiagnosticSeverity.entries) {
            for (source in listOf(null, "", "model")) {
                val original = Diagnostic("CODE", "Message", severity, source, linkedMapOf("b" to "2", "a" to "1"))
                val snapshot = SnapshotDiagnostic.from(original)
                assertEquals(original.code, snapshot.code)
                assertEquals(original.message, snapshot.message)
                assertEquals(original.severity, snapshot.severity)
                assertEquals(original.source, snapshot.source)
                assertEquals(original, snapshot.toDiagnostic())
                assertEquals(listOf("b", "a"), snapshot.details.keys.toList())
                assertEquals(snapshot, SnapshotDiagnostic.from(snapshot.toDiagnostic()))
                assertEquals(snapshot.hashCode(), SnapshotDiagnostic.from(original).hashCode())
            }
        }
    }

    @Test fun `diagnostic value semantics include all fields without imposing new foundation restrictions`() {
        val d = SnapshotDiagnostic("C", "M", DiagnosticSeverity.INFO, "S", mapOf("k" to "v"))
        assertTrue(d.equals(d))
        assertFalse(d.equals(null))
        assertFalse(d.equals("diagnostic"))
        val variants = listOf(
            SnapshotDiagnostic("X", "M", DiagnosticSeverity.INFO, "S", mapOf("k" to "v")),
            SnapshotDiagnostic("C", "X", DiagnosticSeverity.INFO, "S", mapOf("k" to "v")),
            SnapshotDiagnostic("C", "M", DiagnosticSeverity.ERROR, "S", mapOf("k" to "v")),
            SnapshotDiagnostic("C", "M", DiagnosticSeverity.INFO),
            SnapshotDiagnostic("C", "M", DiagnosticSeverity.INFO, "S")
        )
        variants.forEach { assertNotEquals(d, it) }
        val nullable = SnapshotDiagnostic("", "", DiagnosticSeverity.INFO)
        assertEquals(Diagnostic("", "", DiagnosticSeverity.INFO), nullable.toDiagnostic())
        assertEquals(nullable.hashCode(), SnapshotDiagnostic("", "", DiagnosticSeverity.INFO).hashCode())
        assertEquals("SnapshotDiagnostic(code=C, message=M, severity=INFO, source=S, details={k=v})", d.toString())
    }

    @Test fun `map entries and iterators cannot mutate diagnostic details`() {
        val d = RecordFixtures.diagnostic(details = mapOf("k" to "v"))
        assertFailsWith<UnsupportedOperationException> { (d.details as MutableMap)["x"] = "y" }
        assertFailsWith<UnsupportedOperationException> { (d.details.entries.first() as MutableMap.MutableEntry).setValue("changed") }
        assertFailsWith<UnsupportedOperationException> {
            val iterator = (d.details as MutableMap).entries.iterator()
            iterator.next()
            iterator.remove()
        }
        assertEquals(mapOf("k" to "v"), d.details)
    }
}

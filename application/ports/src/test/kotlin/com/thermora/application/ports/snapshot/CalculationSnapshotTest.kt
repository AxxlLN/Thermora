
package com.thermora.application.ports.snapshot

import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalculationSnapshotTest {

    private val input = SnapshotPayload(
        schema = SnapshotSchema("thermora.test.input", 1),
        content = """{"power":10.0}"""
    )

    private val output = SnapshotPayload(
        schema = SnapshotSchema("thermora.test.output", 1),
        content = """{"temperature":42.0}"""
    )

    private val start = Instant.parse("2026-01-01T10:00:00Z")
    private val end = Instant.parse("2026-01-01T10:00:01Z")

    private fun snapshot(
        status: CalculationStatus,
        output: SnapshotPayload?
    ) = CalculationSnapshot(
        modelId = CalculationModelId("test-model"),
        modelVersion = "1.0.0",
        input = input,
        output = output,
        status = status,
        startedAt = start,
        completedAt = end
    )

    @Test
    fun `successful snapshot requires output`() {
        assertFailsWith<IllegalArgumentException> {
            snapshot(CalculationStatus.SUCCESS, null)
        }
    }

    @Test
    fun `failed snapshot cannot contain output`() {
        assertFailsWith<IllegalArgumentException> {
            snapshot(CalculationStatus.FAILURE, output)
        }
    }

    @Test
    fun `valid successful snapshot is accepted`() {
        val result = snapshot(CalculationStatus.SUCCESS, output)

        assertEquals(CalculationStatus.SUCCESS, result.status)
        assertEquals(output, result.output)
    }

    @Test
    fun `valid failed snapshot is accepted`() {
        val result = snapshot(CalculationStatus.FAILURE, null)

        assertEquals(CalculationStatus.FAILURE, result.status)
    }


    @Test
    fun `snapshot diagnostics preserve order and are protected`() {
        val diagnostics = mutableListOf(
            SnapshotDiagnostic(
                code = "FIRST",
                message = "First warning",
                severity = DiagnosticSeverity.WARNING
            ),
            SnapshotDiagnostic(
                code = "SECOND",
                message = "Second warning",
                severity = DiagnosticSeverity.WARNING
            )
        )

        val snapshot = CalculationSnapshot(
            modelId = CalculationModelId("test-model"),
            modelVersion = "1.0.0",
            input = input,
            output = output,
            status = CalculationStatus.SUCCESS,
            startedAt = start,
            completedAt = end,
            diagnostics = diagnostics
        )

        diagnostics.clear()

        assertEquals(
            listOf("FIRST", "SECOND"),
            snapshot.diagnostics.map { it.code }
        )

        assertFailsWith<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST")
            (snapshot.diagnostics as MutableList<SnapshotDiagnostic>).clear()
        }
    }

}

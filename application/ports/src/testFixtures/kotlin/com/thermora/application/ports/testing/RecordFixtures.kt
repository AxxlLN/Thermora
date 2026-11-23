package com.thermora.application.ports.testing

import com.thermora.application.ports.record.CalculationRecord
import com.thermora.application.ports.record.CalculationRecordId
import com.thermora.application.ports.snapshot.CalculationSnapshot
import com.thermora.application.ports.snapshot.CalculationStatus
import com.thermora.application.ports.snapshot.SnapshotDiagnostic
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant

object RecordFixtures {
    val time: Instant = Instant.parse("2026-01-01T00:00:00Z")

    fun diagnostic(code: String = "WARNING", source: String? = "model", details: Map<String, String> = emptyMap()) =
        SnapshotDiagnostic(code, "Message", DiagnosticSeverity.WARNING, source, details)

    fun snapshot(
        modelId: CalculationModelId = CalculationModelId("historical-model"),
        modelVersion: String = "1.0.0",
        input: SnapshotPayload = SnapshotPayload(SnapshotSchema("input", 2), "opaque-input"),
        output: SnapshotPayload? = SnapshotPayload(SnapshotSchema("output", 3), "opaque-output"),
        status: CalculationStatus = CalculationStatus.SUCCESS,
        startedAt: Instant = time,
        completedAt: Instant = time.plusSeconds(1),
        diagnostics: List<SnapshotDiagnostic> = listOf(diagnostic())
    ) = CalculationSnapshot(modelId, modelVersion, input, output, status, startedAt, completedAt, diagnostics)

    fun record(id: String = "record", createdAt: Instant = time.plusSeconds(1), content: String = "opaque-output") =
        CalculationRecord(CalculationRecordId(id), 4,
            snapshot(output = SnapshotPayload(SnapshotSchema("output", 3), content),
                startedAt = createdAt, completedAt = createdAt), createdAt, "build-sha")
}

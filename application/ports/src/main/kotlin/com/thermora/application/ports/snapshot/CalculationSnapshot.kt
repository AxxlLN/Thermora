package com.thermora.application.ports.snapshot

import com.thermora.calculation.api.metadata.CalculationModelId
import java.time.Instant
import java.util.Collections

class CalculationSnapshot(
    val modelId: CalculationModelId,
    val modelVersion: String,
    val input: SnapshotPayload,
    val output: SnapshotPayload?,
    val status: CalculationStatus,
    val startedAt: Instant,
    val completedAt: Instant,
    diagnostics: List<SnapshotDiagnostic> = emptyList()
) {
    val diagnostics: List<SnapshotDiagnostic> =
        Collections.unmodifiableList(ArrayList(diagnostics))

    init {
        require(modelVersion.isNotBlank()) {
            "Model version must not be blank"
        }

        require(!completedAt.isBefore(startedAt)) {
            "Completion time must not precede start time"
        }

        require(status != CalculationStatus.SUCCESS || output != null) {
            "Successful calculation must have an output"
        }

        require(status != CalculationStatus.FAILURE || output == null) {
            "Failed calculation must not have an output"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CalculationSnapshot) return false
        return modelId == other.modelId && modelVersion == other.modelVersion &&
            input == other.input && output == other.output && status == other.status &&
            startedAt == other.startedAt && completedAt == other.completedAt && diagnostics == other.diagnostics
    }

    override fun hashCode(): Int {
        var result = modelId.hashCode()
        result = 31 * result + modelVersion.hashCode()
        result = 31 * result + input.hashCode()
        result = 31 * result + (output?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + startedAt.hashCode()
        result = 31 * result + completedAt.hashCode()
        result = 31 * result + diagnostics.hashCode()
        return result
    }

    override fun toString(): String =
        "CalculationSnapshot(modelId=$modelId, modelVersion=$modelVersion, input=$input, output=$output, status=$status, startedAt=$startedAt, completedAt=$completedAt, diagnostics=$diagnostics)"
}

package com.thermora.application.ports.record

import com.thermora.application.ports.snapshot.CalculationSnapshot
import java.time.Instant

data class CalculationRecord(
    val id: CalculationRecordId,
    val schemaVersion: Int,
    val snapshot: CalculationSnapshot,
    val createdAt: Instant,
    val applicationVersion: String? = null
) {
    init {
        require(schemaVersion > 0) {
            "Record schema version must be positive"
        }

        require(applicationVersion == null || applicationVersion.isNotBlank()) {
            "Application version must not be blank"
        }

        require(!createdAt.isBefore(snapshot.completedAt)) {
            "Record creation time must not precede calculation completion"
        }
    }
}

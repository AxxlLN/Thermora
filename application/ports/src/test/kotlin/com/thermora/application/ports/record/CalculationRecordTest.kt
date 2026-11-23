package com.thermora.application.ports.record

import com.thermora.application.ports.snapshot.*
import com.thermora.calculation.api.metadata.CalculationModelId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalculationRecordTest {

    private val startedAt = Instant.parse("2026-01-01T10:00:00Z")
    private val completedAt = Instant.parse("2026-01-01T10:00:01Z")

    private val snapshot = CalculationSnapshot(
        modelId = CalculationModelId("test-model"),
        modelVersion = "1.0.0",
        input = SnapshotPayload(
            SnapshotSchema("test.input", 1),
            """{"power":10}"""
        ),
        output = SnapshotPayload(
            SnapshotSchema("test.output", 1),
            """{"temperature":40}"""
        ),
        status = CalculationStatus.SUCCESS,
        startedAt = startedAt,
        completedAt = completedAt
    )

    @Test
    fun `valid record is accepted`() {
        val record = CalculationRecord(
            id = CalculationRecordId("record-001"),
            schemaVersion = 1,
            snapshot = snapshot,
            createdAt = completedAt,
            applicationVersion = "0.1.0"
        )

        assertEquals("record-001", record.id.value)
        assertEquals(1, record.schemaVersion)
        assertEquals("0.1.0", record.applicationVersion)
    }

    @Test
    fun `invalid schema version is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CalculationRecord(
                id = CalculationRecordId("record-001"),
                schemaVersion = 0,
                snapshot = snapshot,
                createdAt = completedAt
            )
        }
    }

    @Test
    fun `record cannot be created before calculation completes`() {
        assertFailsWith<IllegalArgumentException> {
            CalculationRecord(
                id = CalculationRecordId("record-001"),
                schemaVersion = 1,
                snapshot = snapshot,
                createdAt = startedAt
            )
        }
    }

    @Test
    fun `blank application version is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CalculationRecord(
                id = CalculationRecordId("record-001"),
                schemaVersion = 1,
                snapshot = snapshot,
                createdAt = completedAt,
                applicationVersion = " "
            )
        }
    }
}

package com.thermora.infrastructure.persistence.memory

import com.thermora.application.ports.record.*
import com.thermora.application.ports.snapshot.*
import com.thermora.calculation.api.metadata.CalculationModelId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InMemoryCalculationRecordRepositoryTest {

    private fun record(
        id: String,
        createdAt: Instant = Instant.parse("2026-01-01T10:00:01Z")
    ): CalculationRecord {
        val snapshot = CalculationSnapshot(
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
            startedAt = Instant.parse("2026-01-01T10:00:00Z"),
            completedAt = Instant.parse("2026-01-01T10:00:01Z")
        )

        return CalculationRecord(
            id = CalculationRecordId(id),
            schemaVersion = 1,
            snapshot = snapshot,
            createdAt = createdAt
        )
    }

    @Test
    fun `inserted record can be retrieved`() {
        val repository = InMemoryCalculationRecordRepository()
        val record = record("record-001")

        assertIs<RepositoryResult.Success<Unit>>(
            repository.insert(record)
        )

        val result = assertIs<RepositoryResult.Success<CalculationRecord?>>(
            repository.findById(record.id)
        )

        assertEquals(record, result.value)
    }

    @Test
    fun `duplicate ID returns conflict`() {
        val repository = InMemoryCalculationRecordRepository()
        val record = record("record-001")

        repository.insert(record)

        val result = assertIs<RepositoryResult.Failure>(
            repository.insert(record)
        )

        assertEquals(RepositoryError.CONFLICT, result.error)
    }

    @Test
    fun `missing record returns null`() {
        val repository = InMemoryCalculationRecordRepository()

        val result = assertIs<RepositoryResult.Success<CalculationRecord?>>(
            repository.findById(CalculationRecordId("missing"))
        )

        assertEquals(null, result.value)
    }

    @Test
    fun `history is sorted by creation time descending`() {
        val repository = InMemoryCalculationRecordRepository()

        repository.insert(
            record(
                "old",
                Instant.parse("2026-01-01T10:00:01Z")
            )
        )

        repository.insert(
            record(
                "new",
                Instant.parse("2026-01-01T10:00:03Z")
            )
        )

        val result = assertIs<RepositoryResult.Success<CalculationHistoryPage>>(
            repository.findPage(CalculationHistoryQuery())
        )

        assertEquals(
            listOf("new", "old"),
            result.value.records.map { it.id.value }
        )
    }

    @Test
    fun `pagination returns correct hasMore`() {
        val repository = InMemoryCalculationRecordRepository()

        repeat(3) {
            repository.insert(record("record-$it"))
        }

        val first = assertIs<RepositoryResult.Success<CalculationHistoryPage>>(
            repository.findPage(
                CalculationHistoryQuery(limit = 2, offset = 0)
            )
        )

        assertEquals(2, first.value.records.size)
        assertTrue(first.value.hasMore)

        val second = assertIs<RepositoryResult.Success<CalculationHistoryPage>>(
            repository.findPage(
                CalculationHistoryQuery(limit = 2, offset = 2)
            )
        )

        assertEquals(1, second.value.records.size)
        assertFalse(second.value.hasMore)
    }
}

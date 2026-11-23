package com.thermora.application.ports.snapshot

import com.thermora.application.ports.record.CalculationHistoryPage
import com.thermora.application.ports.record.CalculationRecord
import com.thermora.application.ports.record.CalculationRecordId
import com.thermora.calculation.api.metadata.CalculationModelId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SnapshotValueSemanticsTest {
    private fun snapshot() = CalculationSnapshot(
        CalculationModelId("historical-model"), "opaque-build-version",
        SnapshotPayload(SnapshotSchema("input", 1), "input"),
        SnapshotPayload(SnapshotSchema("output", 2), "output"),
        CalculationStatus.SUCCESS, Instant.EPOCH, Instant.EPOCH
    )

    private fun record() = CalculationRecord(CalculationRecordId("record"), 3, snapshot(), Instant.EPOCH)

    @Test fun `independently restored snapshots have value semantics`() {
        assertEquals(snapshot(), snapshot())
    }

    @Test fun `independently restored records have value semantics`() {
        assertEquals(record(), record())
    }

    @Test fun `equivalent history pages have value semantics`() {
        assertEquals(CalculationHistoryPage(listOf(record()), false), CalculationHistoryPage(listOf(record()), false))
    }
}

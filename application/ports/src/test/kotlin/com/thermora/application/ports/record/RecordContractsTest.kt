package com.thermora.application.ports.record

import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.application.ports.testing.RecordFixtures
import java.time.Instant
import java.util.UUID
import kotlin.test.*

class RecordContractsTest {
    @Test fun `ID boundaries retain exact case and whitespace without normalization`() {
        for (invalid in listOf("", " ", "\t\n", "x".repeat(129))) {
            assertFailsWith<IllegalArgumentException> { CalculationRecordId(invalid) }
        }
        for (valid in listOf("x", "x".repeat(128), " A ", "идентификатор")) {
            val id = CalculationRecordId(valid)
            assertEquals(valid, id.value)
            assertEquals(id, CalculationRecordId(valid))
            assertEquals(id.hashCode(), CalculationRecordId(valid).hashCode())
            assertEquals("CalculationRecordId(value=$valid)", id.toString())
        }
        assertNotEquals(CalculationRecordId("A"), CalculationRecordId("a"))
        val random = CalculationRecordId.random()
        assertEquals(random.value, UUID.fromString(random.value).toString())
    }

    @Test fun `query accepts exact limit offset boundaries and rejects invalid values`() {
        assertEquals(CalculationHistoryQuery(50, 0), CalculationHistoryQuery())
        for (limit in listOf(Int.MIN_VALUE, -1, 0, 101, Int.MAX_VALUE)) {
            assertFailsWith<IllegalArgumentException> { CalculationHistoryQuery(limit) }
        }
        for (offset in listOf(Long.MIN_VALUE, -1L)) {
            assertFailsWith<IllegalArgumentException> { CalculationHistoryQuery(offset = offset) }
        }
        for (query in listOf(CalculationHistoryQuery(1, 0), CalculationHistoryQuery(100, Long.MAX_VALUE))) {
            val (limit, offset) = query
            assertEquals(query.limit, limit)
            assertEquals(query.offset, offset)
            assertEquals(query, query.copy())
            assertEquals(query.hashCode(), query.copy().hashCode())
            assertEquals("CalculationHistoryQuery(limit=$limit, offset=$offset)", query.toString())
        }
        assertFailsWith<IllegalArgumentException> { CalculationHistoryQuery().copy(limit = 0) }
    }

    @Test fun `record preserves independent versions nullable metadata and all components`() {
        val record = RecordFixtures.record()
        val (id, version, snapshot, createdAt, applicationVersion) = record
        assertEquals(record.id, id)
        assertEquals(4, version)
        assertSame(record.snapshot, snapshot)
        assertEquals(record.createdAt, createdAt)
        assertEquals("build-sha", applicationVersion)
        assertEquals("1.0.0", snapshot.modelVersion)
        assertEquals(2, snapshot.input.schema.version)
        assertEquals(3, snapshot.output!!.schema.version)
        assertEquals(record, record.copy(snapshot = RecordFixtures.snapshot(startedAt = createdAt, completedAt = createdAt)))
        assertEquals(record.hashCode(), record.copy().hashCode())
        assertNull(record.copy(applicationVersion = null).applicationVersion)
        assertEquals("CalculationRecord(id=$id, schemaVersion=$version, snapshot=$snapshot, createdAt=$createdAt, applicationVersion=$applicationVersion)", record.toString())
    }

    @Test fun `record enforces its own version and chronology including extreme timestamps`() {
        val record = RecordFixtures.record()
        for (version in listOf(Int.MIN_VALUE, -1, 0)) {
            assertFailsWith<IllegalArgumentException> { record.copy(schemaVersion = version) }
        }
        assertEquals(Int.MAX_VALUE, record.copy(schemaVersion = Int.MAX_VALUE).schemaVersion)
        for (version in listOf("", " ", "\t")) {
            assertFailsWith<IllegalArgumentException> { record.copy(applicationVersion = version) }
        }
        assertFailsWith<IllegalArgumentException> { record.copy(createdAt = Instant.MIN) }
        for (time in listOf(Instant.MIN, Instant.EPOCH, Instant.MAX)) {
            assertEquals(time, RecordFixtures.record(createdAt = time).createdAt)
        }
        assertEquals(Instant.MAX, record.copy(createdAt = Instant.MAX).createdAt)
    }

    @Test fun `record equality changes with every record field`() {
        val record = RecordFixtures.record()
        listOf(record.copy(id = CalculationRecordId("other")), record.copy(schemaVersion = 5),
            record.copy(snapshot = RecordFixtures.snapshot(output = SnapshotPayload(SnapshotSchema("output", 3), "other"))),
            record.copy(createdAt = Instant.MAX), record.copy(applicationVersion = "other")).forEach {
            assertNotEquals(record, it)
        }
    }

    @Test fun `history page copies source list and has structural equality`() {
        val records = mutableListOf(RecordFixtures.record())
        val page = CalculationHistoryPage(records, true)
        val hash = page.hashCode()
        records.clear()
        assertEquals(listOf(RecordFixtures.record()), page.records)
        assertTrue(page.hasMore)
        assertEquals(hash, page.hashCode())
        assertFailsWith<UnsupportedOperationException> { (page.records as MutableList).clear() }
        assertTrue(page.equals(page))
        assertFalse(page.equals(null))
        assertFalse(page.equals(emptyList<Any>()))
        assertEquals(page, CalculationHistoryPage(listOf(RecordFixtures.record()), true))
        assertEquals(page.hashCode(), CalculationHistoryPage(listOf(RecordFixtures.record()), true).hashCode())
        assertNotEquals(page, CalculationHistoryPage(page.records, false))
        assertNotEquals(page, CalculationHistoryPage(emptyList(), true))
        assertEquals("CalculationHistoryPage(records=${page.records}, hasMore=true)", page.toString())
        assertTrue(CalculationHistoryPage(emptyList(), false).records.isEmpty())
    }

    @Test fun `repository result variants retain nullable values and distinguish every error`() {
        val nullable: RepositoryResult<String?> = RepositoryResult.Success(null)
        assertNull(assertIs<RepositoryResult.Success<String?>>(nullable).value)
        val success = RepositoryResult.Success("value")
        val (value) = success
        assertEquals("value", value)
        assertEquals(success, success.copy())
        assertEquals(success.hashCode(), success.copy().hashCode())
        assertEquals("Success(value=value)", success.toString())
        for (error in RepositoryError.entries) {
            val failure = RepositoryResult.Failure(error)
            val (actual) = failure
            assertEquals(error, failure.error)
            assertEquals(error, actual)
            assertEquals(failure, failure.copy())
            assertEquals(failure.hashCode(), failure.copy().hashCode())
            assertEquals("Failure(error=$error)", failure.toString())
            assertNotEquals<RepositoryResult<*>>(success, failure)
        }
    }
}

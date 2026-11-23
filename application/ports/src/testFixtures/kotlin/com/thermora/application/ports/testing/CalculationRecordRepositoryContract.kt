package com.thermora.application.ports.testing

import com.thermora.application.ports.record.*
import com.thermora.application.ports.snapshot.CalculationStatus
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

abstract class CalculationRecordRepositoryContract {
    protected abstract fun createRepository(): CalculationRecordRepository

    private fun <T> value(result: RepositoryResult<T>): T = assertIs<RepositoryResult.Success<T>>(result).value

    @Test fun `contract inserts and reads an equivalent independently reconstructed record`() {
        val repository = createRepository()
        val record = RecordFixtures.record()
        assertEquals(Unit, value(repository.insert(record)))
        assertEquals(RecordFixtures.record(), value(repository.findById(record.id)))
    }

    @Test fun `contract duplicates conflict and preserve the first record even with different content`() {
        val repository = createRepository()
        val original = RecordFixtures.record(content = "first")
        value(repository.insert(original))
        for (candidate in listOf(original, RecordFixtures.record(content = "different"))) {
            assertEquals(RepositoryError.CONFLICT, assertIs<RepositoryResult.Failure>(repository.insert(candidate)).error)
            assertEquals(original, value(repository.findById(original.id)))
        }
    }

    @Test fun `contract missing record and empty history are successful empty results`() {
        val repository = createRepository()
        assertNull(value(repository.findById(CalculationRecordId("missing"))))
        for (offset in listOf(0L, 1L, Int.MAX_VALUE.toLong(), Long.MAX_VALUE)) {
            val page = value(repository.findPage(CalculationHistoryQuery(1, offset)))
            assertTrue(page.records.isEmpty())
            assertFalse(page.hasMore)
        }
    }

    @Test fun `contract single record has no more data at exact and extreme offsets`() {
        val repository = createRepository()
        val record = RecordFixtures.record()
        value(repository.insert(record))
        val page = value(repository.findPage(CalculationHistoryQuery(1, 0)))
        assertEquals(listOf(record), page.records)
        assertFalse(page.hasMore)
        for (offset in listOf(1L, 2L, Int.MAX_VALUE.toLong(), Long.MAX_VALUE - 1, Long.MAX_VALUE)) {
            val empty = value(repository.findPage(CalculationHistoryQuery(100, offset)))
            assertTrue(empty.records.isEmpty())
            assertFalse(empty.hasMore)
        }
    }

    @Test fun `contract history sorts by creation time then ID descending irrespective of insertion order`() {
        val repository = createRepository()
        val time = RecordFixtures.time.plusSeconds(10)
        val records = listOf(RecordFixtures.record("a", time), RecordFixtures.record("newest", time.plusSeconds(1)),
            RecordFixtures.record("c", time), RecordFixtures.record("oldest", time.minusSeconds(1)), RecordFixtures.record("b", time))
        records.forEach { value(repository.insert(it)) }
        assertEquals(listOf("newest", "c", "b", "a", "oldest"),
            value(repository.findPage(CalculationHistoryQuery())).records.map { it.id.value })
    }

    @Test fun `contract every page boundary preserves order and hasMore without offset overflow`() {
        val repository = createRepository()
        val records = (0..104).map { RecordFixtures.record("record-$it", RecordFixtures.time.plusSeconds(it.toLong())) }
        records.forEach { value(repository.insert(it)) }
        val expected = records.reversed()
        for (limit in listOf(1, 2, 7, 50, 100)) {
            for (offset in listOf(0, 1, 2, 49, 50, 99, 100, 103, 104, 105, 106)) {
                val page = value(repository.findPage(CalculationHistoryQuery(limit, offset.toLong())))
                val remaining = expected.drop(offset)
                assertEquals(remaining.take(limit), page.records, "limit=$limit offset=$offset")
                assertEquals(remaining.size > limit, page.hasMore, "limit=$limit offset=$offset")
            }
        }
        val extreme = value(repository.findPage(CalculationHistoryQuery(100, Long.MAX_VALUE)))
        assertTrue(extreme.records.isEmpty())
        assertFalse(extreme.hasMore)
    }

    @Test fun `contract preserves failure records and ordered diagnostics without requiring installed models`() {
        val repository = createRepository()
        val snapshot = RecordFixtures.snapshot(output = null, status = CalculationStatus.FAILURE,
            diagnostics = listOf(RecordFixtures.diagnostic("FIRST"), RecordFixtures.diagnostic("SECOND")))
        val record = RecordFixtures.record().copy(snapshot = snapshot, createdAt = snapshot.completedAt)
        value(repository.insert(record))
        val restored = assertNotNull(value(repository.findById(record.id)))
        assertEquals(record, restored)
        assertNull(restored.snapshot.output)
        assertEquals(listOf("FIRST", "SECOND"), restored.snapshot.diagnostics.map { it.code })
    }

    @Test fun `contract source mutations and returned collections cannot alter stored records`() {
        val repository = createRepository()
        val details = mutableMapOf("k" to "original")
        val diagnostics = mutableListOf(RecordFixtures.diagnostic(details = details))
        val snapshot = RecordFixtures.snapshot(diagnostics = diagnostics)
        val record = RecordFixtures.record().copy(snapshot = snapshot)
        value(repository.insert(record))
        details.clear()
        diagnostics.clear()
        val restored = assertNotNull(value(repository.findById(record.id)))
        assertEquals("original", restored.snapshot.diagnostics.single().details["k"])
        assertFailsWith<UnsupportedOperationException> { (restored.snapshot.diagnostics as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (restored.snapshot.diagnostics.single().details as MutableMap).clear() }
        val page = value(repository.findPage(CalculationHistoryQuery()))
        assertFailsWith<UnsupportedOperationException> { (page.records as MutableList).clear() }
        assertEquals(record, value(repository.findById(record.id)))
    }

    @Test fun `contract 32 simultaneous inserts of one ID publish exactly one complete candidate`() {
        val repository = createRepository()
        val candidates = (0 until 32).map { RecordFixtures.record(content = "candidate-$it") }
        val ready = CountDownLatch(32)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(32)
        try {
            val futures = candidates.map { candidate -> executor.submit(Callable {
                ready.countDown()
                check(start.await(30, TimeUnit.SECONDS))
                candidate to repository.insert(candidate)
            }) }
            assertTrue(ready.await(30, TimeUnit.SECONDS))
            start.countDown()
            val results = futures.map { it.get(30, TimeUnit.SECONDS) }
            val winners = results.filter { it.second is RepositoryResult.Success }
            assertEquals(1, winners.size)
            val losers = results.filter { it.second is RepositoryResult.Failure }
            assertEquals(31, losers.size)
            losers.forEach { assertEquals(RepositoryError.CONFLICT, (it.second as RepositoryResult.Failure).error) }
            val stored = value(repository.findById(candidates.first().id))
            assertEquals(winners.single().first, stored)
            assertTrue(candidates.contains(stored))
            assertEquals(listOf(stored), value(repository.findPage(CalculationHistoryQuery())).records)
        } finally {
            start.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS))
        }
    }

    @Test fun `contract distinct concurrent inserts and reads lose no records after completion`() {
        val repository = createRepository()
        val start = CountDownLatch(1)
        val ready = CountDownLatch(16)
        val executor = Executors.newFixedThreadPool(16)
        val candidates = (0 until 96).map { RecordFixtures.record("record-$it", content = "candidate-$it") }
        try {
            val futures = candidates.map { candidate -> executor.submit(Callable {
                ready.countDown()
                check(start.await(30, TimeUnit.SECONDS))
                value(repository.insert(candidate))
                assertEquals(candidate, value(repository.findById(candidate.id)))
                val page = value(repository.findPage(CalculationHistoryQuery(7)))
                assertTrue(page.records.size <= 7)
                assertEquals(page.records.size, page.records.map { it.id }.toSet().size)
            }) }
            assertTrue(ready.await(30, TimeUnit.SECONDS))
            start.countDown()
            futures.forEach { it.get(30, TimeUnit.SECONDS) }
            candidates.forEach { assertEquals(it, value(repository.findById(it.id))) }
            val page = value(repository.findPage(CalculationHistoryQuery(100)))
            assertEquals(candidates.toSet(), page.records.toSet())
            assertFalse(page.hasMore)
        } finally {
            start.countDown()
            executor.shutdownNow()
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS))
        }
    }
}

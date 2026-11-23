package com.thermora.infrastructure.persistence.memory

import com.thermora.application.ports.record.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryCalculationRecordRepository :
    CalculationRecordRepository {

    private val records =
        ConcurrentHashMap<CalculationRecordId, CalculationRecord>()

    override fun insert(
        record: CalculationRecord
    ): RepositoryResult<Unit> {
        val previous = records.putIfAbsent(record.id, record)

        return if (previous == null) {
            RepositoryResult.Success(Unit)
        } else {
            RepositoryResult.Failure(RepositoryError.CONFLICT)
        }
    }

    override fun findById(
        id: CalculationRecordId
    ): RepositoryResult<CalculationRecord?> =
        RepositoryResult.Success(records[id])

    override fun findPage(
        query: CalculationHistoryQuery
    ): RepositoryResult<CalculationHistoryPage> {
        val sorted = records.values.sortedWith(
            compareByDescending<CalculationRecord> { it.createdAt }
                .thenByDescending { it.id.value }
        )

        val page = sorted
            .drop(query.offset.coerceAtMost(sorted.size.toLong()).toInt())
            .take(query.limit)

        val hasMore =
            query.offset + page.size.toLong() < sorted.size.toLong()

        return RepositoryResult.Success(
            CalculationHistoryPage(
                records = page,
                hasMore = hasMore
            )
        )
    }
}

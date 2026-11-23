package com.thermora.application.ports.record

interface CalculationRecordRepository {

    fun insert(
        record: CalculationRecord
    ): RepositoryResult<Unit>

    fun findById(
        id: CalculationRecordId
    ): RepositoryResult<CalculationRecord?>

    fun findPage(
        query: CalculationHistoryQuery
    ): RepositoryResult<CalculationHistoryPage>
}

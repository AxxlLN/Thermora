package com.thermora.infrastructure.persistence.memory

import com.thermora.application.ports.testing.CalculationRecordRepositoryContract

class InMemoryRepositoryContractTest : CalculationRecordRepositoryContract() {
    override fun createRepository() = InMemoryCalculationRecordRepository()
}

package com.thermora.application.ports.record

data class CalculationHistoryQuery(
    val limit: Int = 50,
    val offset: Long = 0
) {
    init {
        require(limit in 1..100) {
            "History page limit must be between 1 and 100"
        }

        require(offset >= 0) {
            "History page offset must not be negative"
        }
    }
}

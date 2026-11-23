package com.thermora.application.ports.record

import java.util.UUID

@JvmInline
value class CalculationRecordId(
    val value: String
) {
    init {
        require(value.isNotBlank()) {
            "Calculation record ID must not be blank"
        }

        require(value.length <= 128) {
            "Calculation record ID must not exceed 128 characters"
        }
    }

    companion object {
        fun random(): CalculationRecordId =
            CalculationRecordId(UUID.randomUUID().toString())
    }
}
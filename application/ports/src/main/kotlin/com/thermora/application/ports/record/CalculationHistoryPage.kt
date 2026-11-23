package com.thermora.application.ports.record

import java.util.Collections

class CalculationHistoryPage(
    records: List<CalculationRecord>,
    val hasMore: Boolean
) {
    val records: List<CalculationRecord> =
        Collections.unmodifiableList(ArrayList(records))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CalculationHistoryPage) return false
        return records == other.records && hasMore == other.hasMore
    }

    override fun hashCode(): Int = 31 * records.hashCode() + hasMore.hashCode()

    override fun toString(): String = "CalculationHistoryPage(records=$records, hasMore=$hasMore)"
}

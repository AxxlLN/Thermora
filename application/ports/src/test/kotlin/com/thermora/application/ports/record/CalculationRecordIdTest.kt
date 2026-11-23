package com.thermora.application.ports.record

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class CalculationRecordIdTest {

    @Test
    fun `valid ID is accepted`() {
        val id = CalculationRecordId("calculation-001")

        assertEquals("calculation-001", id.value)
    }

    @Test
    fun `blank ID is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CalculationRecordId("   ")
        }
    }

    @Test
    fun `oversized ID is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CalculationRecordId("a".repeat(129))
        }
    }

    @Test
    fun `generated IDs are unique`() {
        val first = CalculationRecordId.random()
        val second = CalculationRecordId.random()

        assertNotEquals(first, second)
    }
}
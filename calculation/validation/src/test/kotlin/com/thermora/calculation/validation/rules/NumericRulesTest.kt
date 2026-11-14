package com.thermora.calculation.validation.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericRulesTest {

    private data class Input(val value: Double)

    @Test
    fun `finite rule accepts finite values`() {
        val rule = FiniteNumberRule<Input>("value") { it.value }

        assertTrue(rule.validate(Input(0.0)).isValid)
        assertTrue(rule.validate(Input(-100.0)).isValid)
    }

    @Test
    fun `finite rule rejects NaN and infinity`() {
        val rule = FiniteNumberRule<Input>("value") { it.value }

        for (value in listOf(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )) {
            val report = rule.validate(Input(value))

            assertFalse(report.isValid)
            assertEquals("NON_FINITE_NUMBER", report.diagnostics.single().code)
        }
    }

    @Test
    fun `positive rule accepts only finite positive values`() {
        val rule = PositiveNumberRule<Input>("value") { it.value }

        assertTrue(rule.validate(Input(0.001)).isValid)

        for (value in listOf(
            0.0,
            -1.0,
            Double.NaN,
            Double.POSITIVE_INFINITY
        )) {
            assertFalse(rule.validate(Input(value)).isValid)
        }
    }

    @Test
    fun `range rule accepts inclusive boundaries`() {
        val rule = RangeRule<Input>("value", 0.0, 100.0) { it.value }

        assertTrue(rule.validate(Input(0.0)).isValid)
        assertTrue(rule.validate(Input(50.0)).isValid)
        assertTrue(rule.validate(Input(100.0)).isValid)
    }

    @Test
    fun `range rule rejects values outside boundaries`() {
        val rule = RangeRule<Input>("value", 0.0, 100.0) { it.value }

        for (value in listOf(
            -0.001,
            100.001,
            Double.NaN,
            Double.POSITIVE_INFINITY
        )) {
            assertFalse(rule.validate(Input(value)).isValid)
        }
    }

    @Test
    fun `range rule rejects invalid configuration`() {
        assertFailsWith<IllegalArgumentException> {
            RangeRule<Input>("value", 100.0, 0.0) { it.value }
        }

        assertFailsWith<IllegalArgumentException> {
            RangeRule<Input>("value", Double.NaN, 10.0) { it.value }
        }
    }
}

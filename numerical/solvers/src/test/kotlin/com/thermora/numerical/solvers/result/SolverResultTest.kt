package com.thermora.numerical.solvers.result

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class SolverResultTest {
    @Test
    fun `constructor and getter isolate DoubleArray snapshots`() {
        val source = doubleArrayOf(1.0, 2.0)
        val result = SolverResult(source, SolverStatus.CONVERGED, 1, 0.0, 0.0)
        source[0] = 100.0
        val first = assertNotNull(result.value)
        first[1] = 200.0
        assertContentEquals(doubleArrayOf(1.0, 2.0), result.value)
        assertNotSame(first, result.value)
    }

    @Test
    fun `copy and explicit replacement cannot share caller mutable arrays`() {
        val original = SolverResult(doubleArrayOf(1.0), SolverStatus.CONVERGED)
        val copied = original.copy()
        val replacement = doubleArrayOf(2.0)
        val replaced = copied.copy(value = replacement, iterations = 2)
        replacement[0] = 100.0
        assertNotNull(copied.value)[0] = 200.0
        assertContentEquals(doubleArrayOf(1.0), original.value)
        assertContentEquals(doubleArrayOf(1.0), copied.value)
        assertContentEquals(doubleArrayOf(2.0), replaced.value)
        assertEquals(2, replaced.iterations)
    }

    @Test
    fun `destructuring returns an isolated snapshot and preserves component order`() {
        val result = SolverResult(doubleArrayOf(1.0), SolverStatus.MAX_ITERATIONS, 3, 0.5, 1.0)
        val (value, status, iterations, residual, step) = result
        assertNotNull(value)[0] = 100.0
        assertContentEquals(doubleArrayOf(1.0), result.value)
        assertEquals(SolverStatus.MAX_ITERATIONS, status)
        assertEquals(3, iterations)
        assertEquals(0.5, residual)
        assertEquals(1.0, step)
    }

    @Test
    fun `DoubleArray equality and hash remain stable after caller mutations`() {
        val left = SolverResult(doubleArrayOf(1.0), SolverStatus.CONVERGED)
        val right = SolverResult(doubleArrayOf(1.0), SolverStatus.CONVERGED)
        val hash = left.hashCode()
        assertNotNull(left.value)[0] = 100.0
        assertEquals(left, right)
        assertEquals(hash, left.hashCode())
        assertEquals(left.hashCode(), right.hashCode())
        assertNotEquals(left, right.copy(value = doubleArrayOf(2.0)))
        assertNotEquals(left, right.copy(status = SolverStatus.MAX_ITERATIONS))
    }

    @Test
    fun `null and scalar copy retain value semantics`() {
        val missing = SolverResult<Double>(null, SolverStatus.INVALID_INPUT)
        assertNull(missing.copy().value)
        assertEquals(missing, missing.copy())
        val scalar = SolverResult(1.0, SolverStatus.CONVERGED, 2, 0.0, 0.0)
        assertEquals(scalar, scalar.copy())
        assertEquals(2.0, scalar.copy(value = 2.0).value)
        assertEquals("SolverResult(value=1.0, status=CONVERGED, iterations=2, residualNorm=0.0, stepNorm=0.0)", scalar.toString())
    }

    @Test
    fun `DoubleArray content comparison distinguishes signed zero`() {
        val positive = SolverResult(doubleArrayOf(0.0), SolverStatus.CONVERGED)
        val negative = positive.copy(value = doubleArrayOf(-0.0))
        assertNotEquals(positive, negative)
    }

    @Test
    fun `nullable Double metadata preserves boxed equality and hash semantics`() {
        val positive = SolverResult(1.0, SolverStatus.CONVERGED, residualNorm = 0.0, stepNorm = 0.0)
        assertNotEquals(positive, positive.copy(residualNorm = -0.0))
        assertNotEquals(positive, positive.copy(stepNorm = -0.0))
        val nan = positive.copy(residualNorm = Double.NaN, stepNorm = Double.NaN)
        assertEquals(nan, nan.copy())
        assertEquals(nan.hashCode(), nan.copy().hashCode())
    }
}

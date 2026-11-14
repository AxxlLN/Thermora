package com.thermora.numerical.solvers.options

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IterativeSolverOptionsTest {
    @Test
    fun `invalid tolerances fail fast with IllegalArgumentException`() {
        for (invalid in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { IterativeSolverOptions(absoluteTolerance = invalid) }
            assertFailsWith<IllegalArgumentException> { IterativeSolverOptions(relativeTolerance = invalid) }
        }
        assertFailsWith<IllegalArgumentException> { IterativeSolverOptions(absoluteTolerance = 0.0, relativeTolerance = 0.0) }
    }

    @Test
    fun `invalid iteration limits fail fast with IllegalArgumentException`() {
        for (invalid in listOf(0, -1, Int.MIN_VALUE)) {
            assertFailsWith<IllegalArgumentException> { IterativeSolverOptions(maxIterations = invalid) }
        }
    }

    @Test
    fun `invalid relaxation factors fail fast with IllegalArgumentException`() {
        for (invalid in listOf(0.0, -0.0, -1.0, Math.nextUp(1.0), Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { IterativeSolverOptions(relaxationFactor = invalid) }
        }
    }

    @Test
    fun `valid boundaries are retained without changing their binary64 values`() {
        val options = IterativeSolverOptions(0.0, Double.MIN_VALUE, 1, Double.MIN_VALUE)
        assertEquals(Double.MIN_VALUE.toRawBits(), options.relativeTolerance.toRawBits())
        assertEquals(Double.MIN_VALUE.toRawBits(), options.relaxationFactor.toRawBits())
        assertEquals(1.0, options.copy(relaxationFactor = 1.0).relaxationFactor)
        assertEquals(Double.MAX_VALUE, options.copy(absoluteTolerance = Double.MAX_VALUE).absoluteTolerance)
    }

    @Test
    fun `data class copy revalidates options`() {
        val options = IterativeSolverOptions()
        assertFailsWith<IllegalArgumentException> { options.copy(maxIterations = 0) }
        assertFailsWith<IllegalArgumentException> { options.copy(relaxationFactor = 0.0) }
    }
}

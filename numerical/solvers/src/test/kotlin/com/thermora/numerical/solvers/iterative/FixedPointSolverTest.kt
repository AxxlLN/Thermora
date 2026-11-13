package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverStatus
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.math.abs

class FixedPointSolverTest {

    @Test
    fun `converges to fixed point`() {
        val result = FixedPointSolver.solve(
            initial = 1.0,
            update = ::cos
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertNotNull(result.value)
        assertEquals(0.7390851332, result.value, 1e-7)
    }

    @Test
    fun `converges with relaxation`() {
        val result = FixedPointSolver.solve(
            initial = 0.0,
            update = { 2.0 - it },
            options = IterativeSolverOptions(
                relaxationFactor = 0.5
            )
        )

        assertTrue(result.isConverged)
        assertEquals(1.0, result.value)
    }

    @Test
    fun `stops after iteration limit`() {
        val result = FixedPointSolver.solve(
            initial = 0.0,
            update = { it + 1.0 },
            options = IterativeSolverOptions(
                maxIterations = 10
            )
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(10, result.iterations)
    }

    @Test
    fun `detects non finite update`() {
        val result = FixedPointSolver.solve(
            initial = 1.0,
            update = { Double.NaN }
        )

        assertEquals(SolverStatus.NON_FINITE, result.status)
    }

    @Test
    fun `rejects invalid initial value`() {
        val result = FixedPointSolver.solve(
            initial = Double.POSITIVE_INFINITY,
            update = { it }
        )

        assertEquals(SolverStatus.INVALID_INPUT, result.status)
    }

    @Test
    fun `already at fixed point`() {
        val result = FixedPointSolver.solve(
            initial = 5.0,
            update = { it }
        )

        assertTrue(result.isConverged)
        assertEquals(5.0, result.value)
        assertEquals(0.0, result.residualNorm)
    }

    @Test
    fun `residual corresponds to returned value`() {
        val result = FixedPointSolver.solve(
            initial = 0.0,
            update = { 0.5 * it + 1.0 },
            options = IterativeSolverOptions(
                maxIterations = 3
            )
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)

        val value = assertNotNull(result.value)
        val residual = assertNotNull(result.residualNorm)

        assertEquals(
            abs(0.5 * value + 1.0 - value),
            residual,
            1e-12
        )
    }
}
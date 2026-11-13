package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VectorFixedPointSolverTest {

    @Test
    fun `converges for coupled variables`() {
        val result = VectorFixedPointSolver.solve(
            initial = doubleArrayOf(0.0, 0.0),
            update = { x ->
                doubleArrayOf(
                    0.5 * x[1] + 1.0,
                    0.25 * x[0] + 2.0
                )
            }
        )

        assertEquals(SolverStatus.CONVERGED, result.status)

        val value = assertNotNull(result.value)

        assertEquals(16.0 / 7.0, value[0], 1e-6)
        assertEquals(18.0 / 7.0, value[1], 1e-6)
    }

    @Test
    fun `converges with relaxation`() {
        val result = VectorFixedPointSolver.solve(
            initial = doubleArrayOf(0.0, 0.0),
            update = { x ->
                doubleArrayOf(
                    2.0 - x[0],
                    4.0 - x[1]
                )
            },
            options = IterativeSolverOptions(
                relaxationFactor = 0.5
            )
        )

        assertTrue(result.isConverged)

        val value = assertNotNull(result.value)

        assertEquals(1.0, value[0], 1e-10)
        assertEquals(2.0, value[1], 1e-10)
    }

    @Test
    fun `rejects invalid scales`() {
        val result = VectorFixedPointSolver.solve(
            initial = doubleArrayOf(1.0, 2.0),
            update = { it },
            scales = doubleArrayOf(1.0, 0.0)
        )

        assertEquals(SolverStatus.INVALID_INPUT, result.status)
    }

    @Test
    fun `detects invalid update dimension`() {
        val result = VectorFixedPointSolver.solve(
            initial = doubleArrayOf(1.0, 2.0),
            update = { doubleArrayOf(1.0) }
        )

        assertEquals(SolverStatus.NON_FINITE, result.status)
    }

    @Test
    fun `stops at iteration limit`() {
        val result = VectorFixedPointSolver.solve(
            initial = doubleArrayOf(0.0, 0.0),
            update = { x ->
                doubleArrayOf(x[0] + 1.0, x[1] + 1.0)
            },
            options = IterativeSolverOptions(
                maxIterations = 5
            )
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(5, result.iterations)
    }
}
package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverStatus
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal
import kotlin.test.assertNull
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

    @Test
    fun `NS-04 overflow cannot satisfy convergence comparison`() {
        val result = FixedPointSolver.solve(
            initial = 0.0,
            update = { x -> if (x == 0.0) 1e308 else -1e308 },
            options = IterativeSolverOptions(relativeTolerance = 1.9, maxIterations = 1)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(1e308, result.value)
        assertNull(result.residualNorm)
        assertEquals(1e308, result.stepNorm)
    }

    @Test
    fun `NS-05 opposite large values have a finite relaxed midpoint`() {
        val result = FixedPointSolver.solve(
            initial = 1e308,
            update = { -1e308 },
            options = IterativeSolverOptions(maxIterations = 1, relaxationFactor = 0.5)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(0.0, result.value)
        assertEquals(1e308, result.residualNorm)
        assertEquals(1e308, result.stepNorm)
    }

    @Test
    fun `rounded tolerance must not accept a step above the exact tolerance`() {
        val result = FixedPointSolver.solve(
            0.0,
            { 1.0 },
            IterativeSolverOptions(
                absoluteTolerance = Math.nextDown(1.0),
                relativeTolerance = Math.scalb(1.0, -54),
                maxIterations = 1
            )
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(0.0, result.residualNorm)
        assertEquals(1.0, result.stepNorm)
    }

    @Test
    fun `exact comparison can establish convergence when the tolerance exceeds Double range`() {
        val result = FixedPointSolver.solve(
            0.0,
            { x -> if (x == 0.0) 1e308 else -1e308 },
            IterativeSolverOptions(relativeTolerance = 2.1, maxIterations = 1)
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertEquals(1e308, result.value)
        assertNull(result.residualNorm)
        assertEquals(1e308, result.stepNorm)
        assertTrue(
            BigDecimal(1e308).multiply(BigDecimal(2)) <
                BigDecimal(2.1).multiply(BigDecimal(1e308))
        )
    }

    @Test
    fun `opposite maximum values can converge through a midpoint`() {
        val result = FixedPointSolver.solve(
            Double.MAX_VALUE,
            { -it },
            IterativeSolverOptions(maxIterations = 2, relaxationFactor = 0.5)
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertEquals(0.0, result.value)
        assertEquals(0.0, result.residualNorm)
        assertEquals(0.0, result.stepNorm)
        assertEquals(2, result.iterations)
    }

    @Test
    fun `unit relaxation accepts the target without overflowing the intermediate difference`() {
        val result = FixedPointSolver.solve(
            Double.MAX_VALUE,
            { -Double.MAX_VALUE },
            IterativeSolverOptions(maxIterations = 1)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(-Double.MAX_VALUE, result.value)
        assertEquals(0.0, result.residualNorm)
        assertNull(result.stepNorm)
        assertEquals(1, result.iterations)
    }

    @Test
    fun `an unrepresentable previous step does not stop subsequent convergence`() {
        val result = FixedPointSolver.solve(
            Double.MAX_VALUE,
            { -Double.MAX_VALUE },
            IterativeSolverOptions(maxIterations = 2)
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertEquals(-Double.MAX_VALUE, result.value)
        assertEquals(0.0, result.stepNorm)
        assertEquals(0.0, result.residualNorm)
        assertEquals(2, result.iterations)
    }

    @Test
    fun `subnormal midpoint preserves the exact tolerance boundary`() {
        val result = FixedPointSolver.solve(
            -Double.MIN_VALUE,
            { Double.MIN_VALUE },
            IterativeSolverOptions(
                absoluteTolerance = Double.MIN_VALUE,
                relativeTolerance = 0.0,
                maxIterations = 1,
                relaxationFactor = 0.5
            )
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertEquals(0.0, result.value)
        assertEquals(Double.MIN_VALUE, result.stepNorm)
        assertEquals(Double.MIN_VALUE, result.residualNorm)
    }

    @Test
    fun `relaxation rounds the complete affine step instead of an underflowed product`() {
        val result = FixedPointSolver.solve(
            Double.MIN_VALUE,
            { 3.0 * Double.MIN_VALUE },
            IterativeSolverOptions(
                absoluteTolerance = 0.0,
                relativeTolerance = 1e-8,
                maxIterations = 1,
                relaxationFactor = 0.25
            )
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(2.0 * Double.MIN_VALUE, result.value)
        assertEquals(Double.MIN_VALUE, result.stepNorm)
        assertEquals(Double.MIN_VALUE, result.residualNorm)
    }

    @Test
    fun `tiny relaxation does not confuse a rounded zero step with a small residual`() {
        val result = FixedPointSolver.solve(
            1.0,
            { it + 1.0 },
            IterativeSolverOptions(maxIterations = 3, relaxationFactor = Double.MIN_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(1.0, result.value)
        assertEquals(0.0, result.stepNorm)
        assertEquals(1.0, result.residualNorm)
        assertEquals(3, result.iterations)
    }

    @Test
    fun `tiny relaxation and a maximum target retain a representable step`() {
        val result = FixedPointSolver.solve(
            0.0,
            { Double.MAX_VALUE },
            IterativeSolverOptions(maxIterations = 1, relaxationFactor = Double.MIN_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(Double.MAX_VALUE * Double.MIN_VALUE, result.value)
        assertEquals(result.value, result.stepNorm)
        assertEquals(Double.MAX_VALUE, result.residualNorm)
    }

    @Test
    fun `rejects every non finite initial without evaluating the callback`() {
        for (initial in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            val result = FixedPointSolver.solve(initial, { error("must not be evaluated") })
            assertEquals(SolverStatus.INVALID_INPUT, result.status)
            assertNull(result.value)
            assertNull(result.residualNorm)
            assertNull(result.stepNorm)
            assertEquals(0, result.iterations)
        }
    }

    @Test
    fun `non finite first target preserves the initial state`() {
        for (target in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            val result = FixedPointSolver.solve(1.0, { target })
            assertEquals(SolverStatus.NON_FINITE, result.status)
            assertEquals(1.0, result.value)
            assertNull(result.residualNorm)
            assertNull(result.stepNorm)
            assertEquals(0, result.iterations)
        }
    }

    @Test
    fun `non finite evaluation at a new point preserves its step and omits its residual`() {
        for (invalid in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            val result = FixedPointSolver.solve(0.0, { if (it == 0.0) 1.0 else invalid })
            assertEquals(SolverStatus.NON_FINITE, result.status)
            assertEquals(1.0, result.value)
            assertNull(result.residualNorm)
            assertEquals(1.0, result.stepNorm)
            assertEquals(1, result.iterations)
        }
    }

    @Test
    fun `returned residual and last accepted step refer to the final relaxed point`() {
        val result = FixedPointSolver.solve(
            0.0,
            { 0.5 * it + 1.0 },
            IterativeSolverOptions(maxIterations = 2, relaxationFactor = 0.5)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(0.875, result.value)
        assertEquals(0.5625, result.residualNorm)
        assertEquals(0.375, result.stepNorm)
    }

    @Test
    fun `callback exceptions continue to propagate`() {
        val failure = IllegalStateException("callback")
        val actual = kotlin.test.assertFailsWith<IllegalStateException> {
            FixedPointSolver.solve(1.0, { throw failure })
        }
        kotlin.test.assertSame(failure, actual)
    }
}

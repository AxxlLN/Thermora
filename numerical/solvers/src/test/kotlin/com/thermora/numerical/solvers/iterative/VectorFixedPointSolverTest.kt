package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverStatus
import kotlin.math.cos
import kotlin.test.assertContentEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

    @Test
    fun `NS-04 overflowing normalized metrics cannot imply convergence`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(0.0),
            { doubleArrayOf(1.0) },
            IterativeSolverOptions(maxIterations = 1, relaxationFactor = 0.5),
            doubleArrayOf(Double.MIN_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(0.5, assertNotNull(result.value)[0])
        assertNull(result.residualNorm)
        assertNull(result.stepNorm)
    }

    @Test
    fun `NS-05 opposite large states have a finite relaxed midpoint`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(1e308),
            { doubleArrayOf(-1e308) },
            IterativeSolverOptions(maxIterations = 1, relaxationFactor = 0.5)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(0.0, assertNotNull(result.value)[0])
        assertEquals(1e308, result.residualNorm)
        assertEquals(1e308, result.stepNorm)
    }

    @Test
    fun `NS-06 callback mutation cannot change the validated scales snapshot`() {
        val scales = doubleArrayOf(1.0)
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(0.0),
            { x -> scales[0] = Double.POSITIVE_INFINITY; doubleArrayOf(x[0] + 1.0) },
            IterativeSolverOptions(maxIterations = 1),
            scales
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(1.0, assertNotNull(result.value)[0])
        assertEquals(1.0, result.residualNorm)
        assertEquals(1.0, result.stepNorm)
    }

    @Test
    fun `normalization occurs before converting an overflowing raw step to Double`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(-Double.MAX_VALUE),
            { doubleArrayOf(Double.MAX_VALUE) },
            IterativeSolverOptions(maxIterations = 1),
            doubleArrayOf(Double.MAX_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(Double.MAX_VALUE, assertNotNull(result.value)[0])
        assertEquals(2.0, result.stepNorm)
        assertEquals(0.0, result.residualNorm)
    }

    @Test
    fun `normalization recovers a finite residual from an overflowing raw difference`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(0.0),
            { x -> doubleArrayOf(if (x[0] == 0.0) Double.MAX_VALUE else -Double.MAX_VALUE) },
            IterativeSolverOptions(maxIterations = 1),
            doubleArrayOf(Double.MAX_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(Double.MAX_VALUE, assertNotNull(result.value)[0])
        assertEquals(1.0, result.stepNorm)
        assertEquals(2.0, result.residualNorm)
    }

    @Test
    fun `underflowed normalized diagnostics do not imply convergence`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(0.0),
            { doubleArrayOf(Double.MIN_VALUE) },
            IterativeSolverOptions(absoluteTolerance = 0.0, relativeTolerance = 1e-8, maxIterations = 1),
            doubleArrayOf(Double.MAX_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(Double.MIN_VALUE, assertNotNull(result.value)[0])
        assertEquals(0.0, result.stepNorm)
        assertEquals(0.0, result.residualNorm)
    }

    @Test
    fun `maximum normalized metrics refer to the returned point and its last step`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(0.0, 0.0),
            { x -> doubleArrayOf(0.125*x[0] + 4.0, 0.5*x[1] + 6.0) },
            IterativeSolverOptions(maxIterations = 1),
            doubleArrayOf(2.0, 3.0)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertContentEquals(doubleArrayOf(4.0, 6.0), result.value)
        assertEquals(2.0, result.stepNorm)
        assertEquals(1.0, result.residualNorm)
    }

    @Test
    fun `each component uses its own absolute tolerance scale`() {
        for ((scale, expectedStatus) in listOf(4.0 to SolverStatus.CONVERGED, 3.0 to SolverStatus.MAX_ITERATIONS)) {
            val result = VectorFixedPointSolver.solve(
                doubleArrayOf(0.0, 0.0),
                { doubleArrayOf(2.0, 8.0) },
                IterativeSolverOptions(
                    absoluteTolerance = 1.0,
                    relativeTolerance = 0.0,
                    relaxationFactor = 0.5,
                    maxIterations = 1
                ),
                doubleArrayOf(1.0, scale)
            )

            assertEquals(expectedStatus, result.status)
            assertContentEquals(doubleArrayOf(1.0, 4.0), result.value)
        }
    }

    @Test
    fun `relative criterion is independent of scale even when normalized metrics overflow`() {
        for (scale in listOf(1.0, Double.MIN_VALUE, Double.MAX_VALUE)) {
            val result = VectorFixedPointSolver.solve(
                doubleArrayOf(0.0),
                { doubleArrayOf(1.0) },
                IterativeSolverOptions(
                    absoluteTolerance = 0.0,
                    relativeTolerance = 0.5,
                    relaxationFactor = 0.5,
                    maxIterations = 1
                ),
                doubleArrayOf(scale)
            )

            assertEquals(SolverStatus.CONVERGED, result.status)
            assertEquals(0.5, assertNotNull(result.value)[0])
            val expectedNorm = (0.5 / scale).takeIf { it.isFinite() }
            assertEquals(expectedNorm, result.residualNorm)
            assertEquals(expectedNorm, result.stepNorm)
        }
    }

    @Test
    fun `rounded normalized tolerance cannot accept a step above the exact boundary`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(0.0),
            { doubleArrayOf(1.0) },
            IterativeSolverOptions(
                absoluteTolerance = Math.nextDown(1.0),
                relativeTolerance = Math.scalb(1.0, -54),
                maxIterations = 1
            )
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(1.0, result.stepNorm)
        assertEquals(0.0, result.residualNorm)
    }

    @Test
    fun `opposite maximum vectors converge through a finite midpoint`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(Double.MAX_VALUE, -Double.MAX_VALUE),
            { x -> doubleArrayOf(-x[0], -x[1]) },
            IterativeSolverOptions(relaxationFactor = 0.5, maxIterations = 2)
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertContentEquals(doubleArrayOf(0.0, 0.0), result.value)
        assertEquals(0.0, result.residualNorm)
        assertEquals(0.0, result.stepNorm)
        assertEquals(2, result.iterations)
    }

    @Test
    fun `unrepresentable first step does not prevent later convergence`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(Double.MAX_VALUE),
            { doubleArrayOf(-Double.MAX_VALUE) },
            IterativeSolverOptions(maxIterations = 2)
        )

        assertEquals(SolverStatus.CONVERGED, result.status)
        assertEquals(-Double.MAX_VALUE, assertNotNull(result.value)[0])
        assertEquals(0.0, result.residualNorm)
        assertEquals(0.0, result.stepNorm)
        assertEquals(2, result.iterations)
    }

    @Test
    fun `subnormal relaxed step is rounded only after the affine calculation`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(Double.MIN_VALUE),
            { doubleArrayOf(3.0 * Double.MIN_VALUE) },
            IterativeSolverOptions(
                absoluteTolerance = 0.0,
                relativeTolerance = 1e-8,
                maxIterations = 1,
                relaxationFactor = 0.25
            ),
            doubleArrayOf(Double.MIN_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(2.0 * Double.MIN_VALUE, assertNotNull(result.value)[0])
        assertEquals(1.0, result.residualNorm)
        assertEquals(1.0, result.stepNorm)
    }

    @Test
    fun `tiny relaxation cannot replace a residual check with a zero step check`() {
        val result = VectorFixedPointSolver.solve(
            doubleArrayOf(1.0),
            { x -> doubleArrayOf(x[0] + 1.0) },
            IterativeSolverOptions(maxIterations = 3, relaxationFactor = Double.MIN_VALUE)
        )

        assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
        assertEquals(1.0, assertNotNull(result.value)[0])
        assertEquals(1.0, result.residualNorm)
        assertEquals(0.0, result.stepNorm)
    }

    @Test
    fun `all invalid initial states and scales are rejected before callback`() {
        for (initial in listOf(doubleArrayOf(), doubleArrayOf(Double.NaN), doubleArrayOf(Double.POSITIVE_INFINITY), doubleArrayOf(Double.NEGATIVE_INFINITY))) {
            val result = VectorFixedPointSolver.solve(initial, { error("must not be evaluated") })
            assertEquals(SolverStatus.INVALID_INPUT, result.status)
            assertNull(result.value)
        }
        for (scales in listOf(
            doubleArrayOf(), doubleArrayOf(1.0, 1.0), doubleArrayOf(0.0), doubleArrayOf(-0.0),
            doubleArrayOf(-1.0), doubleArrayOf(Double.NaN), doubleArrayOf(Double.POSITIVE_INFINITY),
            doubleArrayOf(Double.NEGATIVE_INFINITY)
        )) {
            val result = VectorFixedPointSolver.solve(doubleArrayOf(1.0), { error("must not be evaluated") }, scales = scales)
            assertEquals(SolverStatus.INVALID_INPUT, result.status)
            assertNull(result.value)
            assertNull(result.residualNorm)
            assertNull(result.stepNorm)
        }
    }

    @Test
    fun `non finite callback components are rejected at either evaluation stage`() {
        for (invalid in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            val first = VectorFixedPointSolver.solve(doubleArrayOf(0.0, 0.0), { doubleArrayOf(1.0, invalid) })
            assertEquals(SolverStatus.NON_FINITE, first.status)
            assertContentEquals(doubleArrayOf(0.0, 0.0), first.value)
            assertEquals(0, first.iterations)
            assertNull(first.residualNorm)
            assertNull(first.stepNorm)

            val second = VectorFixedPointSolver.solve(
                doubleArrayOf(0.0, 0.0),
                { x -> if (x[0] == 0.0) doubleArrayOf(1.0, 2.0) else doubleArrayOf(1.0, invalid) }
            )
            assertEquals(SolverStatus.NON_FINITE, second.status)
            assertContentEquals(doubleArrayOf(1.0, 2.0), second.value)
            assertEquals(1, second.iterations)
            assertNull(second.residualNorm)
            assertEquals(2.0, second.stepNorm)
        }
    }

    @Test
    fun `callback dimension errors retain existing status at both evaluation stages`() {
        for (wrong in listOf(doubleArrayOf(), doubleArrayOf(1.0), doubleArrayOf(1.0, 2.0, 3.0))) {
            val first = VectorFixedPointSolver.solve(doubleArrayOf(0.0, 0.0), { wrong })
            assertEquals(SolverStatus.NON_FINITE, first.status)
            assertEquals(0, first.iterations)
            assertContentEquals(doubleArrayOf(0.0, 0.0), first.value)

            val second = VectorFixedPointSolver.solve(
                doubleArrayOf(0.0, 0.0),
                { x -> if (x[0] == 0.0) doubleArrayOf(1.0, 2.0) else wrong }
            )
            assertEquals(SolverStatus.NON_FINITE, second.status)
            assertEquals(1, second.iterations)
            assertContentEquals(doubleArrayOf(1.0, 2.0), second.value)
            assertNull(second.residualNorm)
            assertEquals(2.0, second.stepNorm)
        }
    }

    @Test
    fun `scales mutation to zero tiny or NaN cannot affect norms or convergence`() {
        for (replacement in listOf(0.0, Double.MIN_VALUE, Double.NaN)) {
            val scales = doubleArrayOf(2.0)
            val result = VectorFixedPointSolver.solve(
                doubleArrayOf(0.0),
                { x -> scales[0] = replacement; doubleArrayOf(x[0] + 2.0) },
                IterativeSolverOptions(maxIterations = 2),
                scales
            )
            assertEquals(SolverStatus.MAX_ITERATIONS, result.status)
            assertEquals(4.0, assertNotNull(result.value)[0])
            assertEquals(1.0, result.residualNorm)
            assertEquals(1.0, result.stepNorm)
        }
    }

    @Test
    fun `callback receives a defensive copy of the initial state`() {
        val initial = doubleArrayOf(0.0, 0.0)
        val result = VectorFixedPointSolver.solve(initial, { x -> x[0] = 1.0; x[1] = 2.0; x })
        assertEquals(SolverStatus.CONVERGED, result.status)
        assertContentEquals(doubleArrayOf(1.0, 2.0), result.value)
        assertContentEquals(doubleArrayOf(0.0, 0.0), initial)
    }

    @Test
    fun `one dimensional unit scale matches scalar solver across extreme and ordinary cases`() {
        data class Case(val initial: Double, val update: (Double) -> Double, val options: IterativeSolverOptions)
        val cases = listOf(
            Case(1.0, ::cos, IterativeSolverOptions()),
            Case(0.0, { 2.0 - it }, IterativeSolverOptions(relaxationFactor = 0.5)),
            Case(0.0, { it + 1.0 }, IterativeSolverOptions(maxIterations = 3)),
            Case(0.0, { if (it == 0.0) 1e308 else -1e308 }, IterativeSolverOptions(relativeTolerance = 1.9, maxIterations = 1)),
            Case(0.0, { if (it == 0.0) 1e308 else -1e308 }, IterativeSolverOptions(relativeTolerance = 2.1, maxIterations = 1)),
            Case(Double.MAX_VALUE, { -it }, IterativeSolverOptions(relaxationFactor = 0.5, maxIterations = 2)),
            Case(Double.MAX_VALUE, { -Double.MAX_VALUE }, IterativeSolverOptions(maxIterations = 1)),
            Case(Double.MIN_VALUE, { 3.0 * Double.MIN_VALUE }, IterativeSolverOptions(relaxationFactor = 0.25, maxIterations = 1)),
            Case(1.0, { it + 1.0 }, IterativeSolverOptions(relaxationFactor = Double.MIN_VALUE, maxIterations = 3)),
            Case(1.0, { Double.NaN }, IterativeSolverOptions()),
            Case(0.0, { if (it == 0.0) 1.0 else Double.POSITIVE_INFINITY }, IterativeSolverOptions())
        )
        for (case in cases) {
            val scalar = FixedPointSolver.solve(case.initial, case.update, case.options)
            val vector = VectorFixedPointSolver.solve(doubleArrayOf(case.initial), { doubleArrayOf(case.update(it[0])) }, case.options)
            assertEquals(scalar.status, vector.status)
            assertEquals(scalar.value, assertNotNull(vector.value)[0])
            assertEquals(scalar.iterations, vector.iterations)
            assertEquals(scalar.residualNorm, vector.residualNorm)
            assertEquals(scalar.stepNorm, vector.stepNorm)
        }
    }

    @Test
    fun `callback exceptions continue to propagate`() {
        val failure = IllegalStateException("callback")
        val actual = kotlin.test.assertFailsWith<IllegalStateException> {
            VectorFixedPointSolver.solve(doubleArrayOf(1.0), { throw failure })
        }
        kotlin.test.assertSame(failure, actual)
    }
}

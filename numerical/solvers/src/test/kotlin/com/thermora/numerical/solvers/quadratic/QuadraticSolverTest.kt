package com.thermora.numerical.solvers.quadratic

import com.thermora.numerical.solvers.result.SolverStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.math.abs
import kotlin.test.assertIs
import kotlin.test.assertTrue

class QuadraticSolverTest {

    @Test
    fun `two real roots`() {
        val result = QuadraticSolver.solve(1.0, -5.0, 6.0)

        assertEquals(SolverStatus.CONVERGED, result.status)

        val roots = assertIs<QuadraticRoots.Two>(result.value)

        assertEquals(2.0, roots.first, 1e-10)
        assertEquals(3.0, roots.second, 1e-10)
    }

    @Test
    fun `double root`() {
        val result = QuadraticSolver.solve(1.0, -2.0, 1.0)

        val roots = assertIs<QuadraticRoots.One>(result.value)

        assertEquals(1.0, roots.value, 1e-10)
    }

    @Test
    fun `no real roots`() {
        val result = QuadraticSolver.solve(1.0, 0.0, 1.0)

        assertIs<QuadraticRoots.None>(result.value)
    }

    @Test
    fun `linear equation`() {
        val result = QuadraticSolver.solve(0.0, 2.0, -4.0)

        val roots = assertIs<QuadraticRoots.One>(result.value)

        assertEquals(2.0, roots.value, 1e-10)
    }

    @Test
    fun `infinite solutions`() {
        val result = QuadraticSolver.solve(0.0, 0.0, 0.0)

        assertIs<QuadraticRoots.Infinite>(result.value)
    }

    @Test
    fun `invalid coefficients`() {
        val result = QuadraticSolver.solve(
            Double.NaN, 1.0, 2.0
        )

        assertEquals(SolverStatus.INVALID_INPUT, result.status)
    }

    @Test
    fun `numerically stable roots`() {
        val result = QuadraticSolver.solve(
            1.0, -1e8, 1.0
        )

        val roots = assertIs<QuadraticRoots.Two>(result.value)

        assertEquals(1e-8, roots.first, 1e-12)
        assertEquals(1e8, roots.second, 1e-6)
    }

    @Test
    fun `excel perforated housing reference`() {
        val result = QuadraticSolver.solve(
            a = 0.0508495,
            b = -5.4044107,
            c = 141.96320226282694
        )

        val roots = assertIs<QuadraticRoots.Two>(result.value)

        assertEquals(47.470234762074924, roots.first, 1e-6)
        assertEquals(58.812239991855776, roots.second, 1e-6)
        assertTrue(result.isConverged)
    }

    @Test
    fun `NS-01 overflow never produces a successful non finite root`() {
        for ((a, b, c) in listOf(
            Triple(0.0, Double.MIN_VALUE, 1.0),
            Triple(Double.MIN_VALUE, 1e308, 1.0),
            // Exact D=0, but the repeated root is 2^1024.
            Triple(Double.MIN_VALUE, -Math.scalb(1.0, -49), Math.scalb(1.0, 974))
        )) {
            val result = QuadraticSolver.solve(a, b, c)
            assertEquals(SolverStatus.NON_FINITE, result.status)
            assertNull(result.value)
        }
    }

    @Test
    fun `NS-01 and NS-02 extreme opposite coefficients retain real root structure`() {
        assertPair(1e-200, 0.0, -1e200, -1e200, 1e200)
        assertNone(1e-200, 0.0, 1e200)
    }

    @Test
    fun `NS-02 squared tiny b does not erase the zero root`() {
        assertPair(1.0, 1e-200, 0.0, -1e-200, 0.0)
    }

    @Test
    fun `NS-02 shared normalization must not erase c`() {
        assertPair(1e200, 0.0, -1e-200, -1e-200, 1e-200)
        assertNone(1e200, 0.0, 1e-200)
        assertPair(1.0, 1e-200, -1e-200, -1e-100, 1e-100)
        assertNone(1.0, 1e-200, 1e-200)
    }

    @Test
    fun `NS-03 exact binary64 discriminant sign survives cancellation`() {
        // (1 + 2^-52) * (1 - 2^-53) > 1: D is strictly negative.
        assertNone(Math.nextUp(1.0), 2.0, Math.nextDown(1.0))
        // (1 + 2^-52) * (1 - 2^-52) < 1: D is strictly positive.
        val roots = assertIs<QuadraticRoots.Two>(
            QuadraticSolver.solve(Math.nextUp(1.0), 2.0, 1.0 - Math.ulp(1.0)).value
        )
        assertEquals(-1.0, roots.first)
        assertEquals(-0.9999999999999996, roots.second)
    }

    @Test
    fun `zero coefficients classify degree exactly`() {
        assertNone(0.0, 0.0, 1.0)
        assertPair(1.0, -2.0, 0.0, 0.0, 2.0)
        assertPair(1.0, 2.0, 0.0, -2.0, 0.0)
        assertPair(1.0, 0.0, -4.0, -2.0, 2.0)
        assertNone(1.0, 0.0, 4.0)
        val zero = assertIs<QuadraticRoots.One>(QuadraticSolver.solve(1.0, 0.0, 0.0).value)
        assertEquals(0L, zero.value.toRawBits())
    }

    @Test
    fun `signed zeros are mathematical zeros and returned zeros are canonical`() {
        for (az in listOf(0.0, -0.0)) {
            for (bz in listOf(0.0, -0.0)) {
                for (cz in listOf(0.0, -0.0)) {
                    assertIs<QuadraticRoots.Infinite>(QuadraticSolver.solve(az, bz, cz).value)
                    val root = assertIs<QuadraticRoots.One>(QuadraticSolver.solve(1.0, bz, cz).value)
                    assertEquals(0L, root.value.toRawBits())
                    val linear = assertIs<QuadraticRoots.One>(QuadraticSolver.solve(az, 1.0, cz).value)
                    assertEquals(0L, linear.value.toRawBits())
                }
            }
        }
        assertPair(1.0, -0.0, -4.0, -2.0, 2.0)
    }

    @Test
    fun `subnormal coefficients and roots are retained`() {
        val tiny = Double.MIN_VALUE
        assertPair(tiny, 0.0, -tiny, -1.0, 1.0)
        assertPair(1.0, -tiny, 0.0, 0.0, tiny)
        val repeated = assertIs<QuadraticRoots.One>(QuadraticSolver.solve(tiny, -2*tiny, tiny).value)
        assertEquals(1.0, repeated.value)
        val linear = assertIs<QuadraticRoots.One>(QuadraticSolver.solve(0.0, 1.0, -tiny).value)
        assertEquals(tiny, linear.value)
        // The mathematical linear root is smaller than the binary64 range: rounds to zero.
        val underflow = assertIs<QuadraticRoots.One>(QuadraticSolver.solve(0.0, Double.MAX_VALUE, tiny).value)
        assertEquals(0L, underflow.value.toRawBits())
    }

    @Test
    fun `large coefficients do not overflow discriminant or q`() {
        assertPair(1e308, -1e308, 1e307, 0.1127016653792583, 0.8872983346207417)
        assertPair(Double.MAX_VALUE, -Double.MAX_VALUE, 0.0, 0.0, 1.0)
        val repeated = assertIs<QuadraticRoots.One>(
            QuadraticSolver.solve(Double.MAX_VALUE / 4, -Double.MAX_VALUE / 2, Double.MAX_VALUE / 4).value
        )
        assertEquals(1.0, repeated.value)
    }

    @Test
    fun `all coefficient positions reject NaN and infinities`() {
        for (invalid in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            for ((a,b,c) in listOf(Triple(invalid,1.0,1.0), Triple(1.0,invalid,1.0), Triple(1.0,1.0,invalid))) {
                val result = QuadraticSolver.solve(a,b,c)
                assertEquals(SolverStatus.INVALID_INPUT, result.status)
                assertNull(result.value)
            }
        }
    }

    @Test
    fun `power of two exponent corpus has correctly classified roots`() {
        // Independent oracle: a*x^2 - c = 0 with an even exponent difference.
        val exponents = listOf(-1074, -1022, -800, -400, 0, 400, 800, 1022)
        for (ae in exponents) {
            for (ce in exponents) {
                val a = Math.scalb(1.0, ae)
                val magnitudeC = Math.scalb(1.0, ce)
                val root = Math.scalb(1.0, (ce - ae) / 2)
                val result = QuadraticSolver.solve(a, 0.0, -magnitudeC)
                if (root.isFinite()) {
                    assertPair(a, 0.0, -magnitudeC, -root, root)
                } else {
                    assertEquals(SolverStatus.NON_FINITE, result.status)
                    assertNull(result.value)
                }
                assertNone(a, 0.0, magnitudeC)
            }
        }
    }

    @Test
    fun `exact power of two rescaling preserves roots`() {
        for (exponent in listOf(-1074, -1000, -500, 0, 500, 1000)) {
            for (sign in listOf(-1.0, 1.0)) {
                val scale = sign * Math.scalb(1.0, exponent)
                assertPair(scale, -5.0*scale, 6.0*scale, 2.0, 3.0)
            }
        }
    }

    private fun assertNone(a: Double, b: Double, c: Double) {
        val result = QuadraticSolver.solve(a, b, c)
        assertEquals(SolverStatus.CONVERGED, result.status)
        assertIs<QuadraticRoots.None>(result.value)
    }

    private fun assertPair(a: Double, b: Double, c: Double, first: Double, second: Double) {
        val result = QuadraticSolver.solve(a, b, c)
        assertEquals(SolverStatus.CONVERGED, result.status, "coefficients: $a, $b, $c")
        val roots = assertIs<QuadraticRoots.Two>(result.value)
        for ((expected, actual) in listOf(first to roots.first, second to roots.second)) {
            assertTrue(actual.isFinite())
            assertTrue(abs(expected - actual) <= 4.0*Math.ulp(expected), "expected $expected, actual $actual")
        }
    }
}

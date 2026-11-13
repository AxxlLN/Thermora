package com.thermora.numerical.solvers.quadratic

import com.thermora.numerical.solvers.result.SolverResult
import com.thermora.numerical.solvers.result.SolverStatus
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Solves the real polynomial whose coefficients are the exact supplied binary64 values.
 * Signed coefficient zeros are mathematical zeros; returned zero roots are canonical +0.0.
 * Degree and discriminant sign are classified exactly, without an epsilon. None therefore
 * means no real roots, never an arithmetic failure. Two describes two distinct mathematical
 * roots even when both round to the same Double.
 *
 * Roots are binary64 approximations, not guaranteed correctly rounded. The stable q formula
 * avoids subtractive cancellation. An exact decimal discriminant prevents both cancellation
 * and exponent loss; extreme root calculations use 80 significant decimal digits. Conversion
 * may underflow to zero (the nearest representable approximation). If either root overflows
 * to infinity, the entire result is NON_FINITE, without a partial solution.
 *
 * BigDecimal(Double), rather than valueOf(Double), preserves the exact binary64 coefficient.
 * Decimal arithmetic is part of the JDK; no external numerical dependency is required.
 */
object QuadraticSolver {
    private val rootContext = MathContext(80, RoundingMode.HALF_EVEN)
    private val two = BigDecimal(2)
    private val four = BigDecimal(4)

    fun solve(a: Double, b: Double, c: Double): SolverResult<QuadraticRoots> {
        if (!a.isFinite() || !b.isFinite() || !c.isFinite()) {
            return SolverResult(value = null, status = SolverStatus.INVALID_INPUT)
        }

        if (a == 0.0) {
            return when {
                b != 0.0 -> one(BigDecimal(c).negate().divide(BigDecimal(b), rootContext).toDouble())
                c == 0.0 -> success(QuadraticRoots.Infinite)
                else -> success(QuadraticRoots.None)
            }
        }

        val ad = BigDecimal(a)
        val bd = BigDecimal(b)
        val cd = BigDecimal(c)
        // No MathContext here: even the sign of an almost-cancelled discriminant is exact.
        val discriminant = bd.multiply(bd).subtract(four.multiply(ad).multiply(cd))
        if (discriminant.signum() < 0) return success(QuadraticRoots.None)
        if (discriminant.signum() == 0) {
            return one(bd.negate().divide(two.multiply(ad), rootContext).toDouble())
        }

        val d = discriminant.toDouble()
        if (d.isFinite() && d >= java.lang.Double.MIN_NORMAL) {
            val sqrtD = sqrt(d)
            // Halve before adding: b + sign(b)*sqrt(D) itself could overflow.
            val q = -0.5 * b - 0.5 * Math.copySign(sqrtD, b)
            val first = q / a
            val second = c / q
            if (abs(q) >= java.lang.Double.MIN_NORMAL && first.isFinite() && second.isFinite()) {
                return pair(first, second)
            }
        }

        // No shared normalization: it could erase a small but essential coefficient.
        // q has no cancellation, so 80 digits suffice for highly accurate binary64 estimates
        // even with thousands of binary exponents between the input coefficients.
        val sqrtD = discriminant.sqrt(rootContext)
        val magnitude = bd.abs().add(sqrtD, rootContext).divide(two, rootContext)
        val q = if (b < 0.0) magnitude else magnitude.negate()
        return pair(
            q.divide(ad, rootContext).toDouble(),
            cd.divide(q, rootContext).toDouble()
        )
    }

    private fun one(root: Double): SolverResult<QuadraticRoots> =
        if (root.isFinite()) success(QuadraticRoots.One(canonicalZero(root))) else nonFinite()

    private fun pair(first: Double, second: Double): SolverResult<QuadraticRoots> =
        if (first.isFinite() && second.isFinite()) {
            success(QuadraticRoots.Two(
                first = canonicalZero(minOf(first, second)),
                second = canonicalZero(maxOf(first, second))
            ))
        } else {
            nonFinite()
        }

    private fun canonicalZero(value: Double): Double = if (value == 0.0) 0.0 else value

    private fun nonFinite() = SolverResult<QuadraticRoots>(
        value = null,
        status = SolverStatus.NON_FINITE
    )

    private fun success(roots: QuadraticRoots) = SolverResult(
        value = roots,
        status = SolverStatus.CONVERGED,
        iterations = 0
    )
}

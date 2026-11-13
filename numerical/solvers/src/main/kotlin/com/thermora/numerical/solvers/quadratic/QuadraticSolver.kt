package com.thermora.numerical.solvers.quadratic

import com.thermora.numerical.solvers.result.SolverResult
import com.thermora.numerical.solvers.result.SolverStatus
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.sqrt

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
        val discriminant = bd.multiply(bd).subtract(four.multiply(ad).multiply(cd))
        if (discriminant.signum() < 0) return success(QuadraticRoots.None)
        if (discriminant.signum() == 0) {
            return one(bd.negate().divide(two.multiply(ad), rootContext).toDouble())
        }

        val d = discriminant.toDouble()
        if (d.isFinite() && d >= java.lang.Double.MIN_NORMAL) {
            val sqrtD = sqrt(d)
            val q = -0.5 * b - 0.5 * Math.copySign(sqrtD, b)
            val first = q / a
            val second = c / q
            if (abs(q) >= java.lang.Double.MIN_NORMAL && first.isFinite() && second.isFinite()) {
                return pair(first, second)
            }
        }

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

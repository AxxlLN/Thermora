package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverResult
import com.thermora.numerical.solvers.result.SolverStatus
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max

object FixedPointSolver {

    fun solve(
        initial: Double,
        update: (Double) -> Double,
        options: IterativeSolverOptions = IterativeSolverOptions()
    ): SolverResult<Double> {
        if (!initial.isFinite()) {
            return SolverResult(
                value = null,
                status = SolverStatus.INVALID_INPUT
            )
        }

        val absoluteTolerance = BigDecimal(options.absoluteTolerance)
        val relativeTolerance = BigDecimal(options.relativeTolerance)
        val relaxationFactor = BigDecimal(options.relaxationFactor)
        var current = initial
        var residualNorm: Double? = null
        var stepNorm: Double? = null
        var target = update(current)
        if (!target.isFinite()) {
            return SolverResult(value = current, status = SolverStatus.NON_FINITE)
        }

        repeat(options.maxIterations) { index ->

            val previous = BigDecimal(current)
            val next = when {
                options.relaxationFactor == 1.0 -> target
                current == target -> current
                else -> previous.add(
                    BigDecimal(target).subtract(previous).multiply(relaxationFactor)
                ).toDouble()
            }
            val state = BigDecimal(next)
            val step = state.subtract(previous).abs()
            stepNorm = step.toDouble().takeIf { it.isFinite() }
            val unchanged = next.toRawBits() == current.toRawBits()
            current = next

            val nextTarget = if (unchanged) target else update(current)

            if (!nextTarget.isFinite()) {
                return SolverResult(
                    value = current,
                    status = SolverStatus.NON_FINITE,
                    iterations = index + 1,
                    stepNorm = stepNorm
                )
            }

            target = nextTarget
            val residual = BigDecimal(nextTarget).subtract(state).abs()
            residualNorm = residual.toDouble().takeIf { it.isFinite() }
            val tolerance = absoluteTolerance.add(
                relativeTolerance.multiply(BigDecimal(max(abs(current), abs(nextTarget))))
            )

            if (residual <= tolerance && step <= tolerance) {
                return SolverResult(
                    value = current,
                    status = SolverStatus.CONVERGED,
                    iterations = index + 1,
                    residualNorm = residualNorm,
                    stepNorm = stepNorm
                )
            }
        }

        return SolverResult(
            value = current,
            status = SolverStatus.MAX_ITERATIONS,
            iterations = options.maxIterations,
            residualNorm = residualNorm,
            stepNorm = stepNorm
        )
    }
}

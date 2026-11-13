package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverResult
import com.thermora.numerical.solvers.result.SolverStatus
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

        var current = initial
        var residual = Double.POSITIVE_INFINITY
        var step = Double.POSITIVE_INFINITY

        repeat(options.maxIterations) { index ->

            val target = update(current)

            if (!target.isFinite()) {
                return SolverResult(
                    value = current,
                    status = SolverStatus.NON_FINITE,
                    iterations = index,
                    residualNorm = residual.takeIf { it.isFinite() },
                    stepNorm = step.takeIf { it.isFinite() }
                )
            }

            val next = current +
                    options.relaxationFactor * (target - current)

            if (!next.isFinite()) {
                return SolverResult(
                    value = current,
                    status = SolverStatus.NON_FINITE,
                    iterations = index + 1
                )
            }

            step = abs(next - current)
            current = next

            val nextTarget = update(current)

            if (!nextTarget.isFinite()) {
                return SolverResult(
                    value = current,
                    status = SolverStatus.NON_FINITE,
                    iterations = index + 1,
                    stepNorm = step
                )
            }

            residual = abs(nextTarget - current)

            val tolerance =
                options.absoluteTolerance +
                        options.relativeTolerance * max(
                    abs(current),
                    abs(nextTarget)
                )

            if (residual <= tolerance && step <= tolerance) {
                return SolverResult(
                    value = current,
                    status = SolverStatus.CONVERGED,
                    iterations = index + 1,
                    residualNorm = residual,
                    stepNorm = step
                )
            }
        }

            return SolverResult(
                value = current,
                status = SolverStatus.MAX_ITERATIONS,
                iterations = options.maxIterations,
                residualNorm = residual.takeIf { it.isFinite() },
                stepNorm = step.takeIf { it.isFinite() }
            )
        }
    }
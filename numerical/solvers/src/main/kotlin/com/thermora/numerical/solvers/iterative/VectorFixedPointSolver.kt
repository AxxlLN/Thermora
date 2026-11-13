package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverResult
import com.thermora.numerical.solvers.result.SolverStatus
import kotlin.math.abs
import kotlin.math.max

object VectorFixedPointSolver {

    fun solve(
        initial: DoubleArray,
        update: (DoubleArray) -> DoubleArray,
        options: IterativeSolverOptions = IterativeSolverOptions(),
        scales: DoubleArray = DoubleArray(initial.size) { 1.0 }
    ): SolverResult<DoubleArray> {

        if (
            initial.isEmpty() ||
            initial.any { !it.isFinite() } ||
            scales.size != initial.size ||
            scales.any { !it.isFinite() || it <= 0.0 }
        ) {
            return SolverResult(
                value = null,
                status = SolverStatus.INVALID_INPUT
            )
        }

        var current = initial.copyOf()
        var residualNorm: Double? = null
        var stepNorm: Double? = null

        fun evaluate(state: DoubleArray): DoubleArray? {
            val result = update(state.copyOf())

            return result.takeIf {
                it.size == state.size && it.all(Double::isFinite)
            }
        }

        repeat(options.maxIterations) { index ->

            val target = evaluate(current)
                ?: return SolverResult(
                    value = current.copyOf(),
                    status = SolverStatus.NON_FINITE,
                    iterations = index,
                    residualNorm = residualNorm,
                    stepNorm = stepNorm
                )

            val next = DoubleArray(current.size) { i ->
                current[i] +
                        options.relaxationFactor *
                        (target[i] - current[i])
            }

            if (next.any { !it.isFinite() }) {
                return SolverResult(
                    value = current.copyOf(),
                    status = SolverStatus.NON_FINITE,
                    iterations = index + 1
                )
            }

            val steps = DoubleArray(current.size) { i ->
                abs(next[i] - current[i]) / scales[i]
            }

            stepNorm = steps.maxOrNull() ?: 0.0
            current = next

            val nextTarget = evaluate(current)
                ?: return SolverResult(
                    value = current.copyOf(),
                    status = SolverStatus.NON_FINITE,
                    iterations = index + 1,
                    stepNorm = stepNorm
                )

            val residuals = DoubleArray(current.size) { i ->
                abs(nextTarget[i] - current[i]) / scales[i]
            }

            residualNorm = residuals.maxOrNull() ?: 0.0

            val converged = current.indices.all { i ->
                val tolerance =
                    options.absoluteTolerance +
                            options.relativeTolerance *
                            max(
                                abs(current[i]),
                                abs(nextTarget[i])
                            ) / scales[i]

                residuals[i] <= tolerance &&
                        steps[i] <= tolerance
            }

            if (converged) {
                return SolverResult(
                    value = current.copyOf(),
                    status = SolverStatus.CONVERGED,
                    iterations = index + 1,
                    residualNorm = residualNorm,
                    stepNorm = stepNorm
                )
            }
        }

        return SolverResult(
            value = current.copyOf(),
            status = SolverStatus.MAX_ITERATIONS,
            iterations = options.maxIterations,
            residualNorm = residualNorm,
            stepNorm = stepNorm
        )
    }
}
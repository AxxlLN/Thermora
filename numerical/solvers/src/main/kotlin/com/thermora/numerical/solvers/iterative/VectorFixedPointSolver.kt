package com.thermora.numerical.solvers.iterative

import com.thermora.numerical.solvers.options.IterativeSolverOptions
import com.thermora.numerical.solvers.result.SolverResult
import com.thermora.numerical.solvers.result.SolverStatus
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max

object VectorFixedPointSolver {

    fun solve(
        initial: DoubleArray,
        update: (DoubleArray) -> DoubleArray,
        options: IterativeSolverOptions = IterativeSolverOptions(),
        scales: DoubleArray = DoubleArray(initial.size) { 1.0 }
    ): SolverResult<DoubleArray> {
        val scaleSnapshot = scales.copyOf()
        if (
            initial.isEmpty() ||
            initial.any { !it.isFinite() } ||
            scaleSnapshot.size != initial.size ||
            scaleSnapshot.any { !it.isFinite() || it <= 0.0 }
        ) {
            return SolverResult(
                value = null,
                status = SolverStatus.INVALID_INPUT
            )
        }

        val decimalScales = Array(scaleSnapshot.size) { BigDecimal(scaleSnapshot[it]) }
        val absoluteTolerance = BigDecimal(options.absoluteTolerance)
        val relativeTolerance = BigDecimal(options.relativeTolerance)
        val relaxationFactor = BigDecimal(options.relaxationFactor)
        var current = initial.copyOf()
        var residualNorm: Double? = null
        var stepNorm: Double? = null

        fun evaluate(state: DoubleArray): DoubleArray = update(state.copyOf()).copyOf()

        fun failure(target: DoubleArray): SolverStatus? = when {
            target.size != current.size -> SolverStatus.INVALID_DIMENSION
            target.any { !it.isFinite() } -> SolverStatus.NON_FINITE
            else -> null
        }

        var target = evaluate(current)
        val initialFailure = failure(target)
        if (initialFailure != null) {
            return SolverResult(value = current, status = initialFailure)
        }

        repeat(options.maxIterations) { index ->
            val previous = Array(current.size) { BigDecimal(current[it]) }
            val next = DoubleArray(current.size) { i ->
                when {
                    options.relaxationFactor == 1.0 -> target[i]
                    current[i] == target[i] -> current[i]
                    else -> previous[i].add(
                        BigDecimal(target[i]).subtract(previous[i]).multiply(relaxationFactor)
                    ).toDouble()
                }
            }
            val state = Array(next.size) { BigDecimal(next[it]) }
            val steps = Array(next.size) { state[it].subtract(previous[it]).abs() }
            stepNorm = normalizedNorm(steps, decimalScales)
            val unchanged = next.contentEquals(current)
            current = next

            val nextTarget = if (unchanged) target else evaluate(current)
            val evaluationFailure = failure(nextTarget)
            if (evaluationFailure != null) {
                return SolverResult(
                    value = current,
                    status = evaluationFailure,
                    iterations = index + 1,
                    stepNorm = stepNorm
                )
            }
            target = nextTarget

            val residuals = Array(current.size) { BigDecimal(nextTarget[it]).subtract(state[it]).abs() }
            residualNorm = normalizedNorm(residuals, decimalScales)

            val converged = current.indices.all { i ->
                val tolerance = absoluteTolerance.multiply(decimalScales[i]).add(
                    relativeTolerance.multiply(BigDecimal(max(abs(current[i]), abs(nextTarget[i]))))
                )
                residuals[i] <= tolerance && steps[i] <= tolerance
            }

            if (converged) {
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

    private fun normalizedNorm(values: Array<BigDecimal>, scales: Array<BigDecimal>): Double? {
        var largest = 0
        for (i in 1 until values.size) {
            if (values[i].multiply(scales[largest]) > values[largest].multiply(scales[i])) {
                largest = i
            }
        }
        val norm = if (scales[largest].compareTo(BigDecimal.ONE) == 0) {
            values[largest]
        } else {
            values[largest].divide(scales[largest], 1075, RoundingMode.HALF_EVEN)
        }
        return norm.toDouble().takeIf { it.isFinite() }
    }
}

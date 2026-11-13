package com.thermora.numerical.solvers.result

data class SolverResult<T>(
    val value: T?,
    val status: SolverStatus,
    val iterations: Int = 0,
    val residualNorm: Double? = null,
    val stepNorm: Double? = null
) {
    val isConverged: Boolean
        get() = status == SolverStatus.CONVERGED
}
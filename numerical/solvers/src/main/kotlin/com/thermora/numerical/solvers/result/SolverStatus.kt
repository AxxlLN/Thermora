package com.thermora.numerical.solvers.result

enum class SolverStatus {
    CONVERGED,
    MAX_ITERATIONS,
    NON_FINITE,
    STAGNATED,
    DIVERGED,
    INVALID_INPUT,
    INVALID_DIMENSION
}
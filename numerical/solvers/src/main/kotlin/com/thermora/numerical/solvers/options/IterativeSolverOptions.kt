package com.thermora.numerical.solvers.options

data class IterativeSolverOptions(
    val absoluteTolerance: Double = 1e-8,
    val relativeTolerance: Double = 1e-8,
    val maxIterations: Int = 1000,
    val relaxationFactor: Double = 1.0
) {
    init {
        require(absoluteTolerance.isFinite() && absoluteTolerance >= 0.0)
        require(relativeTolerance.isFinite() && relativeTolerance >= 0.0)
        require(absoluteTolerance > 0.0 || relativeTolerance > 0.0)
        require(maxIterations > 0)
        require(relaxationFactor.isFinite() && relaxationFactor > 0.0)
        require(relaxationFactor <= 1.0)
    }
}
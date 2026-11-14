package com.thermora.numerical.solvers.result

import java.util.Objects

class SolverResult<T>(
    value: T?,
    val status: SolverStatus,
    val iterations: Int = 0,
    val residualNorm: Double? = null,
    val stepNorm: Double? = null
) {
    private val storedValue: T? = snapshot(value)

    val value: T?
        get() = snapshot(storedValue)

    val isConverged: Boolean
        get() = status == SolverStatus.CONVERGED

    operator fun component1(): T? = value
    operator fun component2(): SolverStatus = status
    operator fun component3(): Int = iterations
    operator fun component4(): Double? = residualNorm
    operator fun component5(): Double? = stepNorm

    fun copy(
        value: T? = this.value,
        status: SolverStatus = this.status,
        iterations: Int = this.iterations,
        residualNorm: Double? = this.residualNorm,
        stepNorm: Double? = this.stepNorm
    ): SolverResult<T> = SolverResult(value, status, iterations, residualNorm, stepNorm)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SolverResult<*>) return false
        val sameValue = if (storedValue is DoubleArray && other.storedValue is DoubleArray) {
            storedValue.contentEquals(other.storedValue)
        } else {
            storedValue == other.storedValue
        }
        return sameValue && status == other.status && iterations == other.iterations &&
                Objects.equals(residualNorm, other.residualNorm) && Objects.equals(stepNorm, other.stepNorm)
    }

    override fun hashCode(): Int {
        var result = if (storedValue is DoubleArray) storedValue.contentHashCode() else storedValue?.hashCode() ?: 0
        result = 31 * result + status.hashCode()
        result = 31 * result + iterations
        result = 31 * result + (residualNorm?.hashCode() ?: 0)
        result = 31 * result + (stepNorm?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        val renderedValue = if (storedValue is DoubleArray) storedValue.contentToString() else storedValue
        return "SolverResult(value=$renderedValue, status=$status, iterations=$iterations, " +
                "residualNorm=$residualNorm, stepNorm=$stepNorm)"
    }

    @Suppress("UNCHECKED_CAST")
    private fun snapshot(value: T?): T? = if (value is DoubleArray) value.copyOf() as T else value
}

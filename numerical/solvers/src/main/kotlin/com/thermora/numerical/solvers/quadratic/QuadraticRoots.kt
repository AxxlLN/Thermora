package com.thermora.numerical.solvers.quadratic

sealed interface QuadraticRoots {

    data class Two(
        val first: Double,
        val second: Double
    ) : QuadraticRoots

    data class One(
        val value: Double
    ) : QuadraticRoots

    data object None : QuadraticRoots

    data object Infinite : QuadraticRoots
}
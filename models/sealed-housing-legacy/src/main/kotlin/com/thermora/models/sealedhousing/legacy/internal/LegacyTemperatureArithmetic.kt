package com.thermora.models.sealedhousing.legacy.internal

internal object LegacyTemperatureArithmetic {

    fun mean(first: Double, second: Double): Double {
        require(first.isFinite() && second.isFinite()) {
            "Mean arguments must be finite"
        }

        val sum = first + second
        return if (sum.isFinite()) sum / 2.0 else first / 2.0 + second / 2.0
    }
}

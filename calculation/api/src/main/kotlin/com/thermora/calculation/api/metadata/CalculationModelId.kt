package com.thermora.calculation.api.metadata

@JvmInline
value class CalculationModelId(
    val value: String
) {
    init {
        require(value.isNotBlank()) {
            "Calculation model ID cannot be blank"
        }

        require(value.matches(Regex("[a-z0-9][a-z0-9._-]*"))) {
            "Invalid calculation model ID: $value"
        }
    }
}
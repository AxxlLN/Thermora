package com.thermora.calculation.api.metadata

data class CalculationModelDescriptor(
    val id: CalculationModelId,
    val name: String,
    val version: String,
    val description: String? = null
) {
    init {
        require(name.isNotBlank()) {
            "Calculation model name cannot be blank"
        }

        require(version.isNotBlank()) {
            "Calculation model version cannot be blank"
        }
    }
}
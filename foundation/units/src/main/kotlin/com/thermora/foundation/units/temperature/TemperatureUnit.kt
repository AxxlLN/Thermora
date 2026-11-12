package com.thermora.foundation.units.temperature

enum class TemperatureUnit(
    val symbol: String
) {
    KELVIN("K"),
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun toKelvin(value: Double): Double {
        require(value.isFinite()) {
            "Temperature must be finite"
        }

        val result = when (this) {
            KELVIN -> value
            CELSIUS -> value + 273.15
            FAHRENHEIT -> (value + 459.67) * 5.0 / 9.0
        }

        require(result.isFinite()) {
            "Temperature conversion overflow"
        }

        return result
    }

    fun fromKelvin(kelvin: Double): Double {
        require(kelvin.isFinite()) {
            "Temperature must be finite"
        }

        val result = when (this) {
            KELVIN -> kelvin
            CELSIUS -> kelvin - 273.15
            FAHRENHEIT -> kelvin * 9.0 / 5.0 - 459.67
        }

        require(result.isFinite()) {
            "Temperature conversion overflow"
        }

        return result
    }

    fun deltaToKelvin(value: Double): Double {
        require(value.isFinite()) {
            "Temperature difference must be finite"
        }

        val result = when (this) {
            KELVIN, CELSIUS -> value
            FAHRENHEIT -> value * 5.0 / 9.0
        }

        require(result.isFinite()) {
            "Temperature difference conversion overflow"
        }

        return result
    }

    fun deltaFromKelvin(kelvin: Double): Double {
        require(kelvin.isFinite()) {
            "Temperature difference must be finite"
        }

        val result = when (this) {
            KELVIN, CELSIUS -> kelvin
            FAHRENHEIT -> kelvin * 9.0 / 5.0
        }

        require(result.isFinite()) {
            "Temperature difference conversion overflow"
        }

        return result
    }
}
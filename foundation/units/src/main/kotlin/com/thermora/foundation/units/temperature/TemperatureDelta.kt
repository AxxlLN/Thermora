package com.thermora.foundation.units.temperature

data class TemperatureDelta private constructor(
    val kelvin: Double
) {

    init {
        require(kelvin.isFinite()) {
            "Temperature difference must be finite"
        }
    }

    fun inUnit(unit: TemperatureUnit): Double =
        unit.deltaFromKelvin(kelvin)

    operator fun plus(other: TemperatureDelta): TemperatureDelta =
        fromKelvin(kelvin + other.kelvin)

    operator fun minus(other: TemperatureDelta): TemperatureDelta =
        fromKelvin(kelvin - other.kelvin)

    operator fun unaryMinus(): TemperatureDelta =
        fromKelvin(-kelvin)

    companion object {

        fun of(
            value: Double,
            unit: TemperatureUnit
        ): TemperatureDelta {
            return fromKelvin(unit.deltaToKelvin(value))
        }

        fun fromKelvin(kelvin: Double): TemperatureDelta =
            TemperatureDelta(kelvin)
    }
}
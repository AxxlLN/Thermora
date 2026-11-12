package com.thermora.foundation.units.temperature

class Temperature private constructor(
    val kelvin: Double
) {

    init {
        require(kelvin.isFinite()) {
            "Temperature must be finite"
        }

        require(kelvin >= 0.0) {
            "Temperature cannot be below absolute zero"
        }
    }

    fun inUnit(unit: TemperatureUnit): Double =
        unit.fromKelvin(kelvin)

    operator fun plus(delta: TemperatureDelta): Temperature =
        fromKelvin(kelvin + delta.kelvin)

    operator fun minus(delta: TemperatureDelta): Temperature =
        fromKelvin(kelvin - delta.kelvin)

    operator fun minus(other: Temperature): TemperatureDelta =
        TemperatureDelta.fromKelvin(kelvin - other.kelvin)

    companion object {

        fun of(
            value: Double,
            unit: TemperatureUnit
        ): Temperature {
            return fromKelvin(unit.toKelvin(value))
        }

        fun fromKelvin(kelvin: Double): Temperature =
            Temperature(kelvin)
    }
}
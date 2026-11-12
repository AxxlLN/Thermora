package com.thermora.foundation.units.dimension

data class PhysicalDimension(
    val length: Int = 0,
    val mass: Int = 0,
    val time: Int = 0,
    val electricCurrent: Int = 0,
    val thermodynamicTemperature: Int = 0,
    val amountOfSubstance: Int = 0,
    val luminousIntensity: Int = 0
) {
    operator fun times(other: PhysicalDimension) =
        PhysicalDimension(
            Math.addExact(length, other.length),
            Math.addExact(mass, other.mass),
            Math.addExact(time, other.time),
            Math.addExact(electricCurrent, other.electricCurrent),
            Math.addExact(thermodynamicTemperature, other.thermodynamicTemperature),
            Math.addExact(amountOfSubstance, other.amountOfSubstance),
            Math.addExact(luminousIntensity, other.luminousIntensity)
        )

    operator fun div(other: PhysicalDimension) =
        PhysicalDimension(
            Math.subtractExact(length, other.length),
            Math.subtractExact(mass, other.mass),
            Math.subtractExact(time, other.time),
            Math.subtractExact(electricCurrent, other.electricCurrent),
            Math.subtractExact(thermodynamicTemperature, other.thermodynamicTemperature),
            Math.subtractExact(amountOfSubstance, other.amountOfSubstance),
            Math.subtractExact(luminousIntensity, other.luminousIntensity)
        )

    companion object {
        val DIMENSIONLESS = PhysicalDimension()
        val LENGTH = PhysicalDimension(length = 1)
        val MASS = PhysicalDimension(mass = 1)
        val TIME = PhysicalDimension(time = 1)
        val TEMPERATURE = PhysicalDimension(thermodynamicTemperature = 1)

        val AREA = LENGTH * LENGTH
        val VOLUME = AREA * LENGTH
        val VELOCITY = LENGTH / TIME

        val POWER = MASS * AREA / TIME / TIME / TIME

        val ENERGY = POWER * TIME

        val DENSITY = MASS / VOLUME

        val THERMAL_CONDUCTIVITY = POWER / LENGTH / TEMPERATURE

        val SPECIFIC_HEAT_CAPACITY = ENERGY / MASS / TEMPERATURE

        val HEAT_FLUX = POWER / AREA

        val VOLUMETRIC_FLOW = VOLUME / TIME
        val KINEMATIC_VISCOSITY = AREA / TIME

        val HEAT_TRANSFER_COEFFICIENT =
            POWER / AREA / TEMPERATURE

        val THERMAL_RESISTANCE =
            TEMPERATURE / POWER
        val PRESSURE = PhysicalDimension(
            length = -1,
            mass = 1,
            time = -2
        )
    }
}
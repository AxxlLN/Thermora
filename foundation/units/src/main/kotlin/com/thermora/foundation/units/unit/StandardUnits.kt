
package com.thermora.foundation.units.unit

import com.thermora.foundation.units.dimension.PhysicalDimension

/**
 * Standard measurement units used by Thermora.
 *
 * All scale factors convert values to corresponding SI base units.
 */
object StandardUnits {

    // Length
    val METER = MeasurementUnit(
        "m", PhysicalDimension.LENGTH, 1.0
    )

    val MILLIMETER = MeasurementUnit(
        "mm", PhysicalDimension.LENGTH, 1e-3
    )

    val CENTIMETER = MeasurementUnit(
        "cm", PhysicalDimension.LENGTH, 1e-2
    )

    // Area
    val SQUARE_METER = MeasurementUnit(
        "m²", PhysicalDimension.AREA, 1.0
    )

    val SQUARE_CENTIMETER = MeasurementUnit(
        "cm²", PhysicalDimension.AREA, 1e-4
    )

    // Volume
    val CUBIC_METER = MeasurementUnit(
        "m³", PhysicalDimension.VOLUME, 1.0
    )

    // Mass
    val KILOGRAM = MeasurementUnit(
        "kg", PhysicalDimension.MASS, 1.0
    )

    val GRAM = MeasurementUnit(
        "g", PhysicalDimension.MASS, 1e-3
    )

    // Time
    val SECOND = MeasurementUnit(
        "s", PhysicalDimension.TIME, 1.0
    )

    val MINUTE = MeasurementUnit(
        "min", PhysicalDimension.TIME, 60.0
    )

    // Temperature
    val KELVIN = MeasurementUnit(
        "K", PhysicalDimension.TEMPERATURE, 1.0
    )

    val CELSIUS = MeasurementUnit(
        "°C", PhysicalDimension.TEMPERATURE, 1.0, 273.15
    )

    // Pressure
    val PASCAL = MeasurementUnit(
        "Pa", PhysicalDimension.PRESSURE, 1.0
    )

    val KILOPASCAL = MeasurementUnit(
        "kPa", PhysicalDimension.PRESSURE, 1e3
    )

    val MEGAPASCAL = MeasurementUnit(
        "MPa", PhysicalDimension.PRESSURE, 1e6
    )

    // Power
    val WATT = MeasurementUnit(
        "W", PhysicalDimension.POWER, 1.0
    )

    val KILOWATT = MeasurementUnit(
        "kW", PhysicalDimension.POWER, 1e3
    )

    // Energy
    val JOULE = MeasurementUnit(
        "J", PhysicalDimension.ENERGY, 1.0
    )

    // Thermal properties
    val WATT_PER_METER_KELVIN = MeasurementUnit(
        "W/(m·K)",
        PhysicalDimension.THERMAL_CONDUCTIVITY,
        1.0
    )

    val JOULE_PER_KILOGRAM_KELVIN = MeasurementUnit(
        "J/(kg·K)",
        PhysicalDimension.SPECIFIC_HEAT_CAPACITY,
        1.0
    )

    val KILOGRAM_PER_CUBIC_METER = MeasurementUnit(
        "kg/m³",
        PhysicalDimension.DENSITY,
        1.0
    )

    val WATT_PER_SQUARE_METER = MeasurementUnit(
        "W/m²",
        PhysicalDimension.HEAT_FLUX,
        1.0
    )
}

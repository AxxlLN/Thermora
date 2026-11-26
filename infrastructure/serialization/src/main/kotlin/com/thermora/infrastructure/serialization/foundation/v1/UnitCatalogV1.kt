package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.dimension.PhysicalDimension
import java.util.Collections

internal object UnitCatalogV1 {
    val entries: Map<String, MeasurementUnit> = buildEntries(listOf(
        "m" to MeasurementUnit("m", PhysicalDimension(1, 0, 0, 0, 0, 0, 0), 1.0),
        "mm" to MeasurementUnit("mm", PhysicalDimension(1, 0, 0, 0, 0, 0, 0), 0.001),
        "cm" to MeasurementUnit("cm", PhysicalDimension(1, 0, 0, 0, 0, 0, 0), 0.01),
        "m2" to MeasurementUnit("m²", PhysicalDimension(2, 0, 0, 0, 0, 0, 0), 1.0),
        "cm2" to MeasurementUnit("cm²", PhysicalDimension(2, 0, 0, 0, 0, 0, 0), 0.0001),
        "m3" to MeasurementUnit("m³", PhysicalDimension(3, 0, 0, 0, 0, 0, 0), 1.0),
        "kg" to MeasurementUnit("kg", PhysicalDimension(0, 1, 0, 0, 0, 0, 0), 1.0),
        "g" to MeasurementUnit("g", PhysicalDimension(0, 1, 0, 0, 0, 0, 0), 0.001),
        "s" to MeasurementUnit("s", PhysicalDimension(0, 0, 1, 0, 0, 0, 0), 1.0),
        "min" to MeasurementUnit("min", PhysicalDimension(0, 0, 1, 0, 0, 0, 0), 60.0),
        "Pa" to MeasurementUnit("Pa", PhysicalDimension(-1, 1, -2, 0, 0, 0, 0), 1.0),
        "kPa" to MeasurementUnit("kPa", PhysicalDimension(-1, 1, -2, 0, 0, 0, 0), 1000.0),
        "MPa" to MeasurementUnit("MPa", PhysicalDimension(-1, 1, -2, 0, 0, 0, 0), 1000000.0),
        "W" to MeasurementUnit("W", PhysicalDimension(2, 1, -3, 0, 0, 0, 0), 1.0),
        "kW" to MeasurementUnit("kW", PhysicalDimension(2, 1, -3, 0, 0, 0, 0), 1000.0),
        "J" to MeasurementUnit("J", PhysicalDimension(2, 1, -2, 0, 0, 0, 0), 1.0),
        "W/(m*K)" to MeasurementUnit("W/(m·K)", PhysicalDimension(1, 1, -3, 0, -1, 0, 0), 1.0),
        "J/(kg*K)" to MeasurementUnit("J/(kg·K)", PhysicalDimension(2, 0, -2, 0, -1, 0, 0), 1.0),
        "kg/m3" to MeasurementUnit("kg/m³", PhysicalDimension(-3, 1, 0, 0, 0, 0, 0), 1.0),
        "W/m2" to MeasurementUnit("W/m²", PhysicalDimension(0, 1, -3, 0, 0, 0, 0), 1.0),
        "m/s" to MeasurementUnit("m/s", PhysicalDimension(1, 0, -1, 0, 0, 0, 0), 1.0),
        "m3/s" to MeasurementUnit("m³/s", PhysicalDimension(3, 0, -1, 0, 0, 0, 0), 1.0),
        "m2/s" to MeasurementUnit("m²/s", PhysicalDimension(2, 0, -1, 0, 0, 0, 0), 1.0),
        "W/(m2*K)" to MeasurementUnit("W/(m²·K)", PhysicalDimension(0, 1, -3, 0, -1, 0, 0), 1.0),
        "K/W" to MeasurementUnit("K/W", PhysicalDimension(-2, -1, 3, 0, 1, 0, 0), 1.0)
    ))
    internal fun buildEntries(source: List<Pair<String, MeasurementUnit>>): Map<String, MeasurementUnit> {
        val result = LinkedHashMap<String, MeasurementUnit>()
        for ((id, unit) in source) {
            check(!result.containsKey(id)) { "Duplicate unit ID: $id" }
            check(unit !in result.values) { "Duplicate unit descriptor: $id" }
            result[id] = unit
        }
        return Collections.unmodifiableMap(result)
    }
    fun find(id: String): MeasurementUnit? = entries[id]
    fun idOf(unit: MeasurementUnit): String? = entries.entries.firstOrNull { (_, candidate) ->
        candidate.symbol == unit.symbol && candidate.dimension == unit.dimension &&
            candidate.scaleToBase.toRawBits() == unit.scaleToBase.toRawBits() &&
            candidate.offsetToBase.toRawBits() == unit.offsetToBase.toRawBits()
    }?.key
}

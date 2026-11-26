package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.dimension.PhysicalDimension

internal object PhysicalDimensionJsonMapper {
    fun read(reader: StrictJsonReader, path: String): PhysicalDimension {
        var length = 0
        var mass = 0
        var time = 0
        var electricCurrent = 0
        var thermodynamicTemperature = 0
        var amountOfSubstance = 0
        var luminousIntensity = 0
        JsonObjectFields(reader, path)
            .required("length") { r, _ -> length = r.readInt() }
            .required("mass") { r, _ -> mass = r.readInt() }
            .required("time") { r, _ -> time = r.readInt() }
            .required("electricCurrent") { r, _ -> electricCurrent = r.readInt() }
            .required("thermodynamicTemperature") { r, _ -> thermodynamicTemperature = r.readInt() }
            .required("amountOfSubstance") { r, _ -> amountOfSubstance = r.readInt() }
            .required("luminousIntensity") { r, _ -> luminousIntensity = r.readInt() }
            .read()
        return PhysicalDimension(length, mass, time, electricCurrent, thermodynamicTemperature, amountOfSubstance, luminousIntensity)
    }
    fun write(writer: StrictJsonWriter, value: PhysicalDimension) {
        writer.startObject()
        writer.fieldName("length"); writer.int(value.length)
        writer.fieldName("mass"); writer.int(value.mass)
        writer.fieldName("time"); writer.int(value.time)
        writer.fieldName("electricCurrent"); writer.int(value.electricCurrent)
        writer.fieldName("thermodynamicTemperature"); writer.int(value.thermodynamicTemperature)
        writer.fieldName("amountOfSubstance"); writer.int(value.amountOfSubstance)
        writer.fieldName("luminousIntensity"); writer.int(value.luminousIntensity)
        writer.endObject()
    }
}

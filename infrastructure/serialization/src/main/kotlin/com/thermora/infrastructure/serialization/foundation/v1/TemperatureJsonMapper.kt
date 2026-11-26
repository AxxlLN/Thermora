package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.temperature.Temperature

internal object TemperatureJsonMapper {
    fun read(reader: StrictJsonReader, path: String): Temperature {
        var kelvin = 0.0
        JsonObjectFields(reader, path).required("kelvin") { r, _ -> kelvin = r.readDouble() }.read()
        return try { Temperature.fromKelvin(kelvin) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value("$path.kelvin", "Invalid Temperature", e) }
    }
    fun write(writer: StrictJsonWriter, value: Temperature) {
        writer.startObject()
        writer.fieldName("kelvin"); writer.double(value.kelvin)
        writer.endObject()
    }
}

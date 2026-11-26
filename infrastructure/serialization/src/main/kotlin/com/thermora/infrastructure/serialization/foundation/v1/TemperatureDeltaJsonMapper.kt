package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.temperature.TemperatureDelta

internal object TemperatureDeltaJsonMapper {
    fun read(reader: StrictJsonReader, path: String): TemperatureDelta {
        var kelvin = 0.0
        JsonObjectFields(reader, path).required("kelvinDelta") { r, _ -> kelvin = r.readDouble() }.read()
        return try { TemperatureDelta.fromKelvin(kelvin) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value("$path.kelvinDelta", "Invalid TemperatureDelta", e) }
    }
    fun write(writer: StrictJsonWriter, value: TemperatureDelta) {
        writer.startObject()
        writer.fieldName("kelvinDelta"); writer.double(value.kelvin)
        writer.endObject()
    }
}

package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.MeasurementUnit

internal object QuantityJsonMapper {
    fun read(reader: StrictJsonReader, path: String): Quantity {
        var value = 0.0
        var unit: MeasurementUnit? = null
        JsonObjectFields(reader, path)
            .required("value") { r, _ -> value = r.readDouble() }
            .required("unit") { r, child -> unit = MeasurementUnitJsonMapper.read(r, child) }
            .read()
        return try { Quantity(value, checkNotNull(unit)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid quantity", e) }
    }
    fun write(writer: StrictJsonWriter, value: Quantity) {
        writer.startObject()
        writer.fieldName("value"); writer.double(value.value)
        writer.fieldName("unit"); MeasurementUnitJsonMapper.write(writer, value.unit)
        writer.endObject()
    }
}

package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.dimension.PhysicalDimension

internal object MeasurementUnitJsonMapper {
    fun read(reader: StrictJsonReader, path: String): MeasurementUnit {
        var kind = ""
        var id: String? = null
        var symbol: String? = null
        var dimension: PhysicalDimension? = null
        var scale: Double? = null
        var offset: Double? = null
        JsonObjectFields(reader, path)
            .required("kind") { r, _ -> kind = r.readString() }
            .optional("id") { r, _ -> id = r.readString() }
            .optional("symbol") { r, _ -> symbol = r.readString() }
            .optional("dimension") { r, child -> dimension = PhysicalDimensionJsonMapper.read(r, child) }
            .optional("scaleToBase") { r, _ -> scale = r.readDouble() }
            .optional("offsetToBase") { r, _ -> offset = r.readDouble() }
            .read()
        fun missing(name: String): Nothing = throw JsonReadFailure.payload("$path.$name", "Missing required field")
        if (kind == "standard") {
            if (symbol != null || dimension != null || scale != null || offset != null) {
                throw JsonReadFailure.payload(path, "Mixed unit variants")
            }
            val unitId = id ?: missing("id")
            return UnitCatalogV1.find(unitId) ?: throw JsonReadFailure.value("$path.id", "Unknown standard unit")
        }
        if (kind != "custom") throw JsonReadFailure.value("$path.kind", "Unknown unit kind")
        if (id != null) throw JsonReadFailure.payload("$path.id", "Field not allowed for custom unit")
        val unitSymbol = symbol ?: missing("symbol")
        val unitDimension = dimension ?: missing("dimension")
        val unitScale = scale ?: missing("scaleToBase")
        val unitOffset = offset ?: missing("offsetToBase")
        try { JsonUnicodeValidator.validateCodePointLength(unitSymbol, 128) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.payload("$path.symbol", "Invalid unit symbol", e) }
        if (unitSymbol.isBlank()) throw JsonReadFailure.value("$path.symbol", "Blank unit symbol")
        if (unitScale <= 0.0) throw JsonReadFailure.value("$path.scaleToBase", "Scale must be positive")
        return MeasurementUnit(unitSymbol, unitDimension, unitScale, unitOffset)
    }
    fun write(writer: StrictJsonWriter, value: MeasurementUnit) {
        JsonUnicodeValidator.validateCodePointLength(value.symbol, 128)
        val id = UnitCatalogV1.idOf(value)
        writer.startObject()
        writer.fieldName("kind"); writer.string(if (id == null) "custom" else "standard")
        if (id != null) { writer.fieldName("id"); writer.string(id) }
        else {
            writer.fieldName("symbol"); writer.string(value.symbol)
            writer.fieldName("dimension"); PhysicalDimensionJsonMapper.write(writer, value.dimension)
            writer.fieldName("scaleToBase"); writer.double(value.scaleToBase)
            writer.fieldName("offsetToBase"); writer.double(value.offsetToBase)
        }
        writer.endObject()
    }
}

package com.thermora.infrastructure.serialization.domain.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.infrastructure.serialization.foundation.v1.*
import com.thermora.infrastructure.serialization.domain.v1.*
import com.thermora.infrastructure.serialization.model.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.domain.ventilation.*
import com.thermora.foundation.units.quantity.*
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.units.unit.*
import com.fasterxml.jackson.core.JsonToken

internal object HeatSourceGeometryJsonMapper {
    fun read(reader: StrictJsonReader, path: String): HeatSourceGeometry {
        var length: Quantity? = null
        var width: Quantity? = null
        var height: Quantity? = null
        var volumeFillCoefficient: Double? = null
        JsonObjectFields(reader, path)
            .required("length") { r, child -> length = QuantityJsonMapper.read(r, child) }
            .required("width") { r, child -> width = QuantityJsonMapper.read(r, child) }
            .required("height") { r, child -> height = QuantityJsonMapper.read(r, child) }
            .required("volumeFillCoefficient") { r, child -> volumeFillCoefficient = r.readDouble() }
            .read()
        return try { HeatSourceGeometry(length = checkNotNull(length), width = checkNotNull(width), height = checkNotNull(height), volumeFillCoefficient = checkNotNull(volumeFillCoefficient)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid HeatSourceGeometry", e) }
    }
    fun write(writer: StrictJsonWriter, value: HeatSourceGeometry) {
        writer.startObject()
        writer.fieldName("length")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.length) }
        writer.fieldName("width")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.width) }
        writer.fieldName("height")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.height) }
        writer.fieldName("volumeFillCoefficient")
        ModelJson.double(writer, value.volumeFillCoefficient)
        writer.endObject()
    }
}

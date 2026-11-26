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

internal object ThermalSourceJsonMapper {
    fun read(reader: StrictJsonReader, path: String): ThermalSource {
        var power: Quantity? = null
        JsonObjectFields(reader, path)
            .required("power") { r, child -> power = QuantityJsonMapper.read(r, child) }
            .read()
        return try { ThermalSource(power = checkNotNull(power)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid ThermalSource", e) }
    }
    fun write(writer: StrictJsonWriter, value: ThermalSource) {
        writer.startObject()
        writer.fieldName("power")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.power) }
        writer.endObject()
    }
}

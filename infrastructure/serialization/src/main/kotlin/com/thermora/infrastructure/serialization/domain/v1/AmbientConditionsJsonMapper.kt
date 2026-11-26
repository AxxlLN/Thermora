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

internal object AmbientConditionsJsonMapper {
    fun read(reader: StrictJsonReader, path: String): AmbientConditions {
        var temperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("temperature") { r, child -> temperature = TemperatureJsonMapper.read(r, child) }
            .read()
        return try { AmbientConditions(temperature = checkNotNull(temperature)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid AmbientConditions", e) }
    }
    fun write(writer: StrictJsonWriter, value: AmbientConditions) {
        writer.startObject()
        writer.fieldName("temperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.temperature) }
        writer.endObject()
    }
}

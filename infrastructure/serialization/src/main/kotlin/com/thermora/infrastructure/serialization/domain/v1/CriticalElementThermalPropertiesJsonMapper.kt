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

internal object CriticalElementThermalPropertiesJsonMapper {
    fun read(reader: StrictJsonReader, path: String): CriticalElementThermalProperties {
        var power: Quantity? = null
        var transitionToCaseResistance: ThermalResistance? = null
        var caseToRadiatorResistance: ThermalResistance? = null
        JsonObjectFields(reader, path)
            .required("power") { r, child -> power = QuantityJsonMapper.read(r, child) }
            .required("transitionToCaseResistance") { r, child -> transitionToCaseResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("caseToRadiatorResistance") { r, child -> caseToRadiatorResistance = ThermalResistanceJsonMapper.read(r, child) }
            .read()
        return try { CriticalElementThermalProperties(power = checkNotNull(power), transitionToCaseResistance = checkNotNull(transitionToCaseResistance), caseToRadiatorResistance = checkNotNull(caseToRadiatorResistance)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid CriticalElementThermalProperties", e) }
    }
    fun write(writer: StrictJsonWriter, value: CriticalElementThermalProperties) {
        writer.startObject()
        writer.fieldName("power")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.power) }
        writer.fieldName("transitionToCaseResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.transitionToCaseResistance) }
        writer.fieldName("caseToRadiatorResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.caseToRadiatorResistance) }
        writer.endObject()
    }
}

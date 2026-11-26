package com.thermora.infrastructure.serialization.perforatedhousing.legacy.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.infrastructure.serialization.foundation.v1.*
import com.thermora.infrastructure.serialization.domain.v1.*
import com.thermora.infrastructure.serialization.model.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.*
import com.thermora.foundation.units.temperature.*
import com.thermora.models.perforatedhousing.legacy.input.*
import com.thermora.models.perforatedhousing.legacy.output.*

internal object PerforatedHousingLegacyInputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): PerforatedHousingLegacyInput {
        var ambient: AmbientConditions? = null
        var source: ThermalSource? = null
        var enclosure: EnclosureGeometry? = null
        var heatedZone: HeatSourceGeometry? = null
        var criticalElement: CriticalElementThermalProperties? = null
        var elementWithRadiatorArea: Quantity? = null
        var perforation: PerforationGeometry? = null
        var trialEnclosureTemperature: Temperature? = null
        var trialZoneTemperature: Temperature? = null
        var convectionCoefficient: Quantity? = null
        var firstIterationPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("ambient") { r, child -> ambient = AmbientConditionsJsonMapper.read(r, child) }
            .required("source") { r, child -> source = ThermalSourceJsonMapper.read(r, child) }
            .required("enclosure") { r, child -> enclosure = EnclosureGeometryJsonMapper.read(r, child) }
            .required("heatedZone") { r, child -> heatedZone = HeatSourceGeometryJsonMapper.read(r, child) }
            .required("criticalElement") { r, child -> criticalElement = CriticalElementThermalPropertiesJsonMapper.read(r, child) }
            .required("elementWithRadiatorArea") { r, child -> elementWithRadiatorArea = QuantityJsonMapper.read(r, child) }
            .required("perforation") { r, child -> perforation = readPerforationGeometry(r, child) }
            .required("trialEnclosureTemperature") { r, child -> trialEnclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("trialZoneTemperature") { r, child -> trialZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("convectionCoefficient") { r, child -> convectionCoefficient = QuantityJsonMapper.read(r, child) }
            .required("firstIterationPower") { r, child -> firstIterationPower = QuantityJsonMapper.read(r, child) }
            .read()
        return PerforatedHousingLegacyInput(ambient = checkNotNull(ambient), source = checkNotNull(source), enclosure = checkNotNull(enclosure), heatedZone = checkNotNull(heatedZone), criticalElement = checkNotNull(criticalElement), elementWithRadiatorArea = checkNotNull(elementWithRadiatorArea), perforation = checkNotNull(perforation), trialEnclosureTemperature = checkNotNull(trialEnclosureTemperature), trialZoneTemperature = checkNotNull(trialZoneTemperature), convectionCoefficient = checkNotNull(convectionCoefficient), firstIterationPower = checkNotNull(firstIterationPower))
    }
    fun write(writer: StrictJsonWriter, value: PerforatedHousingLegacyInput) {
        writer.startObject()
        writer.fieldName("ambient")
        AmbientConditionsJsonMapper.write(writer, value.ambient)
        writer.fieldName("source")
        ThermalSourceJsonMapper.write(writer, value.source)
        writer.fieldName("enclosure")
        EnclosureGeometryJsonMapper.write(writer, value.enclosure)
        writer.fieldName("heatedZone")
        HeatSourceGeometryJsonMapper.write(writer, value.heatedZone)
        writer.fieldName("criticalElement")
        CriticalElementThermalPropertiesJsonMapper.write(writer, value.criticalElement)
        writer.fieldName("elementWithRadiatorArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.elementWithRadiatorArea) }
        writer.fieldName("perforation")
        writePerforationGeometry(writer, value.perforation)
        writer.fieldName("trialEnclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.trialEnclosureTemperature) }
        writer.fieldName("trialZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.trialZoneTemperature) }
        writer.fieldName("convectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.convectionCoefficient) }
        writer.fieldName("firstIterationPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.firstIterationPower) }
        writer.endObject()
    }
    fun readPerforationGeometry(reader: StrictJsonReader, path: String): PerforationGeometry {
        var openArea: Quantity? = null
        var transitionArea: Quantity? = null
        JsonObjectFields(reader, path)
            .required("openArea") { r, child -> openArea = QuantityJsonMapper.read(r, child) }
            .required("transitionArea") { r, child -> transitionArea = QuantityJsonMapper.read(r, child) }
            .read()
        return try { PerforationGeometry(openArea = checkNotNull(openArea), transitionArea = checkNotNull(transitionArea)) }
        catch(e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid PerforationGeometry", e) }
    }
    fun writePerforationGeometry(writer: StrictJsonWriter, value: PerforationGeometry) {
        writer.startObject()
        writer.fieldName("openArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.openArea) }
        writer.fieldName("transitionArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.transitionArea) }
        writer.endObject()
    }
}

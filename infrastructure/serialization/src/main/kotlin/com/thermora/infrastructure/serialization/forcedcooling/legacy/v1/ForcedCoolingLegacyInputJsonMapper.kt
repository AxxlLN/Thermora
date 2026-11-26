package com.thermora.infrastructure.serialization.forcedcooling.legacy.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.infrastructure.serialization.foundation.v1.*
import com.thermora.infrastructure.serialization.domain.v1.*
import com.thermora.infrastructure.serialization.model.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.*
import com.thermora.foundation.units.temperature.*
import com.thermora.models.forcedcooling.legacy.input.*
import com.thermora.models.forcedcooling.legacy.output.*

internal object ForcedCoolingLegacyInputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): ForcedCoolingLegacyInput {
        var ambient: AmbientConditions? = null
        var source: ThermalSource? = null
        var enclosure: EnclosureGeometry? = null
        var heatedZone: HeatSourceGeometry? = null
        var criticalElement: CriticalElementThermalProperties? = null
        var elementWithRadiatorArea: Quantity? = null
        var fanVolumeFlow: Quantity? = null
        var componentLength: Quantity? = null
        var trialEnclosureTemperature: Temperature? = null
        var trialZoneTemperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("ambient") { r, child -> ambient = AmbientConditionsJsonMapper.read(r, child) }
            .required("source") { r, child -> source = ThermalSourceJsonMapper.read(r, child) }
            .required("enclosure") { r, child -> enclosure = EnclosureGeometryJsonMapper.read(r, child) }
            .required("heatedZone") { r, child -> heatedZone = HeatSourceGeometryJsonMapper.read(r, child) }
            .required("criticalElement") { r, child -> criticalElement = CriticalElementThermalPropertiesJsonMapper.read(r, child) }
            .required("elementWithRadiatorArea") { r, child -> elementWithRadiatorArea = QuantityJsonMapper.read(r, child) }
            .required("fanVolumeFlow") { r, child -> fanVolumeFlow = QuantityJsonMapper.read(r, child) }
            .required("componentLength") { r, child -> componentLength = QuantityJsonMapper.read(r, child) }
            .required("trialEnclosureTemperature") { r, child -> trialEnclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("trialZoneTemperature") { r, child -> trialZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .read()
        return ForcedCoolingLegacyInput(ambient = checkNotNull(ambient), source = checkNotNull(source), enclosure = checkNotNull(enclosure), heatedZone = checkNotNull(heatedZone), criticalElement = checkNotNull(criticalElement), elementWithRadiatorArea = checkNotNull(elementWithRadiatorArea), fanVolumeFlow = checkNotNull(fanVolumeFlow), componentLength = checkNotNull(componentLength), trialEnclosureTemperature = checkNotNull(trialEnclosureTemperature), trialZoneTemperature = checkNotNull(trialZoneTemperature))
    }
    fun write(writer: StrictJsonWriter, value: ForcedCoolingLegacyInput) {
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
        writer.fieldName("fanVolumeFlow")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.fanVolumeFlow) }
        writer.fieldName("componentLength")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.componentLength) }
        writer.fieldName("trialEnclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.trialEnclosureTemperature) }
        writer.fieldName("trialZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.trialZoneTemperature) }
        writer.endObject()
    }
}

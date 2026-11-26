package com.thermora.infrastructure.serialization.externalcooling.legacy.v1

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
import com.thermora.models.externalcooling.legacy.input.*
import com.thermora.models.externalcooling.legacy.output.*

internal object ExternalCoolingLegacyInputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): ExternalCoolingLegacyInput {
        var ambient: AmbientConditions? = null
        var source: ThermalSource? = null
        var heatedZone: HeatSourceGeometry? = null
        var criticalElement: CriticalElementThermalProperties? = null
        var elementWithRadiatorArea: Quantity? = null
        var heatTransfer: ExternalCoolingHeatTransferInput? = null
        JsonObjectFields(reader, path)
            .required("ambient") { r, child -> ambient = AmbientConditionsJsonMapper.read(r, child) }
            .required("source") { r, child -> source = ThermalSourceJsonMapper.read(r, child) }
            .required("heatedZone") { r, child -> heatedZone = HeatSourceGeometryJsonMapper.read(r, child) }
            .required("criticalElement") { r, child -> criticalElement = CriticalElementThermalPropertiesJsonMapper.read(r, child) }
            .required("elementWithRadiatorArea") { r, child -> elementWithRadiatorArea = QuantityJsonMapper.read(r, child) }
            .required("heatTransfer") { r, child -> heatTransfer = ModelJson.nullable(r) { readExternalCoolingHeatTransferInput(r, child) } }
            .read()
        return ExternalCoolingLegacyInput(ambient = checkNotNull(ambient), source = checkNotNull(source), heatedZone = checkNotNull(heatedZone), criticalElement = checkNotNull(criticalElement), elementWithRadiatorArea = checkNotNull(elementWithRadiatorArea), heatTransfer = heatTransfer)
    }
    fun write(writer: StrictJsonWriter, value: ExternalCoolingLegacyInput) {
        writer.startObject()
        writer.fieldName("ambient")
        AmbientConditionsJsonMapper.write(writer, value.ambient)
        writer.fieldName("source")
        ThermalSourceJsonMapper.write(writer, value.source)
        writer.fieldName("heatedZone")
        HeatSourceGeometryJsonMapper.write(writer, value.heatedZone)
        writer.fieldName("criticalElement")
        CriticalElementThermalPropertiesJsonMapper.write(writer, value.criticalElement)
        writer.fieldName("elementWithRadiatorArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.elementWithRadiatorArea) }
        writer.fieldName("heatTransfer")
        ModelJson.writeNullable(writer, value.heatTransfer) { item -> writeExternalCoolingHeatTransferInput(writer, item) }
        writer.endObject()
    }
    fun readExternalCoolingHeatTransferInput(reader: StrictJsonReader, path: String): ExternalCoolingHeatTransferInput {
        var enclosure: EnclosureGeometry? = null
        var enclosureEmissivity: Double? = null
        var airflow: AirflowConditions? = null
        var enclosureProbeRises: List<TemperatureDelta>? = null
        var referenceZoneTemperature: Temperature? = null
        var wallGaps: List<ExternalCoolingWallGap>? = null
        var zoneProbeRises: List<TemperatureDelta>? = null
        var zoneProbeResistances: List<ThermalResistance>? = null
        JsonObjectFields(reader, path)
            .required("enclosure") { r, child -> enclosure = EnclosureGeometryJsonMapper.read(r, child) }
            .required("enclosureEmissivity") { r, child -> enclosureEmissivity = r.readDouble() }
            .required("airflow") { r, child -> airflow = readAirflowConditions(r, child) }
            .required("enclosureProbeRises") { r, child -> enclosureProbeRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .required("referenceZoneTemperature") { r, child -> referenceZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("wallGaps") { r, child -> wallGaps = ModelJson.list(r, child) { item, itemPath -> readExternalCoolingWallGap(item, itemPath) } }
            .required("zoneProbeRises") { r, child -> zoneProbeRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .required("zoneProbeResistances") { r, child -> zoneProbeResistances = ModelJson.list(r, child) { item, itemPath -> ThermalResistanceJsonMapper.read(item, itemPath) } }
            .read()
        return ExternalCoolingHeatTransferInput(enclosure = checkNotNull(enclosure), enclosureEmissivity = checkNotNull(enclosureEmissivity), airflow = checkNotNull(airflow), enclosureProbeRises = checkNotNull(enclosureProbeRises), referenceZoneTemperature = checkNotNull(referenceZoneTemperature), wallGaps = checkNotNull(wallGaps), zoneProbeRises = checkNotNull(zoneProbeRises), zoneProbeResistances = checkNotNull(zoneProbeResistances))
    }
    fun writeExternalCoolingHeatTransferInput(writer: StrictJsonWriter, value: ExternalCoolingHeatTransferInput) {
        writer.startObject()
        writer.fieldName("enclosure")
        EnclosureGeometryJsonMapper.write(writer, value.enclosure)
        writer.fieldName("enclosureEmissivity")
        ModelJson.double(writer, value.enclosureEmissivity)
        writer.fieldName("airflow")
        writeAirflowConditions(writer, value.airflow)
        writer.fieldName("enclosureProbeRises")
        ModelJson.writeList(writer, value.enclosureProbeRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.fieldName("referenceZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.referenceZoneTemperature) }
        writer.fieldName("wallGaps")
        ModelJson.writeList(writer, value.wallGaps) { item -> writeExternalCoolingWallGap(writer, item) }
        writer.fieldName("zoneProbeRises")
        ModelJson.writeList(writer, value.zoneProbeRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.fieldName("zoneProbeResistances")
        ModelJson.writeList(writer, value.zoneProbeResistances) { item -> ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, item) } }
        writer.endObject()
    }
    fun readAirflowConditions(reader: StrictJsonReader, path: String): AirflowConditions {
        var velocity: Quantity? = null
        JsonObjectFields(reader, path)
            .required("velocity") { r, child -> velocity = QuantityJsonMapper.read(r, child) }
            .read()
        return try { AirflowConditions(velocity = checkNotNull(velocity)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid AirflowConditions", e) }
    }
    fun writeAirflowConditions(writer: StrictJsonWriter, value: AirflowConditions) {
        writer.startObject()
        writer.fieldName("velocity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.velocity) }
        writer.endObject()
    }
    fun readExternalCoolingWallGap(reader: StrictJsonReader, path: String): ExternalCoolingWallGap {
        var wallIndex: Int? = null
        var gap: Quantity? = null
        JsonObjectFields(reader, path)
            .required("wallIndex") { r, child -> wallIndex = r.readInt() }
            .required("gap") { r, child -> gap = QuantityJsonMapper.read(r, child) }
            .read()
        return ExternalCoolingWallGap(wallIndex = checkNotNull(wallIndex), gap = checkNotNull(gap))
    }
    fun writeExternalCoolingWallGap(writer: StrictJsonWriter, value: ExternalCoolingWallGap) {
        writer.startObject()
        writer.fieldName("wallIndex")
        writer.int(value.wallIndex)
        writer.fieldName("gap")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.gap) }
        writer.endObject()
    }
}

package com.thermora.infrastructure.serialization.sealedhousing.legacy.v1

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
import com.thermora.models.sealedhousing.legacy.input.*
import com.thermora.models.sealedhousing.legacy.output.*

internal object SealedHousingLegacyInputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): SealedHousingLegacyInput {
        var ambient: AmbientConditions? = null
        var source: ThermalSource? = null
        var heatedZone: HeatSourceGeometry? = null
        var criticalElement: CriticalElementThermalProperties? = null
        var elementWithRadiatorArea: Quantity? = null
        var heatTransfer: SealedHousingHeatTransferInput? = null
        JsonObjectFields(reader, path)
            .required("ambient") { r, child -> ambient = AmbientConditionsJsonMapper.read(r, child) }
            .required("source") { r, child -> source = ThermalSourceJsonMapper.read(r, child) }
            .required("heatedZone") { r, child -> heatedZone = HeatSourceGeometryJsonMapper.read(r, child) }
            .required("criticalElement") { r, child -> criticalElement = CriticalElementThermalPropertiesJsonMapper.read(r, child) }
            .required("elementWithRadiatorArea") { r, child -> elementWithRadiatorArea = QuantityJsonMapper.read(r, child) }
            .required("heatTransfer") { r, child -> heatTransfer = ModelJson.nullable(r) { readSealedHousingHeatTransferInput(r, child) } }
            .read()
        return SealedHousingLegacyInput(ambient = checkNotNull(ambient), source = checkNotNull(source), heatedZone = checkNotNull(heatedZone), criticalElement = checkNotNull(criticalElement), elementWithRadiatorArea = checkNotNull(elementWithRadiatorArea), heatTransfer = heatTransfer)
    }
    fun write(writer: StrictJsonWriter, value: SealedHousingLegacyInput) {
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
        ModelJson.writeNullable(writer, value.heatTransfer) { item -> writeSealedHousingHeatTransferInput(writer, item) }
        writer.endObject()
    }
    fun readSealedHousingHeatTransferInput(reader: StrictJsonReader, path: String): SealedHousingHeatTransferInput {
        var enclosure: EnclosureGeometry? = null
        var enclosureEmissivity: Double? = null
        var enclosureReferenceRise: TemperatureDelta? = null
        var enclosureProbeResistances: List<ThermalResistance>? = null
        var enclosureProbeRises: List<TemperatureDelta>? = null
        var referenceZoneTemperature: Temperature? = null
        var wallGaps: List<SealedHousingWallGap>? = null
        var zoneProbeRises: List<TemperatureDelta>? = null
        var zoneProbeResistances: List<ThermalResistance>? = null
        JsonObjectFields(reader, path)
            .required("enclosure") { r, child -> enclosure = EnclosureGeometryJsonMapper.read(r, child) }
            .required("enclosureEmissivity") { r, child -> enclosureEmissivity = r.readDouble() }
            .required("enclosureReferenceRise") { r, child -> enclosureReferenceRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("enclosureProbeResistances") { r, child -> enclosureProbeResistances = ModelJson.list(r, child) { item, itemPath -> ThermalResistanceJsonMapper.read(item, itemPath) } }
            .required("enclosureProbeRises") { r, child -> enclosureProbeRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .required("referenceZoneTemperature") { r, child -> referenceZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("wallGaps") { r, child -> wallGaps = ModelJson.list(r, child) { item, itemPath -> readSealedHousingWallGap(item, itemPath) } }
            .required("zoneProbeRises") { r, child -> zoneProbeRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .required("zoneProbeResistances") { r, child -> zoneProbeResistances = ModelJson.list(r, child) { item, itemPath -> ThermalResistanceJsonMapper.read(item, itemPath) } }
            .read()
        return SealedHousingHeatTransferInput(enclosure = checkNotNull(enclosure), enclosureEmissivity = checkNotNull(enclosureEmissivity), enclosureReferenceRise = checkNotNull(enclosureReferenceRise), enclosureProbeResistances = checkNotNull(enclosureProbeResistances), enclosureProbeRises = checkNotNull(enclosureProbeRises), referenceZoneTemperature = checkNotNull(referenceZoneTemperature), wallGaps = checkNotNull(wallGaps), zoneProbeRises = checkNotNull(zoneProbeRises), zoneProbeResistances = checkNotNull(zoneProbeResistances))
    }
    fun writeSealedHousingHeatTransferInput(writer: StrictJsonWriter, value: SealedHousingHeatTransferInput) {
        writer.startObject()
        writer.fieldName("enclosure")
        EnclosureGeometryJsonMapper.write(writer, value.enclosure)
        writer.fieldName("enclosureEmissivity")
        ModelJson.double(writer, value.enclosureEmissivity)
        writer.fieldName("enclosureReferenceRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureReferenceRise) }
        writer.fieldName("enclosureProbeResistances")
        ModelJson.writeList(writer, value.enclosureProbeResistances) { item -> ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, item) } }
        writer.fieldName("enclosureProbeRises")
        ModelJson.writeList(writer, value.enclosureProbeRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.fieldName("referenceZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.referenceZoneTemperature) }
        writer.fieldName("wallGaps")
        ModelJson.writeList(writer, value.wallGaps) { item -> writeSealedHousingWallGap(writer, item) }
        writer.fieldName("zoneProbeRises")
        ModelJson.writeList(writer, value.zoneProbeRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.fieldName("zoneProbeResistances")
        ModelJson.writeList(writer, value.zoneProbeResistances) { item -> ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, item) } }
        writer.endObject()
    }
    fun readSealedHousingWallGap(reader: StrictJsonReader, path: String): SealedHousingWallGap {
        var wallIndex: Int? = null
        var gap: Quantity? = null
        var orientation: WallOrientation? = null
        JsonObjectFields(reader, path)
            .required("wallIndex") { r, child -> wallIndex = r.readInt() }
            .required("gap") { r, child -> gap = QuantityJsonMapper.read(r, child) }
            .required("orientation") { r, child -> orientation = readWallOrientation(r, child) }
            .read()
        return SealedHousingWallGap(wallIndex = checkNotNull(wallIndex), gap = checkNotNull(gap), orientation = checkNotNull(orientation))
    }
    fun writeSealedHousingWallGap(writer: StrictJsonWriter, value: SealedHousingWallGap) {
        writer.startObject()
        writer.fieldName("wallIndex")
        writer.int(value.wallIndex)
        writer.fieldName("gap")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.gap) }
        writer.fieldName("orientation")
        writeWallOrientation(writer, value.orientation)
        writer.endObject()
    }
    private fun readWallOrientation(r: StrictJsonReader, path: String): WallOrientation = when(r.readString()) {
        "vertical" -> WallOrientation.VERTICAL
        "horizontal-top" -> WallOrientation.HORIZONTAL_TOP
        "horizontal-bottom" -> WallOrientation.HORIZONTAL_BOTTOM
        else -> throw JsonReadFailure.value(path,"Unknown WallOrientation")
    }
    private fun writeWallOrientation(writer: StrictJsonWriter, value: WallOrientation) {
        writer.string(when(value) {
            WallOrientation.VERTICAL -> "vertical"
            WallOrientation.HORIZONTAL_TOP -> "horizontal-top"
            WallOrientation.HORIZONTAL_BOTTOM -> "horizontal-bottom"
        })
    }
}

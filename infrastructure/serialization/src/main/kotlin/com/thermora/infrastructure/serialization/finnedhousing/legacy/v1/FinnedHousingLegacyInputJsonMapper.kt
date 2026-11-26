package com.thermora.infrastructure.serialization.finnedhousing.legacy.v1

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
import com.thermora.models.finnedhousing.legacy.input.*
import com.thermora.models.finnedhousing.legacy.output.*

internal object FinnedHousingLegacyInputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): FinnedHousingLegacyInput {
        var ambient: AmbientConditions? = null
        var source: ThermalSource? = null
        var enclosure: EnclosureGeometry? = null
        var fins: FinGeometry? = null
        var heatedZone: HeatSourceGeometry? = null
        var criticalElement: CriticalElementThermalProperties? = null
        var elementWithRadiatorArea: Quantity? = null
        var enclosureEmissivity: Double? = null
        var externalProbes: ExternalProbeConfiguration? = null
        var internalProbes: InternalProbeConfiguration? = null
        JsonObjectFields(reader, path)
            .required("ambient") { r, child -> ambient = AmbientConditionsJsonMapper.read(r, child) }
            .required("source") { r, child -> source = ThermalSourceJsonMapper.read(r, child) }
            .required("enclosure") { r, child -> enclosure = EnclosureGeometryJsonMapper.read(r, child) }
            .required("fins") { r, child -> fins = readFinGeometry(r, child) }
            .required("heatedZone") { r, child -> heatedZone = HeatSourceGeometryJsonMapper.read(r, child) }
            .required("criticalElement") { r, child -> criticalElement = CriticalElementThermalPropertiesJsonMapper.read(r, child) }
            .required("elementWithRadiatorArea") { r, child -> elementWithRadiatorArea = QuantityJsonMapper.read(r, child) }
            .required("enclosureEmissivity") { r, child -> enclosureEmissivity = r.readDouble() }
            .required("externalProbes") { r, child -> externalProbes = readExternalProbeConfiguration(r, child) }
            .required("internalProbes") { r, child -> internalProbes = readInternalProbeConfiguration(r, child) }
            .read()
        return FinnedHousingLegacyInput(ambient = checkNotNull(ambient), source = checkNotNull(source), enclosure = checkNotNull(enclosure), fins = checkNotNull(fins), heatedZone = checkNotNull(heatedZone), criticalElement = checkNotNull(criticalElement), elementWithRadiatorArea = checkNotNull(elementWithRadiatorArea), enclosureEmissivity = checkNotNull(enclosureEmissivity), externalProbes = checkNotNull(externalProbes), internalProbes = checkNotNull(internalProbes))
    }
    fun write(writer: StrictJsonWriter, value: FinnedHousingLegacyInput) {
        writer.startObject()
        writer.fieldName("ambient")
        AmbientConditionsJsonMapper.write(writer, value.ambient)
        writer.fieldName("source")
        ThermalSourceJsonMapper.write(writer, value.source)
        writer.fieldName("enclosure")
        EnclosureGeometryJsonMapper.write(writer, value.enclosure)
        writer.fieldName("fins")
        writeFinGeometry(writer, value.fins)
        writer.fieldName("heatedZone")
        HeatSourceGeometryJsonMapper.write(writer, value.heatedZone)
        writer.fieldName("criticalElement")
        CriticalElementThermalPropertiesJsonMapper.write(writer, value.criticalElement)
        writer.fieldName("elementWithRadiatorArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.elementWithRadiatorArea) }
        writer.fieldName("enclosureEmissivity")
        ModelJson.double(writer, value.enclosureEmissivity)
        writer.fieldName("externalProbes")
        writeExternalProbeConfiguration(writer, value.externalProbes)
        writer.fieldName("internalProbes")
        writeInternalProbeConfiguration(writer, value.internalProbes)
        writer.endObject()
    }
    fun readFinGeometry(reader: StrictJsonReader, path: String): FinGeometry {
        var width: Quantity? = null
        var height: Quantity? = null
        var length: Quantity? = null
        var countPerSide: Int? = null
        var spacing: Quantity? = null
        JsonObjectFields(reader, path)
            .required("width") { r, child -> width = QuantityJsonMapper.read(r, child) }
            .required("height") { r, child -> height = QuantityJsonMapper.read(r, child) }
            .required("length") { r, child -> length = QuantityJsonMapper.read(r, child) }
            .required("countPerSide") { r, child -> countPerSide = r.readInt() }
            .required("spacing") { r, child -> spacing = QuantityJsonMapper.read(r, child) }
            .read()
        return try { FinGeometry(width = checkNotNull(width), height = checkNotNull(height), length = checkNotNull(length), countPerSide = checkNotNull(countPerSide), spacing = checkNotNull(spacing)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid FinGeometry", e) }
    }
    fun writeFinGeometry(writer: StrictJsonWriter, value: FinGeometry) {
        writer.startObject()
        writer.fieldName("width")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.width) }
        writer.fieldName("height")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.height) }
        writer.fieldName("length")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.length) }
        writer.fieldName("countPerSide")
        writer.int(value.countPerSide)
        writer.fieldName("spacing")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.spacing) }
        writer.endObject()
    }
    fun readExternalProbeConfiguration(reader: StrictJsonReader, path: String): ExternalProbeConfiguration {
        var enclosureRises: List<TemperatureDelta>? = null
        JsonObjectFields(reader, path)
            .required("enclosureRises") { r, child -> enclosureRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .read()
        return ExternalProbeConfiguration(enclosureRises = checkNotNull(enclosureRises))
    }
    fun writeExternalProbeConfiguration(writer: StrictJsonWriter, value: ExternalProbeConfiguration) {
        writer.startObject()
        writer.fieldName("enclosureRises")
        ModelJson.writeList(writer, value.enclosureRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.endObject()
    }
    fun readInternalProbeConfiguration(reader: StrictJsonReader, path: String): InternalProbeConfiguration {
        var wallGaps: List<InternalWallGap>? = null
        var referenceZoneTemperature: Temperature? = null
        var trialZoneRises: List<TemperatureDelta>? = null
        var trialResistances: List<ThermalResistance>? = null
        JsonObjectFields(reader, path)
            .required("wallGaps") { r, child -> wallGaps = ModelJson.list(r, child) { item, itemPath -> readInternalWallGap(item, itemPath) } }
            .required("referenceZoneTemperature") { r, child -> referenceZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("trialZoneRises") { r, child -> trialZoneRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .required("trialResistances") { r, child -> trialResistances = ModelJson.list(r, child) { item, itemPath -> ThermalResistanceJsonMapper.read(item, itemPath) } }
            .read()
        return InternalProbeConfiguration(wallGaps = checkNotNull(wallGaps), referenceZoneTemperature = checkNotNull(referenceZoneTemperature), trialZoneRises = checkNotNull(trialZoneRises), trialResistances = checkNotNull(trialResistances))
    }
    fun writeInternalProbeConfiguration(writer: StrictJsonWriter, value: InternalProbeConfiguration) {
        writer.startObject()
        writer.fieldName("wallGaps")
        ModelJson.writeList(writer, value.wallGaps) { item -> writeInternalWallGap(writer, item) }
        writer.fieldName("referenceZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.referenceZoneTemperature) }
        writer.fieldName("trialZoneRises")
        ModelJson.writeList(writer, value.trialZoneRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.fieldName("trialResistances")
        ModelJson.writeList(writer, value.trialResistances) { item -> ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, item) } }
        writer.endObject()
    }
    fun readInternalWallGap(reader: StrictJsonReader, path: String): InternalWallGap {
        var wallIndex: Int? = null
        var gap: Quantity? = null
        var convectionMultiplier: Double? = null
        JsonObjectFields(reader, path)
            .required("wallIndex") { r, child -> wallIndex = r.readInt() }
            .required("gap") { r, child -> gap = QuantityJsonMapper.read(r, child) }
            .required("convectionMultiplier") { r, child -> convectionMultiplier = r.readDouble() }
            .read()
        return InternalWallGap(wallIndex = checkNotNull(wallIndex), gap = checkNotNull(gap), convectionMultiplier = checkNotNull(convectionMultiplier))
    }
    fun writeInternalWallGap(writer: StrictJsonWriter, value: InternalWallGap) {
        writer.startObject()
        writer.fieldName("wallIndex")
        writer.int(value.wallIndex)
        writer.fieldName("gap")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.gap) }
        writer.fieldName("convectionMultiplier")
        ModelJson.double(writer, value.convectionMultiplier)
        writer.endObject()
    }
}

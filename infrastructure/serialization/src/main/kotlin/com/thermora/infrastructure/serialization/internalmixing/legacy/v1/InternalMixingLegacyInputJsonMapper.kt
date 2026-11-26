package com.thermora.infrastructure.serialization.internalmixing.legacy.v1

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
import com.thermora.models.internalmixing.legacy.input.*
import com.thermora.models.internalmixing.legacy.output.*

internal object InternalMixingLegacyInputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): InternalMixingLegacyInput {
        var sealedHousing: SealedHousingBoundarySnapshot? = null
        var heatedZone: HeatSourceGeometry? = null
        var criticalElement: CriticalElementThermalProperties? = null
        var elementWithRadiatorArea: Quantity? = null
        var heatTransfer: InternalMixingHeatTransferInput? = null
        JsonObjectFields(reader, path)
            .required("sealedHousing") { r, child -> sealedHousing = readSealedHousingBoundarySnapshot(r, child) }
            .required("heatedZone") { r, child -> heatedZone = HeatSourceGeometryJsonMapper.read(r, child) }
            .required("criticalElement") { r, child -> criticalElement = CriticalElementThermalPropertiesJsonMapper.read(r, child) }
            .required("elementWithRadiatorArea") { r, child -> elementWithRadiatorArea = QuantityJsonMapper.read(r, child) }
            .required("heatTransfer") { r, child -> heatTransfer = ModelJson.nullable(r) { readInternalMixingHeatTransferInput(r, child) } }
            .read()
        return InternalMixingLegacyInput(sealedHousing = checkNotNull(sealedHousing), heatedZone = checkNotNull(heatedZone), criticalElement = checkNotNull(criticalElement), elementWithRadiatorArea = checkNotNull(elementWithRadiatorArea), heatTransfer = heatTransfer)
    }
    fun write(writer: StrictJsonWriter, value: InternalMixingLegacyInput) {
        writer.startObject()
        writer.fieldName("sealedHousing")
        writeSealedHousingBoundarySnapshot(writer, value.sealedHousing)
        writer.fieldName("heatedZone")
        HeatSourceGeometryJsonMapper.write(writer, value.heatedZone)
        writer.fieldName("criticalElement")
        CriticalElementThermalPropertiesJsonMapper.write(writer, value.criticalElement)
        writer.fieldName("elementWithRadiatorArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.elementWithRadiatorArea) }
        writer.fieldName("heatTransfer")
        ModelJson.writeNullable(writer, value.heatTransfer) { item -> writeInternalMixingHeatTransferInput(writer, item) }
        writer.endObject()
    }
    fun readSealedHousingBoundarySnapshot(reader: StrictJsonReader, path: String): SealedHousingBoundarySnapshot {
        var ambient: AmbientConditions? = null
        var source: ThermalSource? = null
        var enclosureRise: TemperatureDelta? = null
        var enclosureTemperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("ambient") { r, child -> ambient = AmbientConditionsJsonMapper.read(r, child) }
            .required("source") { r, child -> source = ThermalSourceJsonMapper.read(r, child) }
            .required("enclosureRise") { r, child -> enclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .read()
        return SealedHousingBoundarySnapshot(ambient = checkNotNull(ambient), source = checkNotNull(source), enclosureRise = checkNotNull(enclosureRise), enclosureTemperature = checkNotNull(enclosureTemperature))
    }
    fun writeSealedHousingBoundarySnapshot(writer: StrictJsonWriter, value: SealedHousingBoundarySnapshot) {
        writer.startObject()
        writer.fieldName("ambient")
        AmbientConditionsJsonMapper.write(writer, value.ambient)
        writer.fieldName("source")
        ThermalSourceJsonMapper.write(writer, value.source)
        writer.fieldName("enclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureRise) }
        writer.fieldName("enclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.enclosureTemperature) }
        writer.endObject()
    }
    fun readInternalMixingHeatTransferInput(reader: StrictJsonReader, path: String): InternalMixingHeatTransferInput {
        var enclosure: EnclosureGeometry? = null
        var fan: FanSpecification? = null
        var reducedEmissivity: Double? = null
        var zoneProbeRises: List<TemperatureDelta>? = null
        JsonObjectFields(reader, path)
            .required("enclosure") { r, child -> enclosure = EnclosureGeometryJsonMapper.read(r, child) }
            .required("fan") { r, child -> fan = readFanSpecification(r, child) }
            .required("reducedEmissivity") { r, child -> reducedEmissivity = r.readDouble() }
            .required("zoneProbeRises") { r, child -> zoneProbeRises = ModelJson.list(r, child) { item, itemPath -> TemperatureDeltaJsonMapper.read(item, itemPath) } }
            .read()
        return InternalMixingHeatTransferInput(enclosure = checkNotNull(enclosure), fan = checkNotNull(fan), reducedEmissivity = checkNotNull(reducedEmissivity), zoneProbeRises = checkNotNull(zoneProbeRises))
    }
    fun writeInternalMixingHeatTransferInput(writer: StrictJsonWriter, value: InternalMixingHeatTransferInput) {
        writer.startObject()
        writer.fieldName("enclosure")
        EnclosureGeometryJsonMapper.write(writer, value.enclosure)
        writer.fieldName("fan")
        writeFanSpecification(writer, value.fan)
        writer.fieldName("reducedEmissivity")
        ModelJson.double(writer, value.reducedEmissivity)
        writer.fieldName("zoneProbeRises")
        ModelJson.writeList(writer, value.zoneProbeRises) { item -> ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, item) } }
        writer.endObject()
    }
    fun readFanSpecification(reader: StrictJsonReader, path: String): FanSpecification {
        var nominalVolumeFlowRate: Quantity? = null
        var characteristicDimension: Quantity? = null
        JsonObjectFields(reader, path)
            .required("nominalVolumeFlowRate") { r, child -> nominalVolumeFlowRate = QuantityJsonMapper.read(r, child) }
            .required("characteristicDimension") { r, child -> characteristicDimension = QuantityJsonMapper.read(r, child) }
            .read()
        return try { FanSpecification(nominalVolumeFlowRate = checkNotNull(nominalVolumeFlowRate), characteristicDimension = checkNotNull(characteristicDimension)) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid FanSpecification", e) }
    }
    fun writeFanSpecification(writer: StrictJsonWriter, value: FanSpecification) {
        writer.startObject()
        writer.fieldName("nominalVolumeFlowRate")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.nominalVolumeFlowRate) }
        writer.fieldName("characteristicDimension")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.characteristicDimension) }
        writer.endObject()
    }
}

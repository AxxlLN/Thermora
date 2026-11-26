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

internal object InternalMixingLegacyOutputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): InternalMixingLegacyOutput {
        var enclosureTemperature: Temperature? = null
        var heatedZoneTemperature: Temperature? = null
        var internalAirTemperature: Temperature? = null
        var elementTemperature: Temperature? = null
        var junctionTemperature: Temperature? = null
        var heatedZoneRise: TemperatureDelta? = null
        var heatedZoneToAmbientResistance: ThermalResistance? = null
        var geometryResults: InternalMixingGeometryResults? = null
        var heatTransferResults: InternalMixingHeatTransferResults? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("heatedZoneTemperature") { r, child -> heatedZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("internalAirTemperature") { r, child -> internalAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("elementTemperature") { r, child -> elementTemperature = TemperatureJsonMapper.read(r, child) }
            .required("junctionTemperature") { r, child -> junctionTemperature = TemperatureJsonMapper.read(r, child) }
            .required("heatedZoneRise") { r, child -> heatedZoneRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("heatedZoneToAmbientResistance") { r, child -> heatedZoneToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("geometryResults") { r, child -> geometryResults = ModelJson.nullable(r) { readInternalMixingGeometryResults(r, child) } }
            .required("heatTransferResults") { r, child -> heatTransferResults = ModelJson.nullable(r) { readInternalMixingHeatTransferResults(r, child) } }
            .read()
        return InternalMixingLegacyOutput(enclosureTemperature = checkNotNull(enclosureTemperature), heatedZoneTemperature = checkNotNull(heatedZoneTemperature), internalAirTemperature = checkNotNull(internalAirTemperature), elementTemperature = checkNotNull(elementTemperature), junctionTemperature = checkNotNull(junctionTemperature), heatedZoneRise = checkNotNull(heatedZoneRise), heatedZoneToAmbientResistance = checkNotNull(heatedZoneToAmbientResistance), geometryResults = geometryResults, heatTransferResults = heatTransferResults)
    }
    fun write(writer: StrictJsonWriter, value: InternalMixingLegacyOutput) {
        writer.startObject()
        writer.fieldName("enclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.enclosureTemperature) }
        writer.fieldName("heatedZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.heatedZoneTemperature) }
        writer.fieldName("internalAirTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.internalAirTemperature) }
        writer.fieldName("elementTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.elementTemperature) }
        writer.fieldName("junctionTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.junctionTemperature) }
        writer.fieldName("heatedZoneRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.heatedZoneRise) }
        writer.fieldName("heatedZoneToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.heatedZoneToAmbientResistance) }
        writer.fieldName("geometryResults")
        ModelJson.writeNullable(writer, value.geometryResults) { item -> writeInternalMixingGeometryResults(writer, item) }
        writer.fieldName("heatTransferResults")
        ModelJson.writeNullable(writer, value.heatTransferResults) { item -> writeInternalMixingHeatTransferResults(writer, item) }
        writer.endObject()
    }
    fun readInternalMixingGeometryResults(reader: StrictJsonReader, path: String): InternalMixingGeometryResults {
        var enclosureArea: Quantity? = null
        var heatedZoneArea: Quantity? = null
        var zoneFaceAreaXY: Quantity? = null
        var zoneFaceAreaXZ: Quantity? = null
        var zoneFaceAreaYZ: Quantity? = null
        var freeAirVolume: Quantity? = null
        var wettedArea: Quantity? = null
        var hydraulicDiameter: Quantity? = null
        var fanVolumeFlow: Quantity? = null
        var airVelocity: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureArea") { r, child -> enclosureArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneArea") { r, child -> heatedZoneArea = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaXY") { r, child -> zoneFaceAreaXY = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaXZ") { r, child -> zoneFaceAreaXZ = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaYZ") { r, child -> zoneFaceAreaYZ = QuantityJsonMapper.read(r, child) }
            .required("freeAirVolume") { r, child -> freeAirVolume = QuantityJsonMapper.read(r, child) }
            .required("wettedArea") { r, child -> wettedArea = QuantityJsonMapper.read(r, child) }
            .required("hydraulicDiameter") { r, child -> hydraulicDiameter = QuantityJsonMapper.read(r, child) }
            .required("fanVolumeFlow") { r, child -> fanVolumeFlow = QuantityJsonMapper.read(r, child) }
            .required("airVelocity") { r, child -> airVelocity = QuantityJsonMapper.read(r, child) }
            .read()
        return InternalMixingGeometryResults(enclosureArea = checkNotNull(enclosureArea), heatedZoneArea = checkNotNull(heatedZoneArea), zoneFaceAreaXY = checkNotNull(zoneFaceAreaXY), zoneFaceAreaXZ = checkNotNull(zoneFaceAreaXZ), zoneFaceAreaYZ = checkNotNull(zoneFaceAreaYZ), freeAirVolume = checkNotNull(freeAirVolume), wettedArea = checkNotNull(wettedArea), hydraulicDiameter = checkNotNull(hydraulicDiameter), fanVolumeFlow = checkNotNull(fanVolumeFlow), airVelocity = checkNotNull(airVelocity))
    }
    fun writeInternalMixingGeometryResults(writer: StrictJsonWriter, value: InternalMixingGeometryResults) {
        writer.startObject()
        writer.fieldName("enclosureArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.enclosureArea) }
        writer.fieldName("heatedZoneArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneArea) }
        writer.fieldName("zoneFaceAreaXY")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.zoneFaceAreaXY) }
        writer.fieldName("zoneFaceAreaXZ")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.zoneFaceAreaXZ) }
        writer.fieldName("zoneFaceAreaYZ")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.zoneFaceAreaYZ) }
        writer.fieldName("freeAirVolume")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.freeAirVolume) }
        writer.fieldName("wettedArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.wettedArea) }
        writer.fieldName("hydraulicDiameter")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.hydraulicDiameter) }
        writer.fieldName("fanVolumeFlow")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.fanVolumeFlow) }
        writer.fieldName("airVelocity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airVelocity) }
        writer.endObject()
    }
    fun readInternalMixingHeatTransferResults(reader: StrictJsonReader, path: String): InternalMixingHeatTransferResults {
        var probes: List<InternalMixingHeatTransferProbe>? = null
        JsonObjectFields(reader, path)
            .required("probes") { r, child -> probes = ModelJson.list(r, child) { item, itemPath -> readInternalMixingHeatTransferProbe(item, itemPath) } }
            .read()
        return InternalMixingHeatTransferResults(probes = checkNotNull(probes))
    }
    fun writeInternalMixingHeatTransferResults(writer: StrictJsonWriter, value: InternalMixingHeatTransferResults) {
        writer.startObject()
        writer.fieldName("probes")
        ModelJson.writeList(writer, value.probes) { item -> writeInternalMixingHeatTransferProbe(writer, item) }
        writer.endObject()
    }
    fun readInternalMixingHeatTransferProbe(reader: StrictJsonReader, path: String): InternalMixingHeatTransferProbe {
        var zoneTemperature: Temperature? = null
        var zoneRiseAboveEnclosure: TemperatureDelta? = null
        var meanTemperature: Temperature? = null
        var specificHeatCapacity: Quantity? = null
        var density: Quantity? = null
        var kinematicViscosity: Quantity? = null
        var thermalConductivity: Quantity? = null
        var prandtlNumber: Double? = null
        var convectionCoefficient: Quantity? = null
        var radiationCoefficient: Quantity? = null
        var totalHeatTransferCoefficient: Quantity? = null
        var convectionOnlyResistance: ThermalResistance? = null
        var dissipatedPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("zoneTemperature") { r, child -> zoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("zoneRiseAboveEnclosure") { r, child -> zoneRiseAboveEnclosure = TemperatureDeltaJsonMapper.read(r, child) }
            .required("meanTemperature") { r, child -> meanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("specificHeatCapacity") { r, child -> specificHeatCapacity = QuantityJsonMapper.read(r, child) }
            .required("density") { r, child -> density = QuantityJsonMapper.read(r, child) }
            .required("kinematicViscosity") { r, child -> kinematicViscosity = QuantityJsonMapper.read(r, child) }
            .required("thermalConductivity") { r, child -> thermalConductivity = QuantityJsonMapper.read(r, child) }
            .required("prandtlNumber") { r, child -> prandtlNumber = r.readDouble() }
            .required("convectionCoefficient") { r, child -> convectionCoefficient = QuantityJsonMapper.read(r, child) }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("totalHeatTransferCoefficient") { r, child -> totalHeatTransferCoefficient = QuantityJsonMapper.read(r, child) }
            .required("convectionOnlyResistance") { r, child -> convectionOnlyResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("dissipatedPower") { r, child -> dissipatedPower = QuantityJsonMapper.read(r, child) }
            .read()
        return InternalMixingHeatTransferProbe(zoneTemperature = checkNotNull(zoneTemperature), zoneRiseAboveEnclosure = checkNotNull(zoneRiseAboveEnclosure), meanTemperature = checkNotNull(meanTemperature), specificHeatCapacity = checkNotNull(specificHeatCapacity), density = checkNotNull(density), kinematicViscosity = checkNotNull(kinematicViscosity), thermalConductivity = checkNotNull(thermalConductivity), prandtlNumber = checkNotNull(prandtlNumber), convectionCoefficient = checkNotNull(convectionCoefficient), radiationCoefficient = checkNotNull(radiationCoefficient), totalHeatTransferCoefficient = checkNotNull(totalHeatTransferCoefficient), convectionOnlyResistance = checkNotNull(convectionOnlyResistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeInternalMixingHeatTransferProbe(writer: StrictJsonWriter, value: InternalMixingHeatTransferProbe) {
        writer.startObject()
        writer.fieldName("zoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.zoneTemperature) }
        writer.fieldName("zoneRiseAboveEnclosure")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.zoneRiseAboveEnclosure) }
        writer.fieldName("meanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.meanTemperature) }
        writer.fieldName("specificHeatCapacity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.specificHeatCapacity) }
        writer.fieldName("density")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.density) }
        writer.fieldName("kinematicViscosity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.kinematicViscosity) }
        writer.fieldName("thermalConductivity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.thermalConductivity) }
        writer.fieldName("prandtlNumber")
        ModelJson.double(writer, value.prandtlNumber)
        writer.fieldName("convectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.convectionCoefficient) }
        writer.fieldName("radiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationCoefficient) }
        writer.fieldName("totalHeatTransferCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.totalHeatTransferCoefficient) }
        writer.fieldName("convectionOnlyResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.convectionOnlyResistance) }
        writer.fieldName("dissipatedPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.dissipatedPower) }
        writer.endObject()
    }
}

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

internal object ExternalCoolingLegacyOutputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): ExternalCoolingLegacyOutput {
        var enclosureTemperature: Temperature? = null
        var heatedZoneTemperature: Temperature? = null
        var internalAirTemperature: Temperature? = null
        var elementTemperature: Temperature? = null
        var junctionTemperature: Temperature? = null
        var enclosureRise: TemperatureDelta? = null
        var heatedZoneRise: TemperatureDelta? = null
        var enclosureToAmbientResistance: ThermalResistance? = null
        var heatedZoneToAmbientResistance: ThermalResistance? = null
        var geometryResults: ExternalCoolingGeometryResults? = null
        var externalHeatTransfer: ExternalCoolingExternalHeatTransferResults? = null
        var internalHeatTransfer: ExternalCoolingInternalHeatTransferResults? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("heatedZoneTemperature") { r, child -> heatedZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("internalAirTemperature") { r, child -> internalAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("elementTemperature") { r, child -> elementTemperature = TemperatureJsonMapper.read(r, child) }
            .required("junctionTemperature") { r, child -> junctionTemperature = TemperatureJsonMapper.read(r, child) }
            .required("enclosureRise") { r, child -> enclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("heatedZoneRise") { r, child -> heatedZoneRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("enclosureToAmbientResistance") { r, child -> enclosureToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("heatedZoneToAmbientResistance") { r, child -> heatedZoneToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("geometryResults") { r, child -> geometryResults = ModelJson.nullable(r) { readExternalCoolingGeometryResults(r, child) } }
            .required("externalHeatTransfer") { r, child -> externalHeatTransfer = ModelJson.nullable(r) { readExternalCoolingExternalHeatTransferResults(r, child) } }
            .required("internalHeatTransfer") { r, child -> internalHeatTransfer = ModelJson.nullable(r) { readExternalCoolingInternalHeatTransferResults(r, child) } }
            .read()
        return ExternalCoolingLegacyOutput(enclosureTemperature = checkNotNull(enclosureTemperature), heatedZoneTemperature = checkNotNull(heatedZoneTemperature), internalAirTemperature = checkNotNull(internalAirTemperature), elementTemperature = checkNotNull(elementTemperature), junctionTemperature = checkNotNull(junctionTemperature), enclosureRise = checkNotNull(enclosureRise), heatedZoneRise = checkNotNull(heatedZoneRise), enclosureToAmbientResistance = checkNotNull(enclosureToAmbientResistance), heatedZoneToAmbientResistance = checkNotNull(heatedZoneToAmbientResistance), geometryResults = geometryResults, externalHeatTransfer = externalHeatTransfer, internalHeatTransfer = internalHeatTransfer)
    }
    fun write(writer: StrictJsonWriter, value: ExternalCoolingLegacyOutput) {
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
        writer.fieldName("enclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureRise) }
        writer.fieldName("heatedZoneRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.heatedZoneRise) }
        writer.fieldName("enclosureToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.enclosureToAmbientResistance) }
        writer.fieldName("heatedZoneToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.heatedZoneToAmbientResistance) }
        writer.fieldName("geometryResults")
        ModelJson.writeNullable(writer, value.geometryResults) { item -> writeExternalCoolingGeometryResults(writer, item) }
        writer.fieldName("externalHeatTransfer")
        ModelJson.writeNullable(writer, value.externalHeatTransfer) { item -> writeExternalCoolingExternalHeatTransferResults(writer, item) }
        writer.fieldName("internalHeatTransfer")
        ModelJson.writeNullable(writer, value.internalHeatTransfer) { item -> writeExternalCoolingInternalHeatTransferResults(writer, item) }
        writer.endObject()
    }
    fun readExternalCoolingGeometryResults(reader: StrictJsonReader, path: String): ExternalCoolingGeometryResults {
        var enclosureArea: Quantity? = null
        var heatedZoneArea: Quantity? = null
        var zoneFaceAreaXY: Quantity? = null
        var zoneFaceAreaXZ: Quantity? = null
        var zoneFaceAreaYZ: Quantity? = null
        var flowLength: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureArea") { r, child -> enclosureArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneArea") { r, child -> heatedZoneArea = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaXY") { r, child -> zoneFaceAreaXY = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaXZ") { r, child -> zoneFaceAreaXZ = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaYZ") { r, child -> zoneFaceAreaYZ = QuantityJsonMapper.read(r, child) }
            .required("flowLength") { r, child -> flowLength = QuantityJsonMapper.read(r, child) }
            .read()
        return ExternalCoolingGeometryResults(enclosureArea = checkNotNull(enclosureArea), heatedZoneArea = checkNotNull(heatedZoneArea), zoneFaceAreaXY = checkNotNull(zoneFaceAreaXY), zoneFaceAreaXZ = checkNotNull(zoneFaceAreaXZ), zoneFaceAreaYZ = checkNotNull(zoneFaceAreaYZ), flowLength = checkNotNull(flowLength))
    }
    fun writeExternalCoolingGeometryResults(writer: StrictJsonWriter, value: ExternalCoolingGeometryResults) {
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
        writer.fieldName("flowLength")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.flowLength) }
        writer.endObject()
    }
    fun readExternalCoolingExternalHeatTransferResults(reader: StrictJsonReader, path: String): ExternalCoolingExternalHeatTransferResults {
        var probes: List<ExternalCoolingExternalProbe>? = null
        JsonObjectFields(reader, path)
            .required("probes") { r, child -> probes = ModelJson.list(r, child) { item, itemPath -> readExternalCoolingExternalProbe(item, itemPath) } }
            .read()
        return ExternalCoolingExternalHeatTransferResults(probes = checkNotNull(probes))
    }
    fun writeExternalCoolingExternalHeatTransferResults(writer: StrictJsonWriter, value: ExternalCoolingExternalHeatTransferResults) {
        writer.startObject()
        writer.fieldName("probes")
        ModelJson.writeList(writer, value.probes) { item -> writeExternalCoolingExternalProbe(writer, item) }
        writer.endObject()
    }
    fun readExternalCoolingExternalProbe(reader: StrictJsonReader, path: String): ExternalCoolingExternalProbe {
        var enclosureRise: TemperatureDelta? = null
        var enclosureTemperature: Temperature? = null
        var meanTemperature: Temperature? = null
        var radiationCoefficient: Quantity? = null
        var kinematicViscosity: Quantity? = null
        var thermalConductivity: Quantity? = null
        var convectionCoefficient: Quantity? = null
        var enclosureToAmbientResistance: ThermalResistance? = null
        var dissipatedPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureRise") { r, child -> enclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("meanTemperature") { r, child -> meanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("kinematicViscosity") { r, child -> kinematicViscosity = QuantityJsonMapper.read(r, child) }
            .required("thermalConductivity") { r, child -> thermalConductivity = QuantityJsonMapper.read(r, child) }
            .required("convectionCoefficient") { r, child -> convectionCoefficient = QuantityJsonMapper.read(r, child) }
            .required("enclosureToAmbientResistance") { r, child -> enclosureToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("dissipatedPower") { r, child -> dissipatedPower = QuantityJsonMapper.read(r, child) }
            .read()
        return ExternalCoolingExternalProbe(enclosureRise = checkNotNull(enclosureRise), enclosureTemperature = checkNotNull(enclosureTemperature), meanTemperature = checkNotNull(meanTemperature), radiationCoefficient = checkNotNull(radiationCoefficient), kinematicViscosity = checkNotNull(kinematicViscosity), thermalConductivity = checkNotNull(thermalConductivity), convectionCoefficient = checkNotNull(convectionCoefficient), enclosureToAmbientResistance = checkNotNull(enclosureToAmbientResistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeExternalCoolingExternalProbe(writer: StrictJsonWriter, value: ExternalCoolingExternalProbe) {
        writer.startObject()
        writer.fieldName("enclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureRise) }
        writer.fieldName("enclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.enclosureTemperature) }
        writer.fieldName("meanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.meanTemperature) }
        writer.fieldName("radiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationCoefficient) }
        writer.fieldName("kinematicViscosity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.kinematicViscosity) }
        writer.fieldName("thermalConductivity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.thermalConductivity) }
        writer.fieldName("convectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.convectionCoefficient) }
        writer.fieldName("enclosureToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.enclosureToAmbientResistance) }
        writer.fieldName("dissipatedPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.dissipatedPower) }
        writer.endObject()
    }
    fun readExternalCoolingInternalHeatTransferResults(reader: StrictJsonReader, path: String): ExternalCoolingInternalHeatTransferResults {
        var enclosureTemperature: Temperature? = null
        var referenceZoneTemperature: Temperature? = null
        var meanTemperature: Temperature? = null
        var wallConvectionCoefficients: List<Quantity>? = null
        var reducedEmissivity: Double? = null
        var radiationCoefficient: Quantity? = null
        var zoneToEnclosureResistance: ThermalResistance? = null
        var trials: List<ExternalCoolingInternalProbe>? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("referenceZoneTemperature") { r, child -> referenceZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("meanTemperature") { r, child -> meanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("wallConvectionCoefficients") { r, child -> wallConvectionCoefficients = ModelJson.list(r, child) { item, itemPath -> QuantityJsonMapper.read(item, itemPath) } }
            .required("reducedEmissivity") { r, child -> reducedEmissivity = r.readDouble() }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("zoneToEnclosureResistance") { r, child -> zoneToEnclosureResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("trials") { r, child -> trials = ModelJson.list(r, child) { item, itemPath -> readExternalCoolingInternalProbe(item, itemPath) } }
            .read()
        return ExternalCoolingInternalHeatTransferResults(enclosureTemperature = checkNotNull(enclosureTemperature), referenceZoneTemperature = checkNotNull(referenceZoneTemperature), meanTemperature = checkNotNull(meanTemperature), wallConvectionCoefficients = checkNotNull(wallConvectionCoefficients), reducedEmissivity = checkNotNull(reducedEmissivity), radiationCoefficient = checkNotNull(radiationCoefficient), zoneToEnclosureResistance = checkNotNull(zoneToEnclosureResistance), trials = checkNotNull(trials))
    }
    fun writeExternalCoolingInternalHeatTransferResults(writer: StrictJsonWriter, value: ExternalCoolingInternalHeatTransferResults) {
        writer.startObject()
        writer.fieldName("enclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.enclosureTemperature) }
        writer.fieldName("referenceZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.referenceZoneTemperature) }
        writer.fieldName("meanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.meanTemperature) }
        writer.fieldName("wallConvectionCoefficients")
        ModelJson.writeList(writer, value.wallConvectionCoefficients) { item -> ModelJson.foundation { QuantityJsonMapper.write(writer, item) } }
        writer.fieldName("reducedEmissivity")
        ModelJson.double(writer, value.reducedEmissivity)
        writer.fieldName("radiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationCoefficient) }
        writer.fieldName("zoneToEnclosureResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.zoneToEnclosureResistance) }
        writer.fieldName("trials")
        ModelJson.writeList(writer, value.trials) { item -> writeExternalCoolingInternalProbe(writer, item) }
        writer.endObject()
    }
    fun readExternalCoolingInternalProbe(reader: StrictJsonReader, path: String): ExternalCoolingInternalProbe {
        var zoneTemperature: Temperature? = null
        var zoneRise: TemperatureDelta? = null
        var resistance: ThermalResistance? = null
        var dissipatedPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("zoneTemperature") { r, child -> zoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("zoneRise") { r, child -> zoneRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("resistance") { r, child -> resistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("dissipatedPower") { r, child -> dissipatedPower = QuantityJsonMapper.read(r, child) }
            .read()
        return ExternalCoolingInternalProbe(zoneTemperature = checkNotNull(zoneTemperature), zoneRise = checkNotNull(zoneRise), resistance = checkNotNull(resistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeExternalCoolingInternalProbe(writer: StrictJsonWriter, value: ExternalCoolingInternalProbe) {
        writer.startObject()
        writer.fieldName("zoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.zoneTemperature) }
        writer.fieldName("zoneRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.zoneRise) }
        writer.fieldName("resistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.resistance) }
        writer.fieldName("dissipatedPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.dissipatedPower) }
        writer.endObject()
    }
}

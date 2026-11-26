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

internal object FinnedHousingLegacyOutputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): FinnedHousingLegacyOutput {
        var finalResults: FinnedHousingFinalResults? = null
        var geometryResults: FinnedHousingGeometryResults? = null
        var externalHeatTransfer: ExternalHeatTransferResults? = null
        var internalHeatTransfer: InternalHeatTransferResults? = null
        JsonObjectFields(reader, path)
            .required("finalResults") { r, child -> finalResults = readFinnedHousingFinalResults(r, child) }
            .required("geometryResults") { r, child -> geometryResults = readFinnedHousingGeometryResults(r, child) }
            .required("externalHeatTransfer") { r, child -> externalHeatTransfer = readExternalHeatTransferResults(r, child) }
            .required("internalHeatTransfer") { r, child -> internalHeatTransfer = readInternalHeatTransferResults(r, child) }
            .read()
        return FinnedHousingLegacyOutput(finalResults = checkNotNull(finalResults), geometryResults = checkNotNull(geometryResults), externalHeatTransfer = checkNotNull(externalHeatTransfer), internalHeatTransfer = checkNotNull(internalHeatTransfer))
    }
    fun write(writer: StrictJsonWriter, value: FinnedHousingLegacyOutput) {
        writer.startObject()
        writer.fieldName("finalResults")
        writeFinnedHousingFinalResults(writer, value.finalResults)
        writer.fieldName("geometryResults")
        writeFinnedHousingGeometryResults(writer, value.geometryResults)
        writer.fieldName("externalHeatTransfer")
        writeExternalHeatTransferResults(writer, value.externalHeatTransfer)
        writer.fieldName("internalHeatTransfer")
        writeInternalHeatTransferResults(writer, value.internalHeatTransfer)
        writer.endObject()
    }
    fun readFinnedHousingFinalResults(reader: StrictJsonReader, path: String): FinnedHousingFinalResults {
        var enclosureRise: TemperatureDelta? = null
        var enclosureToAmbientResistance: ThermalResistance? = null
        var heatedZoneRise: TemperatureDelta? = null
        var heatedZoneToAmbientResistance: ThermalResistance? = null
        var enclosureTemperature: Temperature? = null
        var heatedZoneTemperature: Temperature? = null
        var internalAirTemperature: Temperature? = null
        var elementTemperature: Temperature? = null
        var junctionTemperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("enclosureRise") { r, child -> enclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("enclosureToAmbientResistance") { r, child -> enclosureToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("heatedZoneRise") { r, child -> heatedZoneRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("heatedZoneToAmbientResistance") { r, child -> heatedZoneToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("heatedZoneTemperature") { r, child -> heatedZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("internalAirTemperature") { r, child -> internalAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("elementTemperature") { r, child -> elementTemperature = TemperatureJsonMapper.read(r, child) }
            .required("junctionTemperature") { r, child -> junctionTemperature = TemperatureJsonMapper.read(r, child) }
            .read()
        return FinnedHousingFinalResults(enclosureRise = checkNotNull(enclosureRise), enclosureToAmbientResistance = checkNotNull(enclosureToAmbientResistance), heatedZoneRise = checkNotNull(heatedZoneRise), heatedZoneToAmbientResistance = checkNotNull(heatedZoneToAmbientResistance), enclosureTemperature = checkNotNull(enclosureTemperature), heatedZoneTemperature = checkNotNull(heatedZoneTemperature), internalAirTemperature = checkNotNull(internalAirTemperature), elementTemperature = checkNotNull(elementTemperature), junctionTemperature = checkNotNull(junctionTemperature))
    }
    fun writeFinnedHousingFinalResults(writer: StrictJsonWriter, value: FinnedHousingFinalResults) {
        writer.startObject()
        writer.fieldName("enclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureRise) }
        writer.fieldName("enclosureToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.enclosureToAmbientResistance) }
        writer.fieldName("heatedZoneRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.heatedZoneRise) }
        writer.fieldName("heatedZoneToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.heatedZoneToAmbientResistance) }
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
        writer.endObject()
    }
    fun readFinnedHousingGeometryResults(reader: StrictJsonReader, path: String): FinnedHousingGeometryResults {
        var finnedSurfaceArea: Quantity? = null
        var smoothSideArea: Quantity? = null
        var smoothFaceArea: Quantity? = null
        var smoothSurfaceArea: Quantity? = null
        var originalEnclosureArea: Quantity? = null
        var heatedZoneArea: Quantity? = null
        var heatedZoneFaceAreaXY: Quantity? = null
        var heatedZoneFaceAreaXZ: Quantity? = null
        var heatedZoneFaceAreaYZ: Quantity? = null
        JsonObjectFields(reader, path)
            .required("finnedSurfaceArea") { r, child -> finnedSurfaceArea = QuantityJsonMapper.read(r, child) }
            .required("smoothSideArea") { r, child -> smoothSideArea = QuantityJsonMapper.read(r, child) }
            .required("smoothFaceArea") { r, child -> smoothFaceArea = QuantityJsonMapper.read(r, child) }
            .required("smoothSurfaceArea") { r, child -> smoothSurfaceArea = QuantityJsonMapper.read(r, child) }
            .required("originalEnclosureArea") { r, child -> originalEnclosureArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneArea") { r, child -> heatedZoneArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneFaceAreaXY") { r, child -> heatedZoneFaceAreaXY = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneFaceAreaXZ") { r, child -> heatedZoneFaceAreaXZ = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneFaceAreaYZ") { r, child -> heatedZoneFaceAreaYZ = QuantityJsonMapper.read(r, child) }
            .read()
        return FinnedHousingGeometryResults(finnedSurfaceArea = checkNotNull(finnedSurfaceArea), smoothSideArea = checkNotNull(smoothSideArea), smoothFaceArea = checkNotNull(smoothFaceArea), smoothSurfaceArea = checkNotNull(smoothSurfaceArea), originalEnclosureArea = checkNotNull(originalEnclosureArea), heatedZoneArea = checkNotNull(heatedZoneArea), heatedZoneFaceAreaXY = checkNotNull(heatedZoneFaceAreaXY), heatedZoneFaceAreaXZ = checkNotNull(heatedZoneFaceAreaXZ), heatedZoneFaceAreaYZ = checkNotNull(heatedZoneFaceAreaYZ))
    }
    fun writeFinnedHousingGeometryResults(writer: StrictJsonWriter, value: FinnedHousingGeometryResults) {
        writer.startObject()
        writer.fieldName("finnedSurfaceArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.finnedSurfaceArea) }
        writer.fieldName("smoothSideArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.smoothSideArea) }
        writer.fieldName("smoothFaceArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.smoothFaceArea) }
        writer.fieldName("smoothSurfaceArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.smoothSurfaceArea) }
        writer.fieldName("originalEnclosureArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.originalEnclosureArea) }
        writer.fieldName("heatedZoneArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneArea) }
        writer.fieldName("heatedZoneFaceAreaXY")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneFaceAreaXY) }
        writer.fieldName("heatedZoneFaceAreaXZ")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneFaceAreaXZ) }
        writer.fieldName("heatedZoneFaceAreaYZ")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneFaceAreaYZ) }
        writer.endObject()
    }
    fun readExternalHeatTransferResults(reader: StrictJsonReader, path: String): ExternalHeatTransferResults {
        var probes: List<ExternalHeatTransferProbe>? = null
        JsonObjectFields(reader, path)
            .required("probes") { r, child -> probes = ModelJson.list(r, child) { item, itemPath -> readExternalHeatTransferProbe(item, itemPath) } }
            .read()
        return ExternalHeatTransferResults(probes = checkNotNull(probes))
    }
    fun writeExternalHeatTransferResults(writer: StrictJsonWriter, value: ExternalHeatTransferResults) {
        writer.startObject()
        writer.fieldName("probes")
        ModelJson.writeList(writer, value.probes) { item -> writeExternalHeatTransferProbe(writer, item) }
        writer.endObject()
    }
    fun readExternalHeatTransferProbe(reader: StrictJsonReader, path: String): ExternalHeatTransferProbe {
        var enclosureRise: TemperatureDelta? = null
        var smoothMeanTemperature: Temperature? = null
        var smoothRadiationCoefficient: Quantity? = null
        var smoothConvectionCoefficient: Quantity? = null
        var channelParameter: Double? = null
        var channelCorrection: Double? = null
        var channelAirTemperature: Temperature? = null
        var finnedMeanTemperature: Temperature? = null
        var finnedRadiationCoefficient: Quantity? = null
        var finnedConvectionCoefficient: Quantity? = null
        var enclosureToAmbientResistance: ThermalResistance? = null
        var dissipatedPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureRise") { r, child -> enclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("smoothMeanTemperature") { r, child -> smoothMeanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("smoothRadiationCoefficient") { r, child -> smoothRadiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("smoothConvectionCoefficient") { r, child -> smoothConvectionCoefficient = QuantityJsonMapper.read(r, child) }
            .required("channelParameter") { r, child -> channelParameter = r.readDouble() }
            .required("channelCorrection") { r, child -> channelCorrection = r.readDouble() }
            .required("channelAirTemperature") { r, child -> channelAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("finnedMeanTemperature") { r, child -> finnedMeanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("finnedRadiationCoefficient") { r, child -> finnedRadiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("finnedConvectionCoefficient") { r, child -> finnedConvectionCoefficient = QuantityJsonMapper.read(r, child) }
            .required("enclosureToAmbientResistance") { r, child -> enclosureToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("dissipatedPower") { r, child -> dissipatedPower = QuantityJsonMapper.read(r, child) }
            .read()
        return ExternalHeatTransferProbe(enclosureRise = checkNotNull(enclosureRise), smoothMeanTemperature = checkNotNull(smoothMeanTemperature), smoothRadiationCoefficient = checkNotNull(smoothRadiationCoefficient), smoothConvectionCoefficient = checkNotNull(smoothConvectionCoefficient), channelParameter = checkNotNull(channelParameter), channelCorrection = checkNotNull(channelCorrection), channelAirTemperature = checkNotNull(channelAirTemperature), finnedMeanTemperature = checkNotNull(finnedMeanTemperature), finnedRadiationCoefficient = checkNotNull(finnedRadiationCoefficient), finnedConvectionCoefficient = checkNotNull(finnedConvectionCoefficient), enclosureToAmbientResistance = checkNotNull(enclosureToAmbientResistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeExternalHeatTransferProbe(writer: StrictJsonWriter, value: ExternalHeatTransferProbe) {
        writer.startObject()
        writer.fieldName("enclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureRise) }
        writer.fieldName("smoothMeanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.smoothMeanTemperature) }
        writer.fieldName("smoothRadiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.smoothRadiationCoefficient) }
        writer.fieldName("smoothConvectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.smoothConvectionCoefficient) }
        writer.fieldName("channelParameter")
        ModelJson.double(writer, value.channelParameter)
        writer.fieldName("channelCorrection")
        ModelJson.double(writer, value.channelCorrection)
        writer.fieldName("channelAirTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.channelAirTemperature) }
        writer.fieldName("finnedMeanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.finnedMeanTemperature) }
        writer.fieldName("finnedRadiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.finnedRadiationCoefficient) }
        writer.fieldName("finnedConvectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.finnedConvectionCoefficient) }
        writer.fieldName("enclosureToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.enclosureToAmbientResistance) }
        writer.fieldName("dissipatedPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.dissipatedPower) }
        writer.endObject()
    }
    fun readInternalHeatTransferResults(reader: StrictJsonReader, path: String): InternalHeatTransferResults {
        var enclosureTemperature: Temperature? = null
        var referenceZoneTemperature: Temperature? = null
        var meanGapTemperature: Temperature? = null
        var reducedEmissivity: Double? = null
        var radiationCoefficient: Quantity? = null
        var wallConvectionCoefficients: List<Quantity>? = null
        var zoneToEnclosureResistance: ThermalResistance? = null
        var trialResults: List<InternalHeatTransferProbe>? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("referenceZoneTemperature") { r, child -> referenceZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("meanGapTemperature") { r, child -> meanGapTemperature = TemperatureJsonMapper.read(r, child) }
            .required("reducedEmissivity") { r, child -> reducedEmissivity = r.readDouble() }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("wallConvectionCoefficients") { r, child -> wallConvectionCoefficients = ModelJson.list(r, child) { item, itemPath -> QuantityJsonMapper.read(item, itemPath) } }
            .required("zoneToEnclosureResistance") { r, child -> zoneToEnclosureResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("trialResults") { r, child -> trialResults = ModelJson.list(r, child) { item, itemPath -> readInternalHeatTransferProbe(item, itemPath) } }
            .read()
        return InternalHeatTransferResults(enclosureTemperature = checkNotNull(enclosureTemperature), referenceZoneTemperature = checkNotNull(referenceZoneTemperature), meanGapTemperature = checkNotNull(meanGapTemperature), reducedEmissivity = checkNotNull(reducedEmissivity), radiationCoefficient = checkNotNull(radiationCoefficient), wallConvectionCoefficients = checkNotNull(wallConvectionCoefficients), zoneToEnclosureResistance = checkNotNull(zoneToEnclosureResistance), trialResults = checkNotNull(trialResults))
    }
    fun writeInternalHeatTransferResults(writer: StrictJsonWriter, value: InternalHeatTransferResults) {
        writer.startObject()
        writer.fieldName("enclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.enclosureTemperature) }
        writer.fieldName("referenceZoneTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.referenceZoneTemperature) }
        writer.fieldName("meanGapTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.meanGapTemperature) }
        writer.fieldName("reducedEmissivity")
        ModelJson.double(writer, value.reducedEmissivity)
        writer.fieldName("radiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationCoefficient) }
        writer.fieldName("wallConvectionCoefficients")
        ModelJson.writeList(writer, value.wallConvectionCoefficients) { item -> ModelJson.foundation { QuantityJsonMapper.write(writer, item) } }
        writer.fieldName("zoneToEnclosureResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.zoneToEnclosureResistance) }
        writer.fieldName("trialResults")
        ModelJson.writeList(writer, value.trialResults) { item -> writeInternalHeatTransferProbe(writer, item) }
        writer.endObject()
    }
    fun readInternalHeatTransferProbe(reader: StrictJsonReader, path: String): InternalHeatTransferProbe {
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
        return InternalHeatTransferProbe(zoneTemperature = checkNotNull(zoneTemperature), zoneRise = checkNotNull(zoneRise), resistance = checkNotNull(resistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeInternalHeatTransferProbe(writer: StrictJsonWriter, value: InternalHeatTransferProbe) {
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

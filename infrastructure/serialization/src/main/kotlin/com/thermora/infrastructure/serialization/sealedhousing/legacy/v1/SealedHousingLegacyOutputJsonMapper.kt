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

internal object SealedHousingLegacyOutputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): SealedHousingLegacyOutput {
        var enclosureTemperature: Temperature? = null
        var heatedZoneTemperature: Temperature? = null
        var internalAirTemperature: Temperature? = null
        var elementTemperature: Temperature? = null
        var junctionTemperature: Temperature? = null
        var enclosureRise: TemperatureDelta? = null
        var heatedZoneRise: TemperatureDelta? = null
        var enclosureToAmbientResistance: ThermalResistance? = null
        var heatedZoneToAmbientResistance: ThermalResistance? = null
        var geometryResults: SealedHousingGeometryResults? = null
        var externalHeatTransfer: SealedHousingExternalHeatTransferResults? = null
        var internalHeatTransfer: SealedHousingInternalHeatTransferResults? = null
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
            .required("geometryResults") { r, child -> geometryResults = ModelJson.nullable(r) { readSealedHousingGeometryResults(r, child) } }
            .required("externalHeatTransfer") { r, child -> externalHeatTransfer = ModelJson.nullable(r) { readSealedHousingExternalHeatTransferResults(r, child) } }
            .required("internalHeatTransfer") { r, child -> internalHeatTransfer = ModelJson.nullable(r) { readSealedHousingInternalHeatTransferResults(r, child) } }
            .read()
        return SealedHousingLegacyOutput(enclosureTemperature = checkNotNull(enclosureTemperature), heatedZoneTemperature = checkNotNull(heatedZoneTemperature), internalAirTemperature = checkNotNull(internalAirTemperature), elementTemperature = checkNotNull(elementTemperature), junctionTemperature = checkNotNull(junctionTemperature), enclosureRise = checkNotNull(enclosureRise), heatedZoneRise = checkNotNull(heatedZoneRise), enclosureToAmbientResistance = checkNotNull(enclosureToAmbientResistance), heatedZoneToAmbientResistance = checkNotNull(heatedZoneToAmbientResistance), geometryResults = geometryResults, externalHeatTransfer = externalHeatTransfer, internalHeatTransfer = internalHeatTransfer)
    }
    fun write(writer: StrictJsonWriter, value: SealedHousingLegacyOutput) {
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
        ModelJson.writeNullable(writer, value.geometryResults) { item -> writeSealedHousingGeometryResults(writer, item) }
        writer.fieldName("externalHeatTransfer")
        ModelJson.writeNullable(writer, value.externalHeatTransfer) { item -> writeSealedHousingExternalHeatTransferResults(writer, item) }
        writer.fieldName("internalHeatTransfer")
        ModelJson.writeNullable(writer, value.internalHeatTransfer) { item -> writeSealedHousingInternalHeatTransferResults(writer, item) }
        writer.endObject()
    }
    fun readSealedHousingGeometryResults(reader: StrictJsonReader, path: String): SealedHousingGeometryResults {
        var enclosureArea: Quantity? = null
        var heatedZoneArea: Quantity? = null
        var zoneFaceAreaXY: Quantity? = null
        var zoneFaceAreaXZ: Quantity? = null
        var zoneFaceAreaYZ: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureArea") { r, child -> enclosureArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneArea") { r, child -> heatedZoneArea = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaXY") { r, child -> zoneFaceAreaXY = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaXZ") { r, child -> zoneFaceAreaXZ = QuantityJsonMapper.read(r, child) }
            .required("zoneFaceAreaYZ") { r, child -> zoneFaceAreaYZ = QuantityJsonMapper.read(r, child) }
            .read()
        return SealedHousingGeometryResults(enclosureArea = checkNotNull(enclosureArea), heatedZoneArea = checkNotNull(heatedZoneArea), zoneFaceAreaXY = checkNotNull(zoneFaceAreaXY), zoneFaceAreaXZ = checkNotNull(zoneFaceAreaXZ), zoneFaceAreaYZ = checkNotNull(zoneFaceAreaYZ))
    }
    fun writeSealedHousingGeometryResults(writer: StrictJsonWriter, value: SealedHousingGeometryResults) {
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
        writer.endObject()
    }
    fun readSealedHousingExternalHeatTransferResults(reader: StrictJsonReader, path: String): SealedHousingExternalHeatTransferResults {
        var referenceRise: TemperatureDelta? = null
        var meanTemperature: Temperature? = null
        var radiationCoefficient: Quantity? = null
        var laminarAirFactor: Double? = null
        var turbulentAirFactor: Double? = null
        var laminarCoefficients: List<Quantity>? = null
        var turbulentCoefficients: List<Quantity>? = null
        var selectedCoefficients: List<Quantity>? = null
        var regimes: List<SealedHousingConvectionRegime>? = null
        var lowerSwitchThreshold: TemperatureDelta? = null
        var upperSwitchThreshold: TemperatureDelta? = null
        var sideSwitchThresholdValue: Double? = null
        var enclosureToAmbientResistance: ThermalResistance? = null
        var trials: List<SealedHousingExternalProbe>? = null
        JsonObjectFields(reader, path)
            .required("referenceRise") { r, child -> referenceRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("meanTemperature") { r, child -> meanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("laminarAirFactor") { r, child -> laminarAirFactor = r.readDouble() }
            .required("turbulentAirFactor") { r, child -> turbulentAirFactor = r.readDouble() }
            .required("laminarCoefficients") { r, child -> laminarCoefficients = ModelJson.list(r, child) { item, itemPath -> QuantityJsonMapper.read(item, itemPath) } }
            .required("turbulentCoefficients") { r, child -> turbulentCoefficients = ModelJson.list(r, child) { item, itemPath -> QuantityJsonMapper.read(item, itemPath) } }
            .required("selectedCoefficients") { r, child -> selectedCoefficients = ModelJson.list(r, child) { item, itemPath -> QuantityJsonMapper.read(item, itemPath) } }
            .required("regimes") { r, child -> regimes = ModelJson.list(r, child) { item, itemPath -> readSealedHousingConvectionRegime(item, itemPath) } }
            .required("lowerSwitchThreshold") { r, child -> lowerSwitchThreshold = TemperatureDeltaJsonMapper.read(r, child) }
            .required("upperSwitchThreshold") { r, child -> upperSwitchThreshold = TemperatureDeltaJsonMapper.read(r, child) }
            .required("sideSwitchThresholdValue") { r, child -> sideSwitchThresholdValue = r.readDouble() }
            .required("enclosureToAmbientResistance") { r, child -> enclosureToAmbientResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("trials") { r, child -> trials = ModelJson.list(r, child) { item, itemPath -> readSealedHousingExternalProbe(item, itemPath) } }
            .read()
        return SealedHousingExternalHeatTransferResults(referenceRise = checkNotNull(referenceRise), meanTemperature = checkNotNull(meanTemperature), radiationCoefficient = checkNotNull(radiationCoefficient), laminarAirFactor = checkNotNull(laminarAirFactor), turbulentAirFactor = checkNotNull(turbulentAirFactor), laminarCoefficients = checkNotNull(laminarCoefficients), turbulentCoefficients = checkNotNull(turbulentCoefficients), selectedCoefficients = checkNotNull(selectedCoefficients), regimes = checkNotNull(regimes), lowerSwitchThreshold = checkNotNull(lowerSwitchThreshold), upperSwitchThreshold = checkNotNull(upperSwitchThreshold), sideSwitchThresholdValue = checkNotNull(sideSwitchThresholdValue), enclosureToAmbientResistance = checkNotNull(enclosureToAmbientResistance), trials = checkNotNull(trials))
    }
    fun writeSealedHousingExternalHeatTransferResults(writer: StrictJsonWriter, value: SealedHousingExternalHeatTransferResults) {
        writer.startObject()
        writer.fieldName("referenceRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.referenceRise) }
        writer.fieldName("meanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.meanTemperature) }
        writer.fieldName("radiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationCoefficient) }
        writer.fieldName("laminarAirFactor")
        ModelJson.double(writer, value.laminarAirFactor)
        writer.fieldName("turbulentAirFactor")
        ModelJson.double(writer, value.turbulentAirFactor)
        writer.fieldName("laminarCoefficients")
        ModelJson.writeList(writer, value.laminarCoefficients) { item -> ModelJson.foundation { QuantityJsonMapper.write(writer, item) } }
        writer.fieldName("turbulentCoefficients")
        ModelJson.writeList(writer, value.turbulentCoefficients) { item -> ModelJson.foundation { QuantityJsonMapper.write(writer, item) } }
        writer.fieldName("selectedCoefficients")
        ModelJson.writeList(writer, value.selectedCoefficients) { item -> ModelJson.foundation { QuantityJsonMapper.write(writer, item) } }
        writer.fieldName("regimes")
        ModelJson.writeList(writer, value.regimes) { item -> writeSealedHousingConvectionRegime(writer, item) }
        writer.fieldName("lowerSwitchThreshold")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.lowerSwitchThreshold) }
        writer.fieldName("upperSwitchThreshold")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.upperSwitchThreshold) }
        writer.fieldName("sideSwitchThresholdValue")
        ModelJson.double(writer, value.sideSwitchThresholdValue)
        writer.fieldName("enclosureToAmbientResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.enclosureToAmbientResistance) }
        writer.fieldName("trials")
        ModelJson.writeList(writer, value.trials) { item -> writeSealedHousingExternalProbe(writer, item) }
        writer.endObject()
    }
    fun readSealedHousingExternalProbe(reader: StrictJsonReader, path: String): SealedHousingExternalProbe {
        var enclosureRise: TemperatureDelta? = null
        var resistance: ThermalResistance? = null
        var dissipatedPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureRise") { r, child -> enclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("resistance") { r, child -> resistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("dissipatedPower") { r, child -> dissipatedPower = QuantityJsonMapper.read(r, child) }
            .read()
        return SealedHousingExternalProbe(enclosureRise = checkNotNull(enclosureRise), resistance = checkNotNull(resistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeSealedHousingExternalProbe(writer: StrictJsonWriter, value: SealedHousingExternalProbe) {
        writer.startObject()
        writer.fieldName("enclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureRise) }
        writer.fieldName("resistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.resistance) }
        writer.fieldName("dissipatedPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.dissipatedPower) }
        writer.endObject()
    }
    fun readSealedHousingInternalHeatTransferResults(reader: StrictJsonReader, path: String): SealedHousingInternalHeatTransferResults {
        var enclosureTemperature: Temperature? = null
        var referenceZoneTemperature: Temperature? = null
        var meanTemperature: Temperature? = null
        var wallConvectionCoefficients: List<Quantity>? = null
        var reducedEmissivity: Double? = null
        var radiationCoefficient: Quantity? = null
        var zoneToEnclosureResistance: ThermalResistance? = null
        var trials: List<SealedHousingInternalProbe>? = null
        var elementToSinkResistance: ThermalResistance? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("referenceZoneTemperature") { r, child -> referenceZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("meanTemperature") { r, child -> meanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("wallConvectionCoefficients") { r, child -> wallConvectionCoefficients = ModelJson.list(r, child) { item, itemPath -> QuantityJsonMapper.read(item, itemPath) } }
            .required("reducedEmissivity") { r, child -> reducedEmissivity = r.readDouble() }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("zoneToEnclosureResistance") { r, child -> zoneToEnclosureResistance = ThermalResistanceJsonMapper.read(r, child) }
            .required("trials") { r, child -> trials = ModelJson.list(r, child) { item, itemPath -> readSealedHousingInternalProbe(item, itemPath) } }
            .required("elementToSinkResistance") { r, child -> elementToSinkResistance = ThermalResistanceJsonMapper.read(r, child) }
            .read()
        return SealedHousingInternalHeatTransferResults(enclosureTemperature = checkNotNull(enclosureTemperature), referenceZoneTemperature = checkNotNull(referenceZoneTemperature), meanTemperature = checkNotNull(meanTemperature), wallConvectionCoefficients = checkNotNull(wallConvectionCoefficients), reducedEmissivity = checkNotNull(reducedEmissivity), radiationCoefficient = checkNotNull(radiationCoefficient), zoneToEnclosureResistance = checkNotNull(zoneToEnclosureResistance), trials = checkNotNull(trials), elementToSinkResistance = checkNotNull(elementToSinkResistance))
    }
    fun writeSealedHousingInternalHeatTransferResults(writer: StrictJsonWriter, value: SealedHousingInternalHeatTransferResults) {
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
        ModelJson.writeList(writer, value.trials) { item -> writeSealedHousingInternalProbe(writer, item) }
        writer.fieldName("elementToSinkResistance")
        ModelJson.foundation { ThermalResistanceJsonMapper.write(writer, value.elementToSinkResistance) }
        writer.endObject()
    }
    fun readSealedHousingInternalProbe(reader: StrictJsonReader, path: String): SealedHousingInternalProbe {
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
        return SealedHousingInternalProbe(zoneTemperature = checkNotNull(zoneTemperature), zoneRise = checkNotNull(zoneRise), resistance = checkNotNull(resistance), dissipatedPower = checkNotNull(dissipatedPower))
    }
    fun writeSealedHousingInternalProbe(writer: StrictJsonWriter, value: SealedHousingInternalProbe) {
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
    private fun readSealedHousingConvectionRegime(r: StrictJsonReader, path: String): SealedHousingConvectionRegime = when(r.readString()) {
        "laminar" -> SealedHousingConvectionRegime.LAMINAR
        "turbulent" -> SealedHousingConvectionRegime.TURBULENT
        else -> throw JsonReadFailure.value(path,"Unknown SealedHousingConvectionRegime")
    }
    private fun writeSealedHousingConvectionRegime(writer: StrictJsonWriter, value: SealedHousingConvectionRegime) {
        writer.string(when(value) {
            SealedHousingConvectionRegime.LAMINAR -> "laminar"
            SealedHousingConvectionRegime.TURBULENT -> "turbulent"
        })
    }
}

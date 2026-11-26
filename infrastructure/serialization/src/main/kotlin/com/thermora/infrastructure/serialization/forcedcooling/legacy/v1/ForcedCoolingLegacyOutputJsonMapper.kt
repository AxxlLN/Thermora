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

internal object ForcedCoolingLegacyOutputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): ForcedCoolingLegacyOutput {
        var geometry: ForcedCoolingGeometryResults? = null
        var heatTransfer: ForcedCoolingHeatTransferResults? = null
        var airBalance: ForcedCoolingAirBalanceResults? = null
        var convergence: ForcedCoolingConvergenceResults? = null
        var nextStep: ForcedCoolingNextStep? = null
        var finalResults: ForcedCoolingFinalResults? = null
        JsonObjectFields(reader, path)
            .required("geometry") { r, child -> geometry = readForcedCoolingGeometryResults(r, child) }
            .required("heatTransfer") { r, child -> heatTransfer = readForcedCoolingHeatTransferResults(r, child) }
            .required("airBalance") { r, child -> airBalance = readForcedCoolingAirBalanceResults(r, child) }
            .required("convergence") { r, child -> convergence = readForcedCoolingConvergenceResults(r, child) }
            .required("nextStep") { r, child -> nextStep = readForcedCoolingNextStep(r, child) }
            .required("finalResults") { r, child -> finalResults = readForcedCoolingFinalResults(r, child) }
            .read()
        return ForcedCoolingLegacyOutput(geometry = checkNotNull(geometry), heatTransfer = checkNotNull(heatTransfer), airBalance = checkNotNull(airBalance), convergence = checkNotNull(convergence), nextStep = checkNotNull(nextStep), finalResults = checkNotNull(finalResults))
    }
    fun write(writer: StrictJsonWriter, value: ForcedCoolingLegacyOutput) {
        writer.startObject()
        writer.fieldName("geometry")
        writeForcedCoolingGeometryResults(writer, value.geometry)
        writer.fieldName("heatTransfer")
        writeForcedCoolingHeatTransferResults(writer, value.heatTransfer)
        writer.fieldName("airBalance")
        writeForcedCoolingAirBalanceResults(writer, value.airBalance)
        writer.fieldName("convergence")
        writeForcedCoolingConvergenceResults(writer, value.convergence)
        writer.fieldName("nextStep")
        writeForcedCoolingNextStep(writer, value.nextStep)
        writer.fieldName("finalResults")
        writeForcedCoolingFinalResults(writer, value.finalResults)
        writer.endObject()
    }
    fun readForcedCoolingGeometryResults(reader: StrictJsonReader, path: String): ForcedCoolingGeometryResults {
        var enclosureArea: Quantity? = null
        var heatedZoneArea: Quantity? = null
        var freeAirVolume: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureArea") { r, child -> enclosureArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneArea") { r, child -> heatedZoneArea = QuantityJsonMapper.read(r, child) }
            .required("freeAirVolume") { r, child -> freeAirVolume = QuantityJsonMapper.read(r, child) }
            .read()
        return ForcedCoolingGeometryResults(enclosureArea = checkNotNull(enclosureArea), heatedZoneArea = checkNotNull(heatedZoneArea), freeAirVolume = checkNotNull(freeAirVolume))
    }
    fun writeForcedCoolingGeometryResults(writer: StrictJsonWriter, value: ForcedCoolingGeometryResults) {
        writer.startObject()
        writer.fieldName("enclosureArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.enclosureArea) }
        writer.fieldName("heatedZoneArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneArea) }
        writer.fieldName("freeAirVolume")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.freeAirVolume) }
        writer.endObject()
    }
    fun readForcedCoolingHeatTransferResults(reader: StrictJsonReader, path: String): ForcedCoolingHeatTransferResults {
        var convectionMeanTemperature: Temperature? = null
        var densityMeanTemperature: Temperature? = null
        var airVelocity: Quantity? = null
        var kinematicViscosity: Quantity? = null
        var airDensity: Quantity? = null
        var radiationFunction: Quantity? = null
        var radiationCoefficient: Quantity? = null
        var convectionCoefficient: Quantity? = null
        JsonObjectFields(reader, path)
            .required("convectionMeanTemperature") { r, child -> convectionMeanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("densityMeanTemperature") { r, child -> densityMeanTemperature = TemperatureJsonMapper.read(r, child) }
            .required("airVelocity") { r, child -> airVelocity = QuantityJsonMapper.read(r, child) }
            .required("kinematicViscosity") { r, child -> kinematicViscosity = QuantityJsonMapper.read(r, child) }
            .required("airDensity") { r, child -> airDensity = QuantityJsonMapper.read(r, child) }
            .required("radiationFunction") { r, child -> radiationFunction = QuantityJsonMapper.read(r, child) }
            .required("radiationCoefficient") { r, child -> radiationCoefficient = QuantityJsonMapper.read(r, child) }
            .required("convectionCoefficient") { r, child -> convectionCoefficient = QuantityJsonMapper.read(r, child) }
            .read()
        return ForcedCoolingHeatTransferResults(convectionMeanTemperature = checkNotNull(convectionMeanTemperature), densityMeanTemperature = checkNotNull(densityMeanTemperature), airVelocity = checkNotNull(airVelocity), kinematicViscosity = checkNotNull(kinematicViscosity), airDensity = checkNotNull(airDensity), radiationFunction = checkNotNull(radiationFunction), radiationCoefficient = checkNotNull(radiationCoefficient), convectionCoefficient = checkNotNull(convectionCoefficient))
    }
    fun writeForcedCoolingHeatTransferResults(writer: StrictJsonWriter, value: ForcedCoolingHeatTransferResults) {
        writer.startObject()
        writer.fieldName("convectionMeanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.convectionMeanTemperature) }
        writer.fieldName("densityMeanTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.densityMeanTemperature) }
        writer.fieldName("airVelocity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airVelocity) }
        writer.fieldName("kinematicViscosity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.kinematicViscosity) }
        writer.fieldName("airDensity")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airDensity) }
        writer.fieldName("radiationFunction")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationFunction) }
        writer.fieldName("radiationCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationCoefficient) }
        writer.fieldName("convectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.convectionCoefficient) }
        writer.endObject()
    }
    fun readForcedCoolingAirBalanceResults(reader: StrictJsonReader, path: String): ForcedCoolingAirBalanceResults {
        var enclosurePower: Quantity? = null
        var initialAirPower: Quantity? = null
        var airRise: TemperatureDelta? = null
        var calculatedZoneRise: TemperatureDelta? = null
        var calculatedEnclosureRise: TemperatureDelta? = null
        var calculatedAirPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosurePower") { r, child -> enclosurePower = QuantityJsonMapper.read(r, child) }
            .required("initialAirPower") { r, child -> initialAirPower = QuantityJsonMapper.read(r, child) }
            .required("airRise") { r, child -> airRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("calculatedZoneRise") { r, child -> calculatedZoneRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("calculatedEnclosureRise") { r, child -> calculatedEnclosureRise = TemperatureDeltaJsonMapper.read(r, child) }
            .required("calculatedAirPower") { r, child -> calculatedAirPower = QuantityJsonMapper.read(r, child) }
            .read()
        return ForcedCoolingAirBalanceResults(enclosurePower = checkNotNull(enclosurePower), initialAirPower = checkNotNull(initialAirPower), airRise = checkNotNull(airRise), calculatedZoneRise = checkNotNull(calculatedZoneRise), calculatedEnclosureRise = checkNotNull(calculatedEnclosureRise), calculatedAirPower = checkNotNull(calculatedAirPower))
    }
    fun writeForcedCoolingAirBalanceResults(writer: StrictJsonWriter, value: ForcedCoolingAirBalanceResults) {
        writer.startObject()
        writer.fieldName("enclosurePower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.enclosurePower) }
        writer.fieldName("initialAirPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.initialAirPower) }
        writer.fieldName("airRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.airRise) }
        writer.fieldName("calculatedZoneRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.calculatedZoneRise) }
        writer.fieldName("calculatedEnclosureRise")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.calculatedEnclosureRise) }
        writer.fieldName("calculatedAirPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.calculatedAirPower) }
        writer.endObject()
    }
    fun readForcedCoolingConvergenceResults(reader: StrictJsonReader, path: String): ForcedCoolingConvergenceResults {
        var enclosureDifference: TemperatureDelta? = null
        var zoneDifference: TemperatureDelta? = null
        var airPowerDifference: Quantity? = null
        var enclosurePercent: Double? = null
        var zonePercent: Double? = null
        var airPowerPercent: Double? = null
        var withinThreePercent: Boolean? = null
        var withinOnePercent: Boolean? = null
        JsonObjectFields(reader, path)
            .required("enclosureDifference") { r, child -> enclosureDifference = TemperatureDeltaJsonMapper.read(r, child) }
            .required("zoneDifference") { r, child -> zoneDifference = TemperatureDeltaJsonMapper.read(r, child) }
            .required("airPowerDifference") { r, child -> airPowerDifference = QuantityJsonMapper.read(r, child) }
            .required("enclosurePercent") { r, child -> enclosurePercent = ModelJson.nullable(r) { r.readDouble() } }
            .required("zonePercent") { r, child -> zonePercent = ModelJson.nullable(r) { r.readDouble() } }
            .required("airPowerPercent") { r, child -> airPowerPercent = ModelJson.nullable(r) { r.readDouble() } }
            .required("withinThreePercent") { r, child -> withinThreePercent = ModelJson.nullable(r) { r.readBoolean() } }
            .required("withinOnePercent") { r, child -> withinOnePercent = ModelJson.nullable(r) { r.readBoolean() } }
            .read()
        return ForcedCoolingConvergenceResults(enclosureDifference = checkNotNull(enclosureDifference), zoneDifference = checkNotNull(zoneDifference), airPowerDifference = checkNotNull(airPowerDifference), enclosurePercent = enclosurePercent, zonePercent = zonePercent, airPowerPercent = airPowerPercent, withinThreePercent = withinThreePercent, withinOnePercent = withinOnePercent)
    }
    fun writeForcedCoolingConvergenceResults(writer: StrictJsonWriter, value: ForcedCoolingConvergenceResults) {
        writer.startObject()
        writer.fieldName("enclosureDifference")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureDifference) }
        writer.fieldName("zoneDifference")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.zoneDifference) }
        writer.fieldName("airPowerDifference")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airPowerDifference) }
        writer.fieldName("enclosurePercent")
        ModelJson.writeNullable(writer, value.enclosurePercent) { item -> ModelJson.double(writer, item) }
        writer.fieldName("zonePercent")
        ModelJson.writeNullable(writer, value.zonePercent) { item -> ModelJson.double(writer, item) }
        writer.fieldName("airPowerPercent")
        ModelJson.writeNullable(writer, value.airPowerPercent) { item -> ModelJson.double(writer, item) }
        writer.fieldName("withinThreePercent")
        ModelJson.writeNullable(writer, value.withinThreePercent) { writer.boolean(it) }
        writer.fieldName("withinOnePercent")
        ModelJson.writeNullable(writer, value.withinOnePercent) { writer.boolean(it) }
        writer.endObject()
    }
    fun readForcedCoolingNextStep(reader: StrictJsonReader, path: String): ForcedCoolingNextStep {
        var enclosureTemperature: Temperature? = null
        var zoneTemperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = ModelJson.nullable(r) { TemperatureJsonMapper.read(r, child) } }
            .required("zoneTemperature") { r, child -> zoneTemperature = ModelJson.nullable(r) { TemperatureJsonMapper.read(r, child) } }
            .read()
        return ForcedCoolingNextStep(enclosureTemperature = enclosureTemperature, zoneTemperature = zoneTemperature)
    }
    fun writeForcedCoolingNextStep(writer: StrictJsonWriter, value: ForcedCoolingNextStep) {
        writer.startObject()
        writer.fieldName("enclosureTemperature")
        ModelJson.writeNullable(writer, value.enclosureTemperature) { item -> ModelJson.foundation { TemperatureJsonMapper.write(writer, item) } }
        writer.fieldName("zoneTemperature")
        ModelJson.writeNullable(writer, value.zoneTemperature) { item -> ModelJson.foundation { TemperatureJsonMapper.write(writer, item) } }
        writer.endObject()
    }
    fun readForcedCoolingFinalResults(reader: StrictJsonReader, path: String): ForcedCoolingFinalResults {
        var enclosureTemperature: Temperature? = null
        var heatedZoneTemperature: Temperature? = null
        var internalAirTemperature: Temperature? = null
        var elementTemperature: Temperature? = null
        var junctionTemperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("heatedZoneTemperature") { r, child -> heatedZoneTemperature = TemperatureJsonMapper.read(r, child) }
            .required("internalAirTemperature") { r, child -> internalAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("elementTemperature") { r, child -> elementTemperature = TemperatureJsonMapper.read(r, child) }
            .required("junctionTemperature") { r, child -> junctionTemperature = TemperatureJsonMapper.read(r, child) }
            .read()
        return ForcedCoolingFinalResults(enclosureTemperature = checkNotNull(enclosureTemperature), heatedZoneTemperature = checkNotNull(heatedZoneTemperature), internalAirTemperature = checkNotNull(internalAirTemperature), elementTemperature = checkNotNull(elementTemperature), junctionTemperature = checkNotNull(junctionTemperature))
    }
    fun writeForcedCoolingFinalResults(writer: StrictJsonWriter, value: ForcedCoolingFinalResults) {
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
        writer.endObject()
    }
}

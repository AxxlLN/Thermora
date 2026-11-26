package com.thermora.infrastructure.serialization.perforatedhousing.legacy.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.infrastructure.serialization.foundation.v1.*
import com.thermora.infrastructure.serialization.domain.v1.*
import com.thermora.infrastructure.serialization.model.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.*
import com.thermora.foundation.units.temperature.*
import com.thermora.models.perforatedhousing.legacy.input.*
import com.thermora.models.perforatedhousing.legacy.output.*

internal object PerforatedHousingLegacyOutputJsonMapper {
    fun read(reader: StrictJsonReader, path: String): PerforatedHousingLegacyOutput {
        var geometry: PerforatedHousingGeometryResults? = null
        var quadratic: PerforatedHousingQuadraticResults? = null
        var heatTransfer: PerforatedHousingHeatTransferResults? = null
        var airBalance: PerforatedHousingAirBalanceResults? = null
        var convergence: PerforatedHousingConvergenceResults? = null
        var nextStep: PerforatedHousingNextStep? = null
        var finalResults: PerforatedHousingFinalResults? = null
        JsonObjectFields(reader, path)
            .required("geometry") { r, child -> geometry = readPerforatedHousingGeometryResults(r, child) }
            .required("quadratic") { r, child -> quadratic = readPerforatedHousingQuadraticResults(r, child) }
            .required("heatTransfer") { r, child -> heatTransfer = readPerforatedHousingHeatTransferResults(r, child) }
            .required("airBalance") { r, child -> airBalance = readPerforatedHousingAirBalanceResults(r, child) }
            .required("convergence") { r, child -> convergence = readPerforatedHousingConvergenceResults(r, child) }
            .required("nextStep") { r, child -> nextStep = readPerforatedHousingNextStep(r, child) }
            .required("finalResults") { r, child -> finalResults = readPerforatedHousingFinalResults(r, child) }
            .read()
        return PerforatedHousingLegacyOutput(geometry = checkNotNull(geometry), quadratic = checkNotNull(quadratic), heatTransfer = checkNotNull(heatTransfer), airBalance = checkNotNull(airBalance), convergence = checkNotNull(convergence), nextStep = checkNotNull(nextStep), finalResults = checkNotNull(finalResults))
    }
    fun write(writer: StrictJsonWriter, value: PerforatedHousingLegacyOutput) {
        writer.startObject()
        writer.fieldName("geometry")
        writePerforatedHousingGeometryResults(writer, value.geometry)
        writer.fieldName("quadratic")
        writePerforatedHousingQuadraticResults(writer, value.quadratic)
        writer.fieldName("heatTransfer")
        writePerforatedHousingHeatTransferResults(writer, value.heatTransfer)
        writer.fieldName("airBalance")
        writePerforatedHousingAirBalanceResults(writer, value.airBalance)
        writer.fieldName("convergence")
        writePerforatedHousingConvergenceResults(writer, value.convergence)
        writer.fieldName("nextStep")
        writePerforatedHousingNextStep(writer, value.nextStep)
        writer.fieldName("finalResults")
        writePerforatedHousingFinalResults(writer, value.finalResults)
        writer.endObject()
    }
    fun readPerforatedHousingGeometryResults(reader: StrictJsonReader, path: String): PerforatedHousingGeometryResults {
        var enclosureArea: Quantity? = null
        var heatedZoneArea: Quantity? = null
        var freeAirVolume: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureArea") { r, child -> enclosureArea = QuantityJsonMapper.read(r, child) }
            .required("heatedZoneArea") { r, child -> heatedZoneArea = QuantityJsonMapper.read(r, child) }
            .required("freeAirVolume") { r, child -> freeAirVolume = QuantityJsonMapper.read(r, child) }
            .read()
        return PerforatedHousingGeometryResults(enclosureArea = checkNotNull(enclosureArea), heatedZoneArea = checkNotNull(heatedZoneArea), freeAirVolume = checkNotNull(freeAirVolume))
    }
    fun writePerforatedHousingGeometryResults(writer: StrictJsonWriter, value: PerforatedHousingGeometryResults) {
        writer.startObject()
        writer.fieldName("enclosureArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.enclosureArea) }
        writer.fieldName("heatedZoneArea")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.heatedZoneArea) }
        writer.fieldName("freeAirVolume")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.freeAirVolume) }
        writer.endObject()
    }
    fun readPerforatedHousingQuadraticResults(reader: StrictJsonReader, path: String): PerforatedHousingQuadraticResults {
        var legacyD: Double? = null
        var a: Double? = null
        var delta: Double? = null
        var b: Double? = null
        var discriminant: Double? = null
        var lowerRootTemperature: Temperature? = null
        var upperRootTemperature: Temperature? = null
        var secondAirTemperature: Temperature? = null
        JsonObjectFields(reader, path)
            .required("legacyD") { r, child -> legacyD = r.readDouble() }
            .required("a") { r, child -> a = r.readDouble() }
            .required("delta") { r, child -> delta = r.readDouble() }
            .required("b") { r, child -> b = r.readDouble() }
            .required("discriminant") { r, child -> discriminant = r.readDouble() }
            .required("lowerRootTemperature") { r, child -> lowerRootTemperature = TemperatureJsonMapper.read(r, child) }
            .required("upperRootTemperature") { r, child -> upperRootTemperature = TemperatureJsonMapper.read(r, child) }
            .required("secondAirTemperature") { r, child -> secondAirTemperature = TemperatureJsonMapper.read(r, child) }
            .read()
        return PerforatedHousingQuadraticResults(legacyD = checkNotNull(legacyD), a = checkNotNull(a), delta = checkNotNull(delta), b = checkNotNull(b), discriminant = checkNotNull(discriminant), lowerRootTemperature = checkNotNull(lowerRootTemperature), upperRootTemperature = checkNotNull(upperRootTemperature), secondAirTemperature = checkNotNull(secondAirTemperature))
    }
    fun writePerforatedHousingQuadraticResults(writer: StrictJsonWriter, value: PerforatedHousingQuadraticResults) {
        writer.startObject()
        writer.fieldName("legacyD")
        ModelJson.double(writer, value.legacyD)
        writer.fieldName("a")
        ModelJson.double(writer, value.a)
        writer.fieldName("delta")
        ModelJson.double(writer, value.delta)
        writer.fieldName("b")
        ModelJson.double(writer, value.b)
        writer.fieldName("discriminant")
        ModelJson.double(writer, value.discriminant)
        writer.fieldName("lowerRootTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.lowerRootTemperature) }
        writer.fieldName("upperRootTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.upperRootTemperature) }
        writer.fieldName("secondAirTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.secondAirTemperature) }
        writer.endObject()
    }
    fun readPerforatedHousingHeatTransferResults(reader: StrictJsonReader, path: String): PerforatedHousingHeatTransferResults {
        var radiationFunction: Quantity? = null
        var radiationPower: Quantity? = null
        JsonObjectFields(reader, path)
            .required("radiationFunction") { r, child -> radiationFunction = QuantityJsonMapper.read(r, child) }
            .required("radiationPower") { r, child -> radiationPower = QuantityJsonMapper.read(r, child) }
            .read()
        return PerforatedHousingHeatTransferResults(radiationFunction = checkNotNull(radiationFunction), radiationPower = checkNotNull(radiationPower))
    }
    fun writePerforatedHousingHeatTransferResults(writer: StrictJsonWriter, value: PerforatedHousingHeatTransferResults) {
        writer.startObject()
        writer.fieldName("radiationFunction")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationFunction) }
        writer.fieldName("radiationPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.radiationPower) }
        writer.endObject()
    }
    fun readPerforatedHousingAirBalanceResults(reader: StrictJsonReader, path: String): PerforatedHousingAirBalanceResults {
        var heatedZoneAirTemperature: Temperature? = null
        var outletAirTemperature: Temperature? = null
        var massFlow: Quantity? = null
        var airMass: Quantity? = null
        var exchangeRate: Quantity? = null
        var airPower: Quantity? = null
        var enclosurePower: Quantity? = null
        var calculatedTotalPower: Quantity? = null
        var calculatedEnclosureTemperature: Temperature? = null
        var calculatedConvectionCoefficient: Quantity? = null
        JsonObjectFields(reader, path)
            .required("heatedZoneAirTemperature") { r, child -> heatedZoneAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("outletAirTemperature") { r, child -> outletAirTemperature = TemperatureJsonMapper.read(r, child) }
            .required("massFlow") { r, child -> massFlow = QuantityJsonMapper.read(r, child) }
            .required("airMass") { r, child -> airMass = QuantityJsonMapper.read(r, child) }
            .required("exchangeRate") { r, child -> exchangeRate = QuantityJsonMapper.read(r, child) }
            .required("airPower") { r, child -> airPower = QuantityJsonMapper.read(r, child) }
            .required("enclosurePower") { r, child -> enclosurePower = QuantityJsonMapper.read(r, child) }
            .required("calculatedTotalPower") { r, child -> calculatedTotalPower = QuantityJsonMapper.read(r, child) }
            .required("calculatedEnclosureTemperature") { r, child -> calculatedEnclosureTemperature = TemperatureJsonMapper.read(r, child) }
            .required("calculatedConvectionCoefficient") { r, child -> calculatedConvectionCoefficient = QuantityJsonMapper.read(r, child) }
            .read()
        return PerforatedHousingAirBalanceResults(heatedZoneAirTemperature = checkNotNull(heatedZoneAirTemperature), outletAirTemperature = checkNotNull(outletAirTemperature), massFlow = checkNotNull(massFlow), airMass = checkNotNull(airMass), exchangeRate = checkNotNull(exchangeRate), airPower = checkNotNull(airPower), enclosurePower = checkNotNull(enclosurePower), calculatedTotalPower = checkNotNull(calculatedTotalPower), calculatedEnclosureTemperature = checkNotNull(calculatedEnclosureTemperature), calculatedConvectionCoefficient = checkNotNull(calculatedConvectionCoefficient))
    }
    fun writePerforatedHousingAirBalanceResults(writer: StrictJsonWriter, value: PerforatedHousingAirBalanceResults) {
        writer.startObject()
        writer.fieldName("heatedZoneAirTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.heatedZoneAirTemperature) }
        writer.fieldName("outletAirTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.outletAirTemperature) }
        writer.fieldName("massFlow")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.massFlow) }
        writer.fieldName("airMass")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airMass) }
        writer.fieldName("exchangeRate")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.exchangeRate) }
        writer.fieldName("airPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airPower) }
        writer.fieldName("enclosurePower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.enclosurePower) }
        writer.fieldName("calculatedTotalPower")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.calculatedTotalPower) }
        writer.fieldName("calculatedEnclosureTemperature")
        ModelJson.foundation { TemperatureJsonMapper.write(writer, value.calculatedEnclosureTemperature) }
        writer.fieldName("calculatedConvectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.calculatedConvectionCoefficient) }
        writer.endObject()
    }
    fun readPerforatedHousingConvergenceResults(reader: StrictJsonReader, path: String): PerforatedHousingConvergenceResults {
        var enclosureDifference: TemperatureDelta? = null
        var totalPowerDifference: Quantity? = null
        var airPowerDifference: Quantity? = null
        var enclosurePercent: Double? = null
        var totalPowerPercent: Double? = null
        var airPowerPercent: Double? = null
        var withinThreePercent: Boolean? = null
        var withinOnePercent: Boolean? = null
        JsonObjectFields(reader, path)
            .required("enclosureDifference") { r, child -> enclosureDifference = TemperatureDeltaJsonMapper.read(r, child) }
            .required("totalPowerDifference") { r, child -> totalPowerDifference = QuantityJsonMapper.read(r, child) }
            .required("airPowerDifference") { r, child -> airPowerDifference = QuantityJsonMapper.read(r, child) }
            .required("enclosurePercent") { r, child -> enclosurePercent = ModelJson.nullable(r) { r.readDouble() } }
            .required("totalPowerPercent") { r, child -> totalPowerPercent = ModelJson.nullable(r) { r.readDouble() } }
            .required("airPowerPercent") { r, child -> airPowerPercent = ModelJson.nullable(r) { r.readDouble() } }
            .required("withinThreePercent") { r, child -> withinThreePercent = ModelJson.nullable(r) { r.readBoolean() } }
            .required("withinOnePercent") { r, child -> withinOnePercent = ModelJson.nullable(r) { r.readBoolean() } }
            .read()
        return PerforatedHousingConvergenceResults(enclosureDifference = checkNotNull(enclosureDifference), totalPowerDifference = checkNotNull(totalPowerDifference), airPowerDifference = checkNotNull(airPowerDifference), enclosurePercent = enclosurePercent, totalPowerPercent = totalPowerPercent, airPowerPercent = airPowerPercent, withinThreePercent = withinThreePercent, withinOnePercent = withinOnePercent)
    }
    fun writePerforatedHousingConvergenceResults(writer: StrictJsonWriter, value: PerforatedHousingConvergenceResults) {
        writer.startObject()
        writer.fieldName("enclosureDifference")
        ModelJson.foundation { TemperatureDeltaJsonMapper.write(writer, value.enclosureDifference) }
        writer.fieldName("totalPowerDifference")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.totalPowerDifference) }
        writer.fieldName("airPowerDifference")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.airPowerDifference) }
        writer.fieldName("enclosurePercent")
        ModelJson.writeNullable(writer, value.enclosurePercent) { item -> ModelJson.double(writer, item) }
        writer.fieldName("totalPowerPercent")
        ModelJson.writeNullable(writer, value.totalPowerPercent) { item -> ModelJson.double(writer, item) }
        writer.fieldName("airPowerPercent")
        ModelJson.writeNullable(writer, value.airPowerPercent) { item -> ModelJson.double(writer, item) }
        writer.fieldName("withinThreePercent")
        ModelJson.writeNullable(writer, value.withinThreePercent) { writer.boolean(it) }
        writer.fieldName("withinOnePercent")
        ModelJson.writeNullable(writer, value.withinOnePercent) { writer.boolean(it) }
        writer.endObject()
    }
    fun readPerforatedHousingNextStep(reader: StrictJsonReader, path: String): PerforatedHousingNextStep {
        var enclosureTemperature: Temperature? = null
        var zoneTemperature: Temperature? = null
        var convectionCoefficient: Quantity? = null
        JsonObjectFields(reader, path)
            .required("enclosureTemperature") { r, child -> enclosureTemperature = ModelJson.nullable(r) { TemperatureJsonMapper.read(r, child) } }
            .required("zoneTemperature") { r, child -> zoneTemperature = ModelJson.nullable(r) { TemperatureJsonMapper.read(r, child) } }
            .required("convectionCoefficient") { r, child -> convectionCoefficient = QuantityJsonMapper.read(r, child) }
            .read()
        return PerforatedHousingNextStep(enclosureTemperature = enclosureTemperature, zoneTemperature = zoneTemperature, convectionCoefficient = checkNotNull(convectionCoefficient))
    }
    fun writePerforatedHousingNextStep(writer: StrictJsonWriter, value: PerforatedHousingNextStep) {
        writer.startObject()
        writer.fieldName("enclosureTemperature")
        ModelJson.writeNullable(writer, value.enclosureTemperature) { item -> ModelJson.foundation { TemperatureJsonMapper.write(writer, item) } }
        writer.fieldName("zoneTemperature")
        ModelJson.writeNullable(writer, value.zoneTemperature) { item -> ModelJson.foundation { TemperatureJsonMapper.write(writer, item) } }
        writer.fieldName("convectionCoefficient")
        ModelJson.foundation { QuantityJsonMapper.write(writer, value.convectionCoefficient) }
        writer.endObject()
    }
    fun readPerforatedHousingFinalResults(reader: StrictJsonReader, path: String): PerforatedHousingFinalResults {
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
        return PerforatedHousingFinalResults(enclosureTemperature = checkNotNull(enclosureTemperature), heatedZoneTemperature = checkNotNull(heatedZoneTemperature), internalAirTemperature = checkNotNull(internalAirTemperature), elementTemperature = checkNotNull(elementTemperature), junctionTemperature = checkNotNull(junctionTemperature))
    }
    fun writePerforatedHousingFinalResults(writer: StrictJsonWriter, value: PerforatedHousingFinalResults) {
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

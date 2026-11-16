package com.thermora.models.externalcooling.legacy.output

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class ExternalCoolingGeometryResults(
    val enclosureArea: Quantity,
    val heatedZoneArea: Quantity,
    val zoneFaceAreaXY: Quantity,
    val zoneFaceAreaXZ: Quantity,
    val zoneFaceAreaYZ: Quantity,
    val flowLength: Quantity
)

data class ExternalCoolingExternalHeatTransferResults(val probes: List<ExternalCoolingExternalProbe>)

data class ExternalCoolingExternalProbe(
    val enclosureRise: TemperatureDelta,
    val enclosureTemperature: Temperature,
    val meanTemperature: Temperature,
    val radiationCoefficient: Quantity,
    val kinematicViscosity: Quantity,
    val thermalConductivity: Quantity,
    val convectionCoefficient: Quantity,
    val enclosureToAmbientResistance: ThermalResistance,
    val dissipatedPower: Quantity
)

data class ExternalCoolingInternalHeatTransferResults(
    val enclosureTemperature: Temperature,
    val referenceZoneTemperature: Temperature,
    val meanTemperature: Temperature,
    val wallConvectionCoefficients: List<Quantity>,
    val reducedEmissivity: Double,
    val radiationCoefficient: Quantity,
    val zoneToEnclosureResistance: ThermalResistance,
    val trials: List<ExternalCoolingInternalProbe>
)

data class ExternalCoolingInternalProbe(
    val zoneTemperature: Temperature,
    val zoneRise: TemperatureDelta,
    val resistance: ThermalResistance,
    val dissipatedPower: Quantity
)

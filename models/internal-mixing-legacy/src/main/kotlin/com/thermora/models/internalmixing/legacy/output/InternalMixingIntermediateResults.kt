package com.thermora.models.internalmixing.legacy.output

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class InternalMixingGeometryResults(
    val enclosureArea: Quantity,
    val heatedZoneArea: Quantity,
    val zoneFaceAreaXY: Quantity,
    val zoneFaceAreaXZ: Quantity,
    val zoneFaceAreaYZ: Quantity,
    val freeAirVolume: Quantity,
    val wettedArea: Quantity,
    val hydraulicDiameter: Quantity,
    val fanVolumeFlow: Quantity,
    val airVelocity: Quantity
)

data class InternalMixingHeatTransferResults(val probes: List<InternalMixingHeatTransferProbe>)

data class InternalMixingHeatTransferProbe(
    val zoneTemperature: Temperature,
    val zoneRiseAboveEnclosure: TemperatureDelta,
    val meanTemperature: Temperature,
    val specificHeatCapacity: Quantity,
    val density: Quantity,
    val kinematicViscosity: Quantity,
    val thermalConductivity: Quantity,
    val prandtlNumber: Double,
    val convectionCoefficient: Quantity,
    val radiationCoefficient: Quantity,
    val totalHeatTransferCoefficient: Quantity,
    val convectionOnlyResistance: ThermalResistance,
    val dissipatedPower: Quantity
)

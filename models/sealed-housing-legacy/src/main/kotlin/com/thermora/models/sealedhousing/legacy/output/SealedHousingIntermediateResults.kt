package com.thermora.models.sealedhousing.legacy.output

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class SealedHousingGeometryResults(
    val enclosureArea: Quantity,
    val heatedZoneArea: Quantity,
    val zoneFaceAreaXY: Quantity,
    val zoneFaceAreaXZ: Quantity,
    val zoneFaceAreaYZ: Quantity
)

enum class SealedHousingConvectionRegime { LAMINAR, TURBULENT }

data class SealedHousingExternalHeatTransferResults(
    val referenceRise: TemperatureDelta,
    val meanTemperature: Temperature,
    val radiationCoefficient: Quantity,
    val laminarAirFactor: Double,
    val turbulentAirFactor: Double,
    val laminarCoefficients: List<Quantity>,
    val turbulentCoefficients: List<Quantity>,
    val selectedCoefficients: List<Quantity>,
    val regimes: List<SealedHousingConvectionRegime>,
    val lowerSwitchThreshold: TemperatureDelta,
    val upperSwitchThreshold: TemperatureDelta,
    val sideSwitchThresholdValue: Double,
    val enclosureToAmbientResistance: ThermalResistance,
    val trials: List<SealedHousingExternalProbe>
)

data class SealedHousingExternalProbe(
    val enclosureRise: TemperatureDelta,
    val resistance: ThermalResistance,
    val dissipatedPower: Quantity
)

data class SealedHousingInternalHeatTransferResults(
    val enclosureTemperature: Temperature,
    val referenceZoneTemperature: Temperature,
    val meanTemperature: Temperature,
    val wallConvectionCoefficients: List<Quantity>,
    val reducedEmissivity: Double,
    val radiationCoefficient: Quantity,
    val zoneToEnclosureResistance: ThermalResistance,
    val trials: List<SealedHousingInternalProbe>,
    val elementToSinkResistance: ThermalResistance
)

data class SealedHousingInternalProbe(
    val zoneTemperature: Temperature,
    val zoneRise: TemperatureDelta,
    val resistance: ThermalResistance,
    val dissipatedPower: Quantity
)

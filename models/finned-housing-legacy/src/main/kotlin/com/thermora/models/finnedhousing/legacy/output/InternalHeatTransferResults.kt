package com.thermora.models.finnedhousing.legacy.output

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class InternalHeatTransferResults(
    val enclosureTemperature: Temperature,
    val referenceZoneTemperature: Temperature,
    val meanGapTemperature: Temperature,
    val reducedEmissivity: Double,
    val radiationCoefficient: Quantity,
    val wallConvectionCoefficients: List<Quantity>,
    val zoneToEnclosureResistance: ThermalResistance,
    val trialResults: List<InternalHeatTransferProbe>
)

data class InternalHeatTransferProbe(
    val zoneTemperature: Temperature,
    val zoneRise: TemperatureDelta,
    val resistance: ThermalResistance,
    val dissipatedPower: Quantity
)

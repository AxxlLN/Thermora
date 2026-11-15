package com.thermora.models.finnedhousing.legacy.output

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class ExternalHeatTransferResults(
    val probes: List<ExternalHeatTransferProbe>
)

data class ExternalHeatTransferProbe(
    val enclosureRise: TemperatureDelta,
    val smoothMeanTemperature: Temperature,
    val smoothRadiationCoefficient: Quantity,
    val smoothConvectionCoefficient: Quantity,
    val channelParameter: Double,
    val channelCorrection: Double,
    val channelAirTemperature: Temperature,
    val finnedMeanTemperature: Temperature,
    val finnedRadiationCoefficient: Quantity,
    val finnedConvectionCoefficient: Quantity,
    val enclosureToAmbientResistance: ThermalResistance,
    val dissipatedPower: Quantity
)

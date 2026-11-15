package com.thermora.models.finnedhousing.legacy.input

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class InternalProbeConfiguration(
    val wallGaps: List<InternalWallGap>,
    val referenceZoneTemperature: Temperature,
    val trialZoneRises: List<TemperatureDelta>,
    val trialResistances: List<ThermalResistance>
)

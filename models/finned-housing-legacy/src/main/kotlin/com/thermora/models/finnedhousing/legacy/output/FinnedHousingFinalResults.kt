package com.thermora.models.finnedhousing.legacy.output

import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class FinnedHousingFinalResults(
    val enclosureRise: TemperatureDelta,
    val enclosureToAmbientResistance: ThermalResistance,
    val heatedZoneRise: TemperatureDelta,
    val heatedZoneToAmbientResistance: ThermalResistance,
    val enclosureTemperature: Temperature,
    val heatedZoneTemperature: Temperature,
    val internalAirTemperature: Temperature,
    val elementTemperature: Temperature,
    val junctionTemperature: Temperature
)

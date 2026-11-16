package com.thermora.models.externalcooling.legacy.output

import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class ExternalCoolingLegacyOutput @JvmOverloads constructor(
    val enclosureTemperature: Temperature,
    val heatedZoneTemperature: Temperature,
    val internalAirTemperature: Temperature,
    val elementTemperature: Temperature,
    val junctionTemperature: Temperature,
    val enclosureRise: TemperatureDelta,
    val heatedZoneRise: TemperatureDelta,
    val enclosureToAmbientResistance: ThermalResistance,
    val heatedZoneToAmbientResistance: ThermalResistance,
    val geometryResults: ExternalCoolingGeometryResults? = null,
    val externalHeatTransfer: ExternalCoolingExternalHeatTransferResults? = null,
    val internalHeatTransfer: ExternalCoolingInternalHeatTransferResults? = null
) : CalculationOutput

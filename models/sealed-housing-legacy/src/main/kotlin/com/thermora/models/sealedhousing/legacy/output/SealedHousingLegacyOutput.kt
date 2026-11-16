package com.thermora.models.sealedhousing.legacy.output

import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class SealedHousingLegacyOutput @JvmOverloads constructor(
    val enclosureTemperature: Temperature,
    val heatedZoneTemperature: Temperature,
    val internalAirTemperature: Temperature,
    val elementTemperature: Temperature,
    val junctionTemperature: Temperature,

    val enclosureRise: TemperatureDelta,
    val heatedZoneRise: TemperatureDelta,

    val enclosureToAmbientResistance: ThermalResistance,
    val heatedZoneToAmbientResistance: ThermalResistance,
    val geometryResults: SealedHousingGeometryResults? = null,
    val externalHeatTransfer: SealedHousingExternalHeatTransferResults? = null,
    val internalHeatTransfer: SealedHousingInternalHeatTransferResults? = null
) : CalculationOutput
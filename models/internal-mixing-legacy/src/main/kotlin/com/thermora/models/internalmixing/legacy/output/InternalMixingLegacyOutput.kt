
package com.thermora.models.internalmixing.legacy.output

import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

/**
 * Results of the internal mixing legacy calculation.
 *
 * Reproduces the final calculation branch
 * of the original Excel methodology.
 *
 * All absolute temperatures use Kelvin.
 */
data class InternalMixingLegacyOutput @JvmOverloads constructor(
    val enclosureTemperature: Temperature,
    val heatedZoneTemperature: Temperature,
    val internalAirTemperature: Temperature,
    val elementTemperature: Temperature,
    val junctionTemperature: Temperature,
    val heatedZoneRise: TemperatureDelta,
    val heatedZoneToAmbientResistance: ThermalResistance,
    val geometryResults: InternalMixingGeometryResults? = null,
    val heatTransferResults: InternalMixingHeatTransferResults? = null
) : CalculationOutput

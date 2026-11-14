
package com.thermora.models.internalmixing.legacy.input

import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

/**
 * Boundary conditions obtained from a successful
 * sealed-housing calculation.
 *
 * This object does not execute the sealed-housing model.
 */
data class SealedHousingBoundarySnapshot(
    val ambient: AmbientConditions,
    val source: ThermalSource,
    val enclosureRise: TemperatureDelta,
    val enclosureTemperature: Temperature
)

package com.thermora.models.finnedhousing.legacy.input

import com.thermora.calculation.api.model.CalculationInput
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.FinGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity

data class FinnedHousingLegacyInput(
    val ambient: AmbientConditions,
    val source: ThermalSource,
    val enclosure: EnclosureGeometry,
    val fins: FinGeometry,
    val heatedZone: HeatSourceGeometry,
    val criticalElement: CriticalElementThermalProperties,
    val elementWithRadiatorArea: Quantity,
    val enclosureEmissivity: Double,
    val externalProbes: ExternalProbeConfiguration,
    val internalProbes: InternalProbeConfiguration
) : CalculationInput

package com.thermora.models.sealedhousing.legacy.input

import com.thermora.calculation.api.model.CalculationInput
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity

data class SealedHousingLegacyInput(
    val ambient: AmbientConditions,
    val source: ThermalSource,
    val heatedZone: HeatSourceGeometry,
    val criticalElement: CriticalElementThermalProperties,
    val elementWithRadiatorArea: Quantity
) : CalculationInput
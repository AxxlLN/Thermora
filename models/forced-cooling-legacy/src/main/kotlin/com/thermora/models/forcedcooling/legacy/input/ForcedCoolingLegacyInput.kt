package com.thermora.models.forcedcooling.legacy.input
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.calculation.api.model.CalculationInput

/** Saved one-step Excel state, no automatic iteration. */
data class ForcedCoolingLegacyInput(
    val ambient: AmbientConditions,
    val source: ThermalSource,
    val enclosure: EnclosureGeometry,
    val heatedZone: HeatSourceGeometry,
    val criticalElement: CriticalElementThermalProperties,
    val elementWithRadiatorArea: Quantity,
    val fanVolumeFlow: Quantity,
    val componentLength: Quantity,
    val trialEnclosureTemperature: Temperature,
    val trialZoneTemperature: Temperature
) : CalculationInput

package com.thermora.models.perforatedhousing.legacy.input
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.calculation.api.model.CalculationInput

/** Manual trial state of sheet «Перфорированный корпус», F3/F4/I6/AA4. No automatic iteration. */
data class PerforatedHousingLegacyInput(
    val ambient: AmbientConditions,
    val source: ThermalSource,
    val enclosure: EnclosureGeometry,
    val heatedZone: HeatSourceGeometry,
    val criticalElement: CriticalElementThermalProperties,
    val elementWithRadiatorArea: Quantity,
    val perforation: PerforationGeometry,
    val trialEnclosureTemperature: Temperature,
    val trialZoneTemperature: Temperature,
    val convectionCoefficient: Quantity,
    val firstIterationPower: Quantity
) : CalculationInput

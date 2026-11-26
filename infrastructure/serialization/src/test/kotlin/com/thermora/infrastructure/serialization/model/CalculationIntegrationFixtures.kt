package com.thermora.infrastructure.serialization.model

import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.FinGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.finnedhousing.legacy.input.ExternalProbeConfiguration
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.input.InternalProbeConfiguration
import com.thermora.models.finnedhousing.legacy.input.InternalWallGap

internal object CalculationIntegrationFixtures {
    fun excelInput(): FinnedHousingLegacyInput = FinnedHousingLegacyInput(
        ambient = AmbientConditions(Temperature.fromKelvin(313.0)),
        source = ThermalSource(Quantity(4.3, StandardUnits.WATT)),
        enclosure = EnclosureGeometry(
            length = Quantity(0.121, StandardUnits.METER),
            width = Quantity(0.078, StandardUnits.METER),
            height = Quantity(0.039, StandardUnits.METER)
        ),
        fins = FinGeometry(
            width = Quantity(0.003, StandardUnits.METER),
            height = Quantity(0.025, StandardUnits.METER),
            length = Quantity(0.039, StandardUnits.METER),
            countPerSide = 10,
            spacing = Quantity(0.00533, StandardUnits.METER)
        ),
        heatedZone = HeatSourceGeometry(
            length = Quantity(0.112, StandardUnits.METER),
            width = Quantity(0.069, StandardUnits.METER),
            height = Quantity(0.0055, StandardUnits.METER),
            volumeFillCoefficient = 0.5
        ),
        criticalElement = CriticalElementThermalProperties(
            power = Quantity(1.25, StandardUnits.WATT),
            transitionToCaseResistance = ThermalResistance(Quantity(5.0, StandardUnits.KELVIN_PER_WATT)),
            caseToRadiatorResistance = ThermalResistance(Quantity(0.5, StandardUnits.KELVIN_PER_WATT))
        ),
        elementWithRadiatorArea = Quantity(0.00423, StandardUnits.SQUARE_METER),
        enclosureEmissivity = 0.9,
        externalProbes = ExternalProbeConfiguration(
            enclosureRises = listOf(5.0, 10.0, 15.0).map { TemperatureDelta.fromKelvin(it) }
        ),
        internalProbes = InternalProbeConfiguration(
            wallGaps = listOf(0.003, 0.003, 0.003, 0.003, 0.0265, 0.004).mapIndexed { index, gap ->
                InternalWallGap(
                    wallIndex = index + 1,
                    gap = Quantity(gap, StandardUnits.METER),
                    convectionMultiplier = listOf(1.0, 1.0, 1.0, 1.0, 0.7, 1.3)[index]
                )
            },
            referenceZoneTemperature = Temperature.fromKelvin(360.63098423378119),
            trialZoneRises = listOf(10.0, 20.0, 40.0).map { TemperatureDelta.fromKelvin(it) },
            trialResistances = listOf(5.0556031543490354, 4.5439787678362187, 4.0195022659471871)
                .map { ThermalResistance(Quantity(it, StandardUnits.KELVIN_PER_WATT)) }
        )
    )
}

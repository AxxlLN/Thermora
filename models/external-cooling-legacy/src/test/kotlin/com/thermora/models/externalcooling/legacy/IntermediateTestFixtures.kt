package com.thermora.models.externalcooling.legacy

import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.domain.ventilation.AirflowConditions
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.externalcooling.legacy.input.*

internal object IntermediateTestFixtures {
    fun length(value: Double) = Quantity(value, StandardUnits.METER)
    fun delta(value: Double) = TemperatureDelta.fromKelvin(value)
    fun resistance(value: Double) = ThermalResistance(Quantity(value, StandardUnits.KELVIN_PER_WATT))
    fun input(trace: Boolean = true): ExternalCoolingLegacyInput {
        val ambient = AmbientConditions(Temperature.fromKelvin(313.0))
        val source = ThermalSource(Quantity(4.3, StandardUnits.WATT))
        val enclosure = EnclosureGeometry(length(0.121), length(0.078), length(0.039))
        val zone = HeatSourceGeometry(length(0.112), length(0.069), length(0.0055), 0.5)
        val element = CriticalElementThermalProperties(Quantity(1.25, StandardUnits.WATT), resistance(5.0), resistance(0.5))
        val area = Quantity(0.00423, StandardUnits.SQUARE_METER)
        val configuration = ExternalCoolingHeatTransferInput(enclosure, 0.9,
            AirflowConditions(Quantity(1.0, StandardUnits.METER_PER_SECOND)),
            listOf(10.0, 20.0, 40.0).map { delta(it) }, Temperature.fromKelvin(339.9496599897106),
            listOf(0.003, 0.003, 0.003, 0.003, 0.0265, 0.004).mapIndexed { index, gap ->
                ExternalCoolingWallGap(index + 1, length(gap)) },
            listOf(10.0, 20.0, 40.0).map { delta(it) },
            listOf(5.0128999826425042, 4.5073135876550134, 3.9767450970870399).map { resistance(it) })
        return ExternalCoolingLegacyInput(ambient, source, zone, element, area, if (trace) configuration else null)
    }
}

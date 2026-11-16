package com.thermora.models.internalmixing.legacy

import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.domain.ventilation.FanSpecification
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.models.internalmixing.legacy.input.*

internal object IntermediateTestFixtures {
    fun length(value: Double) = Quantity(value, StandardUnits.METER)
    fun delta(value: Double) = TemperatureDelta.fromKelvin(value)
    fun resistance(value: Double) = ThermalResistance(Quantity(value, StandardUnits.KELVIN_PER_WATT))
    fun input(trace: Boolean = true): InternalMixingLegacyInput {
        val ambient = AmbientConditions(Temperature.fromKelvin(313.0))
        val source = ThermalSource(Quantity(4.3, StandardUnits.WATT))
        val enclosure = EnclosureGeometry(length(0.121), length(0.078), length(0.039))
        val zone = HeatSourceGeometry(length(0.112), length(0.069), length(0.0055), 0.5)
        val element = CriticalElementThermalProperties(Quantity(1.25, StandardUnits.WATT), resistance(5.0), resistance(0.5))
        val area = Quantity(0.00423, StandardUnits.SQUARE_METER)
        val configuration = InternalMixingHeatTransferInput(enclosure,
            FanSpecification(Quantity(2.05, MeasurementUnit("CFM", PhysicalDimension.VOLUMETRIC_FLOW, 0.00047194745)), length(0.03)),
            0.76737666355170309, listOf(10.0, 20.0, 40.0).map { delta(it) })
        return InternalMixingLegacyInput(SealedHousingBoundarySnapshot(ambient, source,
            delta(10.872614243073194), Temperature.fromKelvin(323.8726142430732)), zone, element, area,
            if (trace) configuration else null)
    }
}

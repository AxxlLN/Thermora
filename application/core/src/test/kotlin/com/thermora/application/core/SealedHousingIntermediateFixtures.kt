package com.thermora.application.core

import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.geometry.WallOrientation
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.input.*

internal object SealedHousingIntermediateFixtures {
    fun length(value: Double) = Quantity(value, StandardUnits.METER)
    fun delta(value: Double) = TemperatureDelta.fromKelvin(value)
    fun resistance(value: Double) = ThermalResistance(Quantity(value, StandardUnits.KELVIN_PER_WATT))
    fun input(trace: Boolean = true): SealedHousingLegacyInput {
        val ambient = AmbientConditions(Temperature.fromKelvin(313.0))
        val source = ThermalSource(Quantity(4.3, StandardUnits.WATT))
        val enclosure = EnclosureGeometry(length(0.121), length(0.078), length(0.039))
        val zone = HeatSourceGeometry(length(0.112), length(0.069), length(0.0055), 0.5)
        val element = CriticalElementThermalProperties(Quantity(1.25, StandardUnits.WATT), resistance(5.0), resistance(0.5))
        val area = Quantity(0.00423, StandardUnits.SQUARE_METER)
        val configuration = SealedHousingHeatTransferInput(enclosure, 0.9,
            delta(10.0), listOf(2.5523502985120694, 2.3095762007089808, 2.037553429294511).map { resistance(it) },
            listOf(10.0, 20.0, 40.0).map { delta(it) }, Temperature.fromKelvin(333.8726142430732),
            listOf(0.003, 0.003, 0.003, 0.003, 0.0265, 0.004).mapIndexed { index, gap ->
                SealedHousingWallGap(index + 1, length(gap), if (index == 4) WallOrientation.HORIZONTAL_TOP
                    else if (index == 5) WallOrientation.HORIZONTAL_BOTTOM else WallOrientation.VERTICAL) },
            listOf(10.0, 20.0, 40.0).map { delta(it) },
            listOf(5.0084308936934487, 4.5077724220544884, 3.9803152672844573).map { resistance(it) })
        return SealedHousingLegacyInput(ambient, source, zone, element, area, if (trace) configuration else null)
    }
}

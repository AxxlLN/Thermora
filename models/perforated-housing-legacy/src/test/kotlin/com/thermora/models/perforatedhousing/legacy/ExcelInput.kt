package com.thermora.models.perforatedhousing.legacy

import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits.METER
import com.thermora.foundation.units.unit.StandardUnits.MILLIMETER
import com.thermora.foundation.units.unit.StandardUnits.WATT
import com.thermora.foundation.units.unit.StandardUnits.SQUARE_METER
import com.thermora.foundation.units.unit.StandardUnits.KELVIN_PER_WATT
import com.thermora.foundation.units.unit.StandardUnits.WATT_PER_SQUARE_METER_KELVIN
import com.thermora.models.perforatedhousing.legacy.input.*

internal fun excelInput() = PerforatedHousingLegacyInput(
    AmbientConditions(Temperature.fromKelvin(313.0)), ThermalSource(Quantity(4.3, WATT)),
    EnclosureGeometry(Quantity(0.121, METER), Quantity(0.078, METER), Quantity(0.039, METER)),
    HeatSourceGeometry(Quantity(0.112, METER), Quantity(0.069, METER), Quantity(0.0055, METER), 0.5),
    CriticalElementThermalProperties(Quantity(1.25, WATT), ThermalResistance(Quantity(5.0, KELVIN_PER_WATT)), ThermalResistance(Quantity(0.5, KELVIN_PER_WATT))),
    Quantity(0.00423, SQUARE_METER), PerforationGeometry(Quantity(0.00365, SQUARE_METER), Quantity(0.00115, SQUARE_METER)),
    Temperature.fromKelvin(320.9), Temperature.fromKelvin(337.1), Quantity(12.0, WATT_PER_SQUARE_METER_KELVIN), Quantity(4.3, WATT)
)

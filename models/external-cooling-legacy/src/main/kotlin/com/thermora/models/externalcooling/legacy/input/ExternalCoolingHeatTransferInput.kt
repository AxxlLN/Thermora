package com.thermora.models.externalcooling.legacy.input

import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.ventilation.AirflowConditions
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class ExternalCoolingHeatTransferInput(
    val enclosure: EnclosureGeometry,
    val enclosureEmissivity: Double,
    val airflow: AirflowConditions,
    val enclosureProbeRises: List<TemperatureDelta>,
    val referenceZoneTemperature: Temperature,
    val wallGaps: List<ExternalCoolingWallGap>,
    val zoneProbeRises: List<TemperatureDelta>,
    val zoneProbeResistances: List<ThermalResistance>
)

data class ExternalCoolingWallGap(
    val wallIndex: Int,
    val gap: Quantity
)

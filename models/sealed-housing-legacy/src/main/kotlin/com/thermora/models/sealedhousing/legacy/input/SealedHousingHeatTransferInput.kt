package com.thermora.models.sealedhousing.legacy.input

import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.WallOrientation
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta

data class SealedHousingHeatTransferInput(
    val enclosure: EnclosureGeometry,
    val enclosureEmissivity: Double,
    val enclosureReferenceRise: TemperatureDelta,
    val enclosureProbeResistances: List<ThermalResistance>,
    val enclosureProbeRises: List<TemperatureDelta>,
    val referenceZoneTemperature: Temperature,
    val wallGaps: List<SealedHousingWallGap>,
    val zoneProbeRises: List<TemperatureDelta>,
    val zoneProbeResistances: List<ThermalResistance>
)

data class SealedHousingWallGap(
    val wallIndex: Int,
    val gap: Quantity,
    val orientation: WallOrientation
)

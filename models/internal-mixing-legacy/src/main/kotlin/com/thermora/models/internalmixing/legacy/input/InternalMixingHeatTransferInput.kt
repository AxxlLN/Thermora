package com.thermora.models.internalmixing.legacy.input

import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.ventilation.FanSpecification
import com.thermora.foundation.units.temperature.TemperatureDelta

data class InternalMixingHeatTransferInput(
    val enclosure: EnclosureGeometry,
    val fan: FanSpecification,
    val reducedEmissivity: Double,
    val zoneProbeRises: List<TemperatureDelta>
)

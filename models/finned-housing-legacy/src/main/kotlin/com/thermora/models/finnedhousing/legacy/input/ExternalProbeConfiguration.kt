package com.thermora.models.finnedhousing.legacy.input

import com.thermora.foundation.units.temperature.TemperatureDelta

data class ExternalProbeConfiguration(
    val enclosureRises: List<TemperatureDelta>
)

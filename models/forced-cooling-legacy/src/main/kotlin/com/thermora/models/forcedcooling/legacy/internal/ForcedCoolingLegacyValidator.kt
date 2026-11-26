package com.thermora.models.forcedcooling.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.models.forcedcooling.legacy.input.ForcedCoolingLegacyInput

internal class ForcedCoolingLegacyValidator {
    fun validate(i: ForcedCoolingLegacyInput): ValidationReport {
        val errors = mutableListOf<Diagnostic>()
        fun requireInput(ok: Boolean, path: String, detail: String, details: Map<String, String> = emptyMap()) {
            if (!ok) errors += Diagnostic("FORCED_COOLING_INVALID_INPUT", detail, DiagnosticSeverity.ERROR, path, details)
        }
        fun quantity(q: Quantity, d: PhysicalDimension, path: String, positive: Boolean = true) {
            requireInput(q.unit.dimension == d && (if (positive) q.baseValue > 0 else q.baseValue >= 0), path, "Wrong dimension or non-positive value")
        }
        quantity(i.elementWithRadiatorArea, PhysicalDimension.AREA, "elementWithRadiatorArea")
        quantity(i.fanVolumeFlow, PhysicalDimension.VOLUMETRIC_FLOW, "fanVolumeFlow")
        quantity(i.componentLength, PhysicalDimension.LENGTH, "componentLength")
        requireInput(i.source.power.baseValue > 0, "source.power", "Total power must be positive (T18 divides by power)")
        requireInput(i.ambient.temperature.kelvin > 0, "ambient.temperature", "Ambient must exceed zero kelvin")
        requireInput(i.trialZoneTemperature.kelvin > i.trialEnclosureTemperature.kelvin, "trialZoneTemperature", "Zone trial must exceed enclosure trial (J8)",
            mapOf("sheet" to "Продуваемый корпус", "cell" to "J8"))
        requireInput(i.trialEnclosureTemperature.kelvin > i.ambient.temperature.kelvin, "trialEnclosureTemperature", "Enclosure trial must exceed ambient")
        requireInput(i.heatedZone.length.baseValue < i.enclosure.length.baseValue && i.heatedZone.width.baseValue < i.enclosure.width.baseValue && i.heatedZone.height.baseValue < i.enclosure.height.baseValue,
            "heatedZone", "Heated zone must fit inside enclosure")
        return ValidationReport(errors)
    }
}

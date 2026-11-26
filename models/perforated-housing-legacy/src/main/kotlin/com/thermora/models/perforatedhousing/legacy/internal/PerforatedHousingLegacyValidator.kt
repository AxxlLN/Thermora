package com.thermora.models.perforatedhousing.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.models.perforatedhousing.legacy.input.PerforatedHousingLegacyInput

internal class PerforatedHousingLegacyValidator {
    fun validate(i: PerforatedHousingLegacyInput): ValidationReport {
        val errors = mutableListOf<Diagnostic>()
        fun requireInput(ok: Boolean, path: String, detail: String) {
            if (!ok) errors += Diagnostic("PERFORATED_HOUSING_INVALID_INPUT", detail, DiagnosticSeverity.ERROR, path)
        }
        fun quantity(q: Quantity, d: PhysicalDimension, path: String, positive: Boolean = true) {
            requireInput(q.unit.dimension == d && (if (positive) q.baseValue > 0 else q.baseValue >= 0), path, "Wrong dimension or non-positive value")
        }
        quantity(i.elementWithRadiatorArea, PhysicalDimension.AREA, "elementWithRadiatorArea")
        quantity(i.convectionCoefficient, PhysicalDimension.HEAT_TRANSFER_COEFFICIENT, "convectionCoefficient")
        quantity(i.firstIterationPower, PhysicalDimension.POWER, "firstIterationPower", false)
        requireInput(i.source.power.baseValue > 0, "source.power", "Total power must be positive (T18 divides by power)")
        requireInput(i.ambient.temperature.kelvin > 0, "ambient.temperature", "Ambient must exceed zero kelvin")
        requireInput(i.trialZoneTemperature.kelvin > i.trialEnclosureTemperature.kelvin, "trialZoneTemperature", "Zone trial must exceed enclosure trial (J9)")
        requireInput(i.trialEnclosureTemperature.kelvin > i.ambient.temperature.kelvin, "trialEnclosureTemperature", "Enclosure trial must exceed ambient")
        requireInput(i.heatedZone.length.baseValue < i.enclosure.length.baseValue && i.heatedZone.width.baseValue < i.enclosure.width.baseValue && i.heatedZone.height.baseValue < i.enclosure.height.baseValue,
            "heatedZone", "Heated zone must fit inside enclosure")
        val area = 2 * (i.enclosure.length.baseValue * i.enclosure.width.baseValue + i.enclosure.height.baseValue * (i.enclosure.length.baseValue + i.enclosure.width.baseValue))
        requireInput(i.perforation.openArea.baseValue <= area && i.perforation.transitionArea.baseValue <= area, "perforation", "Opening areas cannot exceed the enclosure surface")
        return ValidationReport(errors)
    }
}

package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import java.math.BigDecimal

internal class FinnedHousingLegacyValidator :
    ValidationRule<FinnedHousingLegacyInput> {

    override fun validate(
        input: FinnedHousingLegacyInput
    ): ValidationReport {

        val diagnostics = mutableListOf<Diagnostic>()

        fun error(
            code: String,
            message: String,
            source: String
        ) {
            diagnostics += Diagnostic(
                code = code,
                message = message,
                severity = DiagnosticSeverity.ERROR,
                source = source
            )
        }

        // Total thermal power
        if (input.source.power.baseValue <= 0.0) {
            error(
                "FINNED_HOUSING_INVALID_TOTAL_POWER",
                "Total thermal power must be positive",
                "source.power"
            )
        }

        // Element area
        val elementArea = input.elementWithRadiatorArea

        if (
            elementArea.unit.dimension != PhysicalDimension.AREA ||
            elementArea.baseValue <= 0.0
        ) {
            error(
                "FINNED_HOUSING_INVALID_ELEMENT_AREA",
                "Element area must be positive and have area dimension",
                "elementWithRadiatorArea"
            )
        }

        // Enclosure emissivity
        if (
            !input.enclosureEmissivity.isFinite() ||
            input.enclosureEmissivity <= 0.0 ||
            input.enclosureEmissivity > 1.0
        ) {
            error(
                "FINNED_HOUSING_INVALID_EMISSIVITY",
                "Enclosure emissivity must be in range (0, 1]",
                "enclosureEmissivity"
            )
        }

        // Fin packing
        val fins = input.fins
        val occupiedWidth =
            BigDecimal(fins.width.baseValue).multiply(BigDecimal(fins.countPerSide))
                .add(BigDecimal(fins.spacing.baseValue).multiply(BigDecimal(fins.countPerSide - 1)))

        if (occupiedWidth > BigDecimal(input.enclosure.width.baseValue)) {
            diagnostics += Diagnostic(
                code = "FINNED_HOUSING_INVALID_FIN_PACKING",
                message = "Fins do not fit within enclosure width",
                severity = DiagnosticSeverity.WARNING,
                source = "fins"
            )
        }

        // External probes
        val externalRises = input.externalProbes.enclosureRises

        if (
            externalRises.size != 3 ||
            externalRises.any {
                !it.kelvin.isFinite() || it.kelvin <= 0.0
            }
        ) {
            error(
                "FINNED_HOUSING_INVALID_EXTERNAL_PROBES",
                "Exactly three positive external trial rises are required",
                "externalProbes.enclosureRises"
            )
        }

        // Internal wall gaps
        val walls = input.internalProbes.wallGaps

        if (
            walls.size != 6 ||
            walls.map { it.wallIndex }.toSet() != (1..6).toSet()
        ) {
            error(
                "FINNED_HOUSING_INVALID_WALL_CONFIGURATION",
                "Exactly six uniquely indexed walls (1..6) are required",
                "internalProbes.wallGaps"
            )
        }

        walls.forEach { wall ->
            if (
                wall.gap.unit.dimension != PhysicalDimension.LENGTH ||
                wall.gap.baseValue <= 0.0 ||
                !wall.convectionMultiplier.isFinite() ||
                wall.convectionMultiplier <= 0.0
            ) {
                error(
                    "FINNED_HOUSING_INVALID_WALL_PARAMETERS",
                    "Wall gap must be a positive length and multiplier must be positive",
                    "internalProbes.wallGaps[${wall.wallIndex}]"
                )
            }
        }

        // Internal trial values
        val internal = input.internalProbes

        if (
            internal.trialZoneRises.size != 3 ||
            internal.trialResistances.size != 3
        ) {
            error(
                "FINNED_HOUSING_INVALID_INTERNAL_PROBES",
                "Exactly three trial rises and resistances are required",
                "internalProbes"
            )
        }

        if (
            internal.trialZoneRises.any {
                !it.kelvin.isFinite() || it.kelvin <= 0.0
            }
        ) {
            error(
                "FINNED_HOUSING_INVALID_INTERNAL_RISE",
                "Internal trial rises must be positive",
                "internalProbes.trialZoneRises"
            )
        }

        if (
            internal.trialResistances.any {
                !it.value.baseValue.isFinite() ||
                        it.value.baseValue <= 0.0
            }
        ) {
            error(
                "FINNED_HOUSING_INVALID_INTERNAL_RESISTANCE",
                "Internal trial resistances must be positive",
                "internalProbes.trialResistances"
            )
        }

        if (internal.referenceZoneTemperature.kelvin <= input.ambient.temperature.kelvin) {
            error(
                "FINNED_HOUSING_INVALID_REFERENCE_TEMPERATURE",
                "Reference zone temperature must exceed ambient temperature",
                "internalProbes.referenceZoneTemperature"
            )
        }

        return ValidationReport(diagnostics)
    }
}

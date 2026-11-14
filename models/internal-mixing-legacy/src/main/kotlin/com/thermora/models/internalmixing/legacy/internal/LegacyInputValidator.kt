package com.thermora.models.internalmixing.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.calculation.validation.rules.PositiveNumberRule
import com.thermora.calculation.validation.validator.CompositeValidator
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import kotlin.math.abs
import kotlin.math.max

internal class LegacyInputValidator(
    private val source: String
) : ValidationRule<InternalMixingLegacyInput> {

    private val validator = CompositeValidator(
        listOf(
            positive(
                "totalPower",
                "INVALID_TOTAL_POWER"
            ) { it.sealedHousing.source.power.baseValue },

            positive(
                "elementWithRadiatorArea",
                "INVALID_ELEMENT_AREA"
            ) { it.elementWithRadiatorArea.baseValue },

            ValidationRule { input ->
                if (input.elementWithRadiatorArea.unit.dimension == PhysicalDimension.AREA) {
                    ValidationReport.valid()
                } else {
                    invalid(
                        "INVALID_ELEMENT_AREA",
                        "Element with radiator must have area dimension"
                    )
                }
            },


            ValidationRule { input ->
                val elementPower = input.criticalElement.power.baseValue

                if (
                    elementPower.isFinite() &&
                    elementPower >= 0.0
                ) {
                    ValidationReport.valid()
                } else {
                    invalid(
                        "INVALID_ELEMENT_POWER",
                        "Critical element power must be finite and non-negative"
                    )
                }
            },

            ValidationRule { input ->
                val rise = input.sealedHousing.enclosureRise.kelvin

                if (rise.isFinite() && rise >= 0.0) {
                    ValidationReport.valid()
                } else {
                    invalid(
                        "INVALID_ENCLOSURE_RISE",
                        "Enclosure temperature rise must be finite and non-negative"
                    )
                }
            },

            ValidationRule { input ->
                val ambient = input.sealedHousing.ambient.temperature.kelvin
                val rise = input.sealedHousing.enclosureRise.kelvin
                val enclosure = input.sealedHousing.enclosureTemperature.kelvin

                val expected = ambient + rise

                val scale = max(
                    1.0,
                    max(abs(expected), abs(enclosure))
                )

                val tolerance = 1e-12 * scale

                if (
                    ambient.isFinite() &&
                    enclosure.isFinite() &&
                    expected.isFinite() &&
                    abs(enclosure - expected) <= tolerance
                ) {
                    ValidationReport.valid()
                } else {
                    invalid(
                        "INCONSISTENT_ENCLOSURE_TEMPERATURE",
                        "Enclosure temperature must equal ambient temperature plus enclosure rise"
                    )
                }
            }
        )
    )

    override fun validate(
        input: InternalMixingLegacyInput
    ): ValidationReport =
        ValidationReport(validator.validate(input).diagnostics.take(1))

    private fun positive(
        field: String,
        code: String,
        selector: (InternalMixingLegacyInput) -> Double
    ): ValidationRule<InternalMixingLegacyInput> {

        val rule = PositiveNumberRule(field, selector)

        return ValidationRule { input ->
            val report = rule.validate(input)

            if (report.isValid) {
                ValidationReport.valid()
            } else {
                invalid(
                    code,
                    "$field must be positive and finite"
                )
            }
        }
    }

    private fun invalid(
        code: String,
        message: String
    ): ValidationReport =
        ValidationReport.of(
            Diagnostic(
                code = code,
                message = message,
                severity = DiagnosticSeverity.ERROR,
                source = source
            )
        )
}

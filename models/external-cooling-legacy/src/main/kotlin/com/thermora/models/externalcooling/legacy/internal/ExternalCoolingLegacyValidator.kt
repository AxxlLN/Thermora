package com.thermora.models.externalcooling.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.calculation.validation.rules.PositiveNumberRule
import com.thermora.calculation.validation.validator.CompositeValidator
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput

internal class ExternalCoolingLegacyValidator :
    ValidationRule<ExternalCoolingLegacyInput> {

    private val validator = CompositeValidator(
        listOf(
            withDiagnostic(
                PositiveNumberRule(
                    field = "totalPower",
                    selector = { it.source.power.baseValue }
                ),
                "EXTERNAL_COOLING_INVALID_TOTAL_POWER",
                "Total thermal power must be finite and positive"
            ),
            ValidationRule { input ->
                val area = input.elementWithRadiatorArea

                if (area.unit.dimension == PhysicalDimension.AREA) {
                    ValidationReport.valid()
                } else {
                    ValidationReport.of(
                        Diagnostic(
                            code = "EXTERNAL_COOLING_INVALID_ELEMENT_AREA",
                            message = "Element area must have area dimension",
                            severity = DiagnosticSeverity.ERROR,
                            source = "elementWithRadiatorArea"
                        )
                    )
                }
            },
            withDiagnostic(
                PositiveNumberRule(
                    field = "elementWithRadiatorArea",
                    selector = { it.elementWithRadiatorArea.baseValue }
                ),
                "EXTERNAL_COOLING_INVALID_ELEMENT_AREA",
                "Element area must be finite and positive"
            )
        )
    )

    override fun validate(
        input: ExternalCoolingLegacyInput
    ): ValidationReport =
        ValidationReport(validator.validate(input).diagnostics.take(1))

    private fun withDiagnostic(
        rule: ValidationRule<ExternalCoolingLegacyInput>,
        code: String,
        message: String
    ): ValidationRule<ExternalCoolingLegacyInput> =
        ValidationRule { input ->
            val report = rule.validate(input)

            if (report.isValid) {
                report
            } else {
                ValidationReport.of(
                    Diagnostic(
                        code = code,
                        message = message,
                        severity = DiagnosticSeverity.ERROR
                    )
                )
            }
        }
}

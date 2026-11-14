package com.thermora.models.sealedhousing.legacy.internal

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.validation.rule.ValidationRule
import com.thermora.calculation.validation.rules.PositiveNumberRule
import com.thermora.calculation.validation.validator.CompositeValidator
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput

internal class LegacyInputValidator(source: String) : ValidationRule<SealedHousingLegacyInput> {

    private val powerDiagnostic = Diagnostic(
        code = "INVALID_TOTAL_POWER",
        message = "Total dissipated power must be positive and finite",
        severity = DiagnosticSeverity.ERROR,
        source = source
    )

    private val areaDiagnostic = Diagnostic(
        code = "INVALID_ELEMENT_AREA",
        message = "Element with radiator area must be positive and have area dimension",
        severity = DiagnosticSeverity.ERROR,
        source = source
    )

    private val validator = CompositeValidator(
        listOf(
            withLegacyDiagnostic(
                PositiveNumberRule<SealedHousingLegacyInput>("source.power") { it.source.power.baseValue },
                powerDiagnostic
            ),
            ValidationRule<SealedHousingLegacyInput> { input ->
                if (input.elementWithRadiatorArea.unit.dimension == PhysicalDimension.AREA) {
                    ValidationReport.valid()
                } else {
                    ValidationReport.of(areaDiagnostic)
                }
            },
            withLegacyDiagnostic(
                PositiveNumberRule<SealedHousingLegacyInput>("elementWithRadiatorArea") {
                    it.elementWithRadiatorArea.baseValue
                },
                areaDiagnostic
            )
        )
    )

    override fun validate(input: SealedHousingLegacyInput): ValidationReport =
        ValidationReport(validator.validate(input).diagnostics.take(1))

    private fun withLegacyDiagnostic(
        rule: ValidationRule<SealedHousingLegacyInput>,
        diagnostic: Diagnostic
    ): ValidationRule<SealedHousingLegacyInput> = ValidationRule { input ->
        val report = rule.validate(input)
        if (report.isValid) report else ValidationReport.of(diagnostic)
    }
}

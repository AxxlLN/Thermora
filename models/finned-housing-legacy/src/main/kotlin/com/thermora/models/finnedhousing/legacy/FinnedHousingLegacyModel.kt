package com.thermora.models.finnedhousing.legacy

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.internal.FinnedHousingLegacyValidator
import com.thermora.models.finnedhousing.legacy.internal.LegacyCalculationException
import com.thermora.models.finnedhousing.legacy.internal.LegacyExternalHeatTransfer
import com.thermora.models.finnedhousing.legacy.internal.LegacyFinalCalculations
import com.thermora.models.finnedhousing.legacy.internal.LegacyGeometry
import com.thermora.models.finnedhousing.legacy.internal.LegacyInternalHeatTransfer
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingLegacyOutput

class FinnedHousingLegacyModel : CalculationModel<FinnedHousingLegacyInput, FinnedHousingLegacyOutput> {
    override val descriptor = CalculationModelDescriptor(
        CalculationModelId("finned-housing-legacy"), "Finned Housing (Legacy)", "1.0.0",
        "Legacy Excel finned housing model with explicit trial heat transfer and fixed final correlations"
    )
    override val inputType = FinnedHousingLegacyInput::class
    override val outputType = FinnedHousingLegacyOutput::class
    private val validator = FinnedHousingLegacyValidator()

    override fun validate(input: FinnedHousingLegacyInput): ValidationReport = validateSnapshot(snapshot(input))

    override fun calculate(input: FinnedHousingLegacyInput): CalculationResult<FinnedHousingLegacyOutput> {
        val request = snapshot(input)
        val validation = validateSnapshot(request)
        if (!validation.isValid) return CalculationResult.Failure(validation.diagnostics)
        return try {
            val geometry = LegacyGeometry.calculate(request)
            val final = LegacyFinalCalculations.calculate(request, geometry)
            val external = LegacyExternalHeatTransfer.calculate(request, geometry)
            val internal = LegacyInternalHeatTransfer.calculate(request, geometry, final.enclosureTemperature)
            val diagnostics = mutableListOf(
                warning("FINNED_HOUSING_LEGACY_APPLICABILITY_UNKNOWN",
                    "The applicability range of the empirical Excel correlations has not been established", "AC12,AC25"),
                warning("FINNED_HOUSING_LEGACY_FIXED_CORRELATIONS",
                    "Final temperature rises use fixed power correlations and are not recalibrated from fin geometry or trial heat transfer", "AC12,AC25")
            )
            external.probes.forEachIndexed { index, probe ->
                if (probe.channelCorrection > 1.0) diagnostics.add(warning(
                    "FINNED_HOUSING_CHANNEL_CORRECTION_OUTSIDE_RANGE",
                    "Channel correction exceeds one and predicts channel air below ambient temperature", "L${index + 5}"))
            }
            CalculationResult.Success(FinnedHousingLegacyOutput(final, geometry, external, internal), diagnostics)
        } catch (exception: LegacyCalculationException) {
            CalculationResult.Failure(listOf(calculationError(exception)))
        }
    }

    private fun validateSnapshot(input: FinnedHousingLegacyInput): ValidationReport {
        val report = validator.validate(input)
        if (!report.isValid) return report
        return try {
            val enclosure = LegacyFinalCalculations.enclosureTemperature(input)
            if (input.internalProbes.referenceZoneTemperature.kelvin <= enclosure.kelvin) {
                ValidationReport(report.diagnostics + Diagnostic(
                    code = "FINNED_HOUSING_INVALID_REFERENCE_TEMPERATURE",
                    message = "Reference zone temperature must exceed calculated enclosure temperature",
                    severity = DiagnosticSeverity.ERROR,
                    source = "internalProbes.referenceZoneTemperature",
                    details = mapOf("cell" to "H16-G16", "enclosureKelvin" to enclosure.kelvin.toString())
                ))
            } else report
        } catch (exception: LegacyCalculationException) {
            ValidationReport(report.diagnostics + calculationError(exception))
        }
    }

    private fun snapshot(input: FinnedHousingLegacyInput): FinnedHousingLegacyInput = input.copy(
        externalProbes = input.externalProbes.copy(enclosureRises = input.externalProbes.enclosureRises.toList()),
        internalProbes = input.internalProbes.copy(wallGaps = input.internalProbes.wallGaps.toList(),
            trialZoneRises = input.internalProbes.trialZoneRises.toList(),
            trialResistances = input.internalProbes.trialResistances.toList())
    )

    private fun calculationError(exception: LegacyCalculationException) = Diagnostic(
        code = "FINNED_HOUSING_CALCULATION_ERROR", message = exception.message ?: "Invalid numerical result",
        severity = DiagnosticSeverity.ERROR, source = descriptor.id.value,
        details = mapOf("sheet" to "Оребрение герметичного корпуса", "cell" to exception.cell)
    )

    private fun warning(code: String, message: String, cell: String) = Diagnostic(
        code, message, DiagnosticSeverity.WARNING, descriptor.id.value,
        mapOf("sheet" to "Оребрение герметичного корпуса", "cell" to cell)
    )
}

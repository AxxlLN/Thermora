package com.thermora.models.externalcooling.legacy

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.internal.LegacyIntermediateCalculations
import com.thermora.models.externalcooling.legacy.internal.ExternalCoolingLegacyValidator
import com.thermora.models.externalcooling.legacy.internal.LegacyEnclosureRiseCurve
import com.thermora.models.externalcooling.legacy.internal.LegacyEnclosureResistance
import com.thermora.models.externalcooling.legacy.internal.LegacyHeatedZoneRiseCurve
import com.thermora.models.externalcooling.legacy.internal.LegacyHeatedZoneResistance
import com.thermora.models.externalcooling.legacy.internal.LegacyTemperatureCalculator
import com.thermora.models.externalcooling.legacy.internal.LegacyHeatedZoneArea
import com.thermora.models.externalcooling.legacy.internal.LegacyElementTemperature
import com.thermora.models.externalcooling.legacy.internal.LegacyJunctionTemperature
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput
import kotlin.reflect.KClass

/**
 * Legacy external cooling thermal model.
 *
 * Reproduces Excel calculations:
 * AA11, AA12, AA23, AA24, AA27-AA31.
 */
class ExternalCoolingLegacyModel :
    CalculationModel<ExternalCoolingLegacyInput, ExternalCoolingLegacyOutput> {

    private val inputValidator = ExternalCoolingLegacyValidator()

    override val descriptor = CalculationModelDescriptor(
        id = CalculationModelId("external-cooling-legacy"),
        name = "External Cooling Legacy",
        version = "1.0.0",
        description = "Legacy external cooling thermal model based on Excel"
    )

    override val inputType: KClass<ExternalCoolingLegacyInput> =
        ExternalCoolingLegacyInput::class

    override val outputType: KClass<ExternalCoolingLegacyOutput> =
        ExternalCoolingLegacyOutput::class

    override fun validate(
        input: ExternalCoolingLegacyInput
    ): ValidationReport = inputValidator.validate(input).merge(LegacyIntermediateCalculations.validate(input))

    override fun calculate(
        input: ExternalCoolingLegacyInput
    ): CalculationResult<ExternalCoolingLegacyOutput> {

        val validation = validate(input)

        if (!validation.isValid) {
            return CalculationResult.Failure(validation.diagnostics)
        }

        return try {
            val ambientTemperature = input.ambient.temperature
            val totalPowerWatts = input.source.power.baseValue
            val elementPowerWatts = input.criticalElement.power.baseValue

            // Excel AA11
            val enclosureRise = LegacyEnclosureRiseCurve.calculate(
                totalPowerWatts
            )

            // Excel AA12
            val enclosureResistance = LegacyEnclosureResistance.calculate(
                enclosureRise = enclosureRise,
                totalPowerWatts = totalPowerWatts
            )

            // Excel AA23
            val heatedZoneRise = LegacyHeatedZoneRiseCurve.calculate(
                enclosureRise = enclosureRise,
                totalPowerWatts = totalPowerWatts
            )

            // Excel AA24
            val heatedZoneResistance = LegacyHeatedZoneResistance.calculate(
                heatedZoneRise = heatedZoneRise,
                totalPowerWatts = totalPowerWatts
            )

            // Excel AA27
            val enclosureTemperature =
                LegacyTemperatureCalculator.enclosureTemperature(
                    ambient = ambientTemperature,
                    enclosureRise = enclosureRise
                )

            // Excel AA28
            val heatedZoneTemperature =
                LegacyTemperatureCalculator.heatedZoneTemperature(
                    ambient = ambientTemperature,
                    heatedZoneRise = heatedZoneRise
                )

            // Excel AA29
            val internalAirTemperature =
                LegacyTemperatureCalculator.internalAirTemperature(
                    enclosure = enclosureTemperature,
                    heatedZone = heatedZoneTemperature
                )

            // Effective heated-zone area
            val heatedZoneArea = LegacyHeatedZoneArea.calculate(
                lengthMeters = input.heatedZone.length.baseValue,
                widthMeters = input.heatedZone.width.baseValue,
                heightMeters = input.heatedZone.height.baseValue,
                volumeFillCoefficient =
                    input.heatedZone.volumeFillCoefficient
            )

            // Excel AA30
            val elementTemperature = LegacyElementTemperature.calculate(
                ambient = ambientTemperature,
                heatedZoneRise = heatedZoneRise,
                elementPowerWatts = elementPowerWatts,
                heatedZoneAreaSquareMeters = heatedZoneArea,
                elementAreaSquareMeters =
                    input.elementWithRadiatorArea.baseValue,
                totalPowerWatts = totalPowerWatts
            )

            // Excel AA31
            val junctionTemperature = LegacyJunctionTemperature.calculate(
                elementTemperature = elementTemperature,
                elementPowerWatts = elementPowerWatts,
                junctionToCaseResistance =
                    input.criticalElement
                        .transitionToCaseResistance.value.baseValue,
                caseToRadiatorResistance =
                    input.criticalElement
                        .caseToRadiatorResistance.value.baseValue
            )

            val output = ExternalCoolingLegacyOutput(
                enclosureTemperature = enclosureTemperature,
                heatedZoneTemperature = heatedZoneTemperature,
                internalAirTemperature = internalAirTemperature,
                elementTemperature = elementTemperature,
                junctionTemperature = junctionTemperature,
                enclosureRise = enclosureRise,
                heatedZoneRise = heatedZoneRise,
                enclosureToAmbientResistance = enclosureResistance,
                heatedZoneToAmbientResistance = heatedZoneResistance
            )

            CalculationResult.Success(
                LegacyIntermediateCalculations.attach(input, output),
                listOf(
                    Diagnostic(
                        code = "EXTERNAL_COOLING_LEGACY_APPLICABILITY_UNKNOWN",
                        message = "The applicability range of the empirical Excel correlations has not been established",
                        severity = DiagnosticSeverity.WARNING,
                        source = descriptor.id.value
                    )
                ) + LegacyIntermediateCalculations.warnings(input)
            )
        } catch (exception: IllegalArgumentException) {
            CalculationResult.Failure(
                listOf(
                    Diagnostic(
                        code = "EXTERNAL_COOLING_CALCULATION_ERROR",
                        message = exception.message ?: "Invalid external cooling calculation parameters",
                        severity = DiagnosticSeverity.ERROR,
                        source = descriptor.id.value
                    )
                )
            )
        }
    }
}

package com.thermora.models.internalmixing.legacy

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import com.thermora.models.internalmixing.legacy.internal.LegacyIntermediateCalculations
import com.thermora.models.internalmixing.legacy.internal.LegacyElementTemperature
import com.thermora.models.internalmixing.legacy.internal.LegacyHeatedZoneArea
import com.thermora.models.internalmixing.legacy.internal.LegacyInputValidator
import com.thermora.models.internalmixing.legacy.internal.LegacyTemperatureArithmetic
import com.thermora.models.internalmixing.legacy.internal.LegacyZoneRiseCurve
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput

class InternalMixingLegacyModel :
    CalculationModel<InternalMixingLegacyInput, InternalMixingLegacyOutput> {

    override val descriptor = CalculationModelDescriptor(
        id = CalculationModelId("internal-mixing-legacy"),
        name = "Internal Mixing (Legacy)",
        version = "1.0.0",
        description = "Empirical internal mixing thermal model reproducing the original Excel methodology. Junction temperature is based on heated-zone temperature."
    )

    override val inputType = InternalMixingLegacyInput::class
    override val outputType = InternalMixingLegacyOutput::class

    private val inputValidator = LegacyInputValidator(descriptor.id.value)

    override fun validate(
        input: InternalMixingLegacyInput
    ): ValidationReport = inputValidator.validate(input).merge(LegacyIntermediateCalculations.validate(input))

    override fun calculate(
        input: InternalMixingLegacyInput
    ): CalculationResult<InternalMixingLegacyOutput> {

        val validation = validate(input)

        if (!validation.isValid) {
            return CalculationResult.Failure(validation.diagnostics)
        }

        return try {
            val boundary = input.sealedHousing

            val totalPower = boundary.source.power.baseValue
            val elementPower = input.criticalElement.power.baseValue

            // Excel: AB15
            val zoneRise = LegacyZoneRiseCurve.calculate(
                totalPowerWatts = totalPower,
                enclosureRise = boundary.enclosureRise
            )

            // Excel: AB16
            val zoneResistanceValue = zoneRise.kelvin / totalPower

            require(
                zoneResistanceValue.isFinite() &&
                        zoneResistanceValue > 0.0
            ) {
                "Heated-zone resistance must be positive and finite"
            }

            val zoneResistance = ThermalResistance(
                Quantity(
                    zoneResistanceValue,
                    StandardUnits.KELVIN_PER_WATT
                )
            )

            // Excel: AB19
            val enclosureTemperature =
                boundary.enclosureTemperature

            // Excel: AB20
            val zoneTemperature =
                LegacyTemperatureArithmetic.addRise(
                    boundary.ambient.temperature,
                    zoneRise
                )

            // Excel: AB21
            val airTemperature =
                LegacyTemperatureArithmetic.meanTemperature(
                    enclosureTemperature,
                    zoneTemperature
                )

            // Excel: "Исходные данные"!V12
            val zoneArea =
                LegacyHeatedZoneArea.calculate(input.heatedZone)

            // Excel: AB22
            val elementTemperature =
                LegacyElementTemperature.calculateElement(
                    ambientTemperature = boundary.ambient.temperature,
                    heatedZoneRise = zoneRise,
                    heatedZoneArea = zoneArea,
                    elementPowerWatts = elementPower,
                    totalPowerWatts = totalPower,
                    elementArea = input.elementWithRadiatorArea.baseValue
                )

            // Excel: AB23
            // Legacy C07: junction uses zone temperature,
            // not element temperature.
            val junctionTemperature =
                LegacyElementTemperature.calculateJunction(
                    heatedZoneTemperature = zoneTemperature,
                    elementPowerWatts = elementPower,
                    transitionToCaseResistance =
                        input.criticalElement
                            .transitionToCaseResistance
                            .value.baseValue,
                    caseToRadiatorResistance =
                        input.criticalElement
                            .caseToRadiatorResistance
                            .value.baseValue
                )

            val output = InternalMixingLegacyOutput(
                enclosureTemperature = enclosureTemperature,
                heatedZoneTemperature = zoneTemperature,
                internalAirTemperature = airTemperature,
                elementTemperature = elementTemperature,
                junctionTemperature = junctionTemperature,
                heatedZoneRise = zoneRise,
                heatedZoneToAmbientResistance = zoneResistance
            )

            CalculationResult.Success(
                value = LegacyIntermediateCalculations.attach(input, output),
                diagnostics = listOf(
                    Diagnostic(
                        code = "INTERNAL_MIXING_LEGACY_APPLICABILITY_UNKNOWN",
                        message = "The applicability range of the empirical internal mixing correlation has not been established",
                        severity = DiagnosticSeverity.WARNING,
                        source = descriptor.id.value
                    ),
                    Diagnostic(
                        code = "INTERNAL_MIXING_LEGACY_JUNCTION_USES_ZONE",
                        message = "Legacy Excel calculates junction temperature from heated-zone temperature rather than element temperature",
                        severity = DiagnosticSeverity.WARNING,
                        source = descriptor.id.value
                    )
                ) + LegacyIntermediateCalculations.warnings(input)
            )

        } catch (exception: IllegalArgumentException) {
            failure(
                code = "INTERNAL_MIXING_CALCULATION_ERROR",
                message = exception.message
                    ?: "Invalid internal mixing calculation parameters"
            )
        }
    }

    private fun failure(
        code: String,
        message: String
    ): CalculationResult.Failure =
        CalculationResult.Failure(
            diagnostics = listOf(
                Diagnostic(
                    code = code,
                    message = message,
                    severity = DiagnosticSeverity.ERROR,
                    source = descriptor.id.value
                )
            )
        )
}

package com.thermora.models.sealedhousing.legacy

import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.internal.LegacyCaseRiseCurve
import com.thermora.models.sealedhousing.legacy.internal.LegacyZoneRiseCurve
import com.thermora.models.sealedhousing.legacy.internal.LegacyElementTemperature
import com.thermora.models.sealedhousing.legacy.internal.LegacyTemperatureArithmetic
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput

class SealedHousingLegacyModel :
    CalculationModel<SealedHousingLegacyInput, SealedHousingLegacyOutput> {

    override val descriptor = CalculationModelDescriptor(
        id = CalculationModelId("sealed-housing-legacy"),
        name = "Sealed Housing (Legacy)",
        version = "1.0.0",
        description = "Empirical sealed housing thermal model based on the original Excel methodology"
    )

    override val inputType = SealedHousingLegacyInput::class
    override val outputType = SealedHousingLegacyOutput::class

    override fun calculate(
        input: SealedHousingLegacyInput
    ): CalculationResult<SealedHousingLegacyOutput> {

        val power = input.source.power.baseValue
        val elementArea = input.elementWithRadiatorArea

        if (!power.isFinite() || power <= 0.0) {
            return failure(
                "INVALID_TOTAL_POWER",
                "Total dissipated power must be positive and finite"
            )
        }

        if (
            elementArea.unit.dimension != PhysicalDimension.AREA ||
            !elementArea.baseValue.isFinite() ||
            elementArea.baseValue <= 0.0
        ) {
            return failure(
                "INVALID_ELEMENT_AREA",
                "Element with radiator area must be positive and have area dimension"
            )
        }

        return try {
            val enclosureRise =
                LegacyCaseRiseCurve.calculate(power)

            val zoneRise =
                LegacyZoneRiseCurve.calculate(power, enclosureRise)

            val ambientKelvin = input.ambient.temperature.kelvin

            val enclosureTemperature = Temperature.fromKelvin(
                ambientKelvin + enclosureRise.kelvin
            )

            val zoneTemperature = Temperature.fromKelvin(
                ambientKelvin + zoneRise.kelvin
            )

            val airTemperature = Temperature.fromKelvin(
                LegacyTemperatureArithmetic.mean(enclosureTemperature.kelvin, zoneTemperature.kelvin)
            )

            val elementTemperature =
                LegacyElementTemperature.calculate(
                    ambientTemperature = input.ambient.temperature,
                    heatedZoneRise = zoneRise,
                    totalPowerWatts = power,
                    geometry = input.heatedZone,
                    criticalElement = input.criticalElement,
                    elementWithRadiatorArea = elementArea
                )

            val junctionTemperature =
                LegacyElementTemperature.calculateJunction(
                    elementTemperature,
                    input.criticalElement
                )

            val enclosureResistance = ThermalResistance(
                Quantity(
                    enclosureRise.kelvin / power,
                    StandardUnits.KELVIN_PER_WATT
                )
            )

            val zoneResistance = ThermalResistance(
                Quantity(
                    zoneRise.kelvin / power,
                    StandardUnits.KELVIN_PER_WATT
                )
            )

            val output = SealedHousingLegacyOutput(
                enclosureTemperature = enclosureTemperature,
                heatedZoneTemperature = zoneTemperature,
                internalAirTemperature = airTemperature,
                elementTemperature = elementTemperature,
                junctionTemperature = junctionTemperature,
                enclosureRise = enclosureRise,
                heatedZoneRise = zoneRise,
                enclosureToAmbientResistance = enclosureResistance,
                heatedZoneToAmbientResistance = zoneResistance
            )

            CalculationResult.Success(
                value = output,
                diagnostics = listOf(
                    Diagnostic(
                        code = "SEALED_LEGACY_APPLICABILITY_UNKNOWN",
                        message = "The applicability range of the empirical Excel correlations has not been established",
                        severity = DiagnosticSeverity.WARNING,
                        source = descriptor.id.value
                    )
                )
            )

        } catch (exception: IllegalArgumentException) {
            failure(
                "SEALED_LEGACY_CALCULATION_ERROR",
                exception.message ?: "Invalid calculation parameters"
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

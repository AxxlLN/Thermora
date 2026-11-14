package com.thermora.models.sealedhousing.legacy.internal

import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.dimension.PhysicalDimension

internal object LegacyElementTemperature {

    private const val BASE_FACTOR = 0.75
    private const val POWER_FACTOR = 0.25

    fun calculate(
        ambientTemperature: Temperature,
        heatedZoneTemperature: Temperature,
        totalPowerWatts: Double,
        geometry: HeatSourceGeometry,
        criticalElement: CriticalElementThermalProperties,
        elementWithRadiatorArea: Quantity
    ): Temperature {

        require(totalPowerWatts.isFinite() && totalPowerWatts > 0.0) {
            "Total power must be positive and finite"
        }

        require(elementWithRadiatorArea.unit.dimension == PhysicalDimension.AREA) {
            "Element with radiator area must have area dimension"
        }

        val elementArea = elementWithRadiatorArea.baseValue

        require(elementArea.isFinite() && elementArea > 0.0) {
            "Element with radiator area must be positive and finite"
        }

        val zoneArea = calculateZoneArea(geometry)
        val elementPower = criticalElement.power.baseValue

        val powerRatio =
            elementPower * zoneArea / (elementArea * totalPowerWatts)

        val temperatureFactor =
            BASE_FACTOR + POWER_FACTOR * powerRatio

        val elementTemperatureKelvin =
            ambientTemperature.kelvin +
                    (heatedZoneTemperature.kelvin - ambientTemperature.kelvin) *
                    temperatureFactor

        require(elementTemperatureKelvin.isFinite() && elementTemperatureKelvin >= 0.0) {
            "Calculated element temperature must be finite and non-negative"
        }

        return Temperature.fromKelvin(elementTemperatureKelvin)
    }

    fun calculateJunction(
        elementTemperature: Temperature,
        criticalElement: CriticalElementThermalProperties
    ): Temperature {

        val elementPower = criticalElement.power.baseValue

        val transitionResistance =
            criticalElement.transitionToCaseResistance.value.baseValue

        val radiatorResistance =
            criticalElement.caseToRadiatorResistance.value.baseValue

        val junctionTemperatureKelvin =
            elementTemperature.kelvin +
                    (transitionResistance + radiatorResistance) * elementPower

        require(junctionTemperatureKelvin.isFinite() && junctionTemperatureKelvin >= 0.0) {
            "Calculated junction temperature must be finite and non-negative"
        }

        return Temperature.fromKelvin(junctionTemperatureKelvin)
    }

    private fun calculateZoneArea(geometry: HeatSourceGeometry): Double {

        val length = geometry.length.baseValue
        val width = geometry.width.baseValue
        val height = geometry.height.baseValue
        val fill = geometry.volumeFillCoefficient

        val area = 2.0 * (
                length * width +
                        height * fill * (length + width)
                )

        require(area.isFinite() && area > 0.0) {
            "Calculated heated zone area must be positive and finite"
        }

        return area
    }
}
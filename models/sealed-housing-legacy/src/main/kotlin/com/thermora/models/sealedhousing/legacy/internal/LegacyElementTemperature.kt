package com.thermora.models.sealedhousing.legacy.internal

import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.dimension.PhysicalDimension
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal object LegacyElementTemperature {

    private const val BASE_FACTOR = 0.75
    private const val POWER_FACTOR = 0.25
    private val TEMPERATURE_CONTEXT = MathContext(40, RoundingMode.HALF_EVEN)

    fun calculate(
        ambientTemperature: Temperature,
        heatedZoneRise: TemperatureDelta,
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

        val rise = BigDecimal(heatedZoneRise.kelvin)
        val denominator = BigDecimal(elementArea).multiply(BigDecimal(totalPowerWatts))
        val baseTemperature = BigDecimal(ambientTemperature.kelvin)
            .add(rise.multiply(BigDecimal(BASE_FACTOR)))
        val additionalHeatingNumerator = rise.multiply(BigDecimal(POWER_FACTOR))
            .multiply(BigDecimal(elementPower))
            .multiply(zoneArea)

        val elementTemperatureKelvin = baseTemperature.multiply(denominator)
            .add(additionalHeatingNumerator)
            .divide(denominator, TEMPERATURE_CONTEXT)
            .toDouble()

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

        val junctionTemperatureKelvin = BigDecimal(transitionResistance)
            .add(BigDecimal(radiatorResistance))
            .multiply(BigDecimal(elementPower))
            .add(BigDecimal(elementTemperature.kelvin))
            .toDouble()

        require(junctionTemperatureKelvin.isFinite() && junctionTemperatureKelvin >= 0.0) {
            "Calculated junction temperature must be finite and non-negative"
        }

        return Temperature.fromKelvin(junctionTemperatureKelvin)
    }

    private fun calculateZoneArea(geometry: HeatSourceGeometry): BigDecimal {
        val length = BigDecimal(geometry.length.baseValue)
        val width = BigDecimal(geometry.width.baseValue)
        val height = BigDecimal(geometry.height.baseValue)
        val fill = BigDecimal(geometry.volumeFillCoefficient)

        return length.multiply(width)
            .add(height.multiply(fill).multiply(length.add(width)))
            .multiply(BigDecimal(2))
    }
}

package com.thermora.models.externalcooling.legacy

import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExternalCoolingLegacyModelTest {

    private val model = ExternalCoolingLegacyModel()

    private fun validInput(
        totalPower: Double = 4.3,
        elementArea: Quantity = Quantity(
            0.00423,
            StandardUnits.SQUARE_METER
        )
    ) = ExternalCoolingLegacyInput(
        ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        ),
        source = ThermalSource(
            Quantity(totalPower, StandardUnits.WATT)
        ),
        heatedZone = HeatSourceGeometry(
            length = Quantity(0.112, StandardUnits.METER),
            width = Quantity(0.069, StandardUnits.METER),
            height = Quantity(0.0055, StandardUnits.METER),
            volumeFillCoefficient = 0.5
        ),
        criticalElement = CriticalElementThermalProperties(
            power = Quantity(1.25, StandardUnits.WATT),
            transitionToCaseResistance = ThermalResistance(
                Quantity(5.0, StandardUnits.KELVIN_PER_WATT)
            ),
            caseToRadiatorResistance = ThermalResistance(
                Quantity(0.5, StandardUnits.KELVIN_PER_WATT)
            )
        ),
        elementWithRadiatorArea = elementArea
    )

    @Test
    fun validInputPassesValidation() {
        val report = model.validate(validInput())

        assertTrue(report.isValid)
        assertTrue(report.diagnostics.isEmpty())
    }

    @Test
    fun zeroTotalPowerIsRejected() {
        val report = model.validate(
            validInput(totalPower = 0.0)
        )

        assertFalse(report.isValid)
        assertEquals(
            "EXTERNAL_COOLING_INVALID_TOTAL_POWER",
            report.diagnostics.single().code
        )
    }

    @Test
    fun zeroElementAreaIsRejected() {
        val report = model.validate(
            validInput(
                elementArea = Quantity(
                    0.0,
                    StandardUnits.SQUARE_METER
                )
            )
        )

        assertFalse(report.isValid)
        assertEquals(
            "EXTERNAL_COOLING_INVALID_ELEMENT_AREA",
            report.diagnostics.single().code
        )
    }

    @Test
    fun incorrectAreaDimensionIsRejected() {
        val report = model.validate(
            validInput(
                elementArea = Quantity(
                    0.00423,
                    StandardUnits.METER
                )
            )
        )

        assertFalse(report.isValid)
        assertEquals(
            "EXTERNAL_COOLING_INVALID_ELEMENT_AREA",
            report.diagnostics.single().code
        )
    }


    @Test
    fun validInputProducesExcelReferenceResults() {
        val result = model.calculate(validInput())

        val success =
            assertIs<CalculationResult.Success<ExternalCoolingLegacyOutput>>(result)

        val output = success.value

        // AA11 — Enclosure temperature rise
        assertEquals(
            6.9496599897105886,
            output.enclosureRise.kelvin,
            1e-9
        )

        // AA12 — Enclosure-to-ambient resistance
        assertEquals(
            1.6161999976071137,
            output.enclosureToAmbientResistance.value.baseValue,
            1e-9
        )

        // AA23 — Heated-zone temperature rise
        assertEquals(
            26.35835392600632,
            output.heatedZoneRise.kelvin,
            1e-9
        )

        // AA24 — Heated-zone-to-ambient resistance
        assertEquals(
            6.1298497502340279,
            output.heatedZoneToAmbientResistance.value.baseValue,
            1e-9
        )

        // AA27 — Enclosure temperature
        assertEquals(
            319.9496599897106,
            output.enclosureTemperature.kelvin,
            1e-9
        )

        // AA28 — Heated-zone temperature
        assertEquals(
            339.35835392600632,
            output.heatedZoneTemperature.kelvin,
            1e-9
        )

        // AA29 — Internal air temperature
        assertEquals(
            329.65400695785843,
            output.internalAirTemperature.kelvin,
            1e-9
        )

        // AA30 — Critical element temperature
        assertEquals(
            340.21891491007619,
            output.elementTemperature.kelvin,
            1e-9
        )

        // AA31 — Junction temperature
        assertEquals(
            347.09391491007619,
            output.junctionTemperature.kelvin,
            1e-9
        )
    }


    @Test
    fun invalidInputReturnsValidationFailure() {
        val result = model.calculate(
            validInput(totalPower = 0.0)
        )

        val failure = assertIs<CalculationResult.Failure>(result)

        assertEquals(
            "EXTERNAL_COOLING_INVALID_TOTAL_POWER",
            failure.diagnostics.single().code
        )
    }
}

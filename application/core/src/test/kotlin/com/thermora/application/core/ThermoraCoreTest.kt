package com.thermora.application.core

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.externalcooling.legacy.ExternalCoolingLegacyModel
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import java.util.concurrent.CancellationException
import kotlin.test.assertSame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ThermoraCoreTest {

    private val modelId = CalculationModelId("sealed-housing-legacy")

    private fun referenceInput() = SealedHousingLegacyInput(
        ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        ),
        source = ThermalSource(
            Quantity(4.3, StandardUnits.WATT)
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
        elementWithRadiatorArea = Quantity(
            0.00423,
            StandardUnits.SQUARE_METER
        )
    )

    @Test
    fun `factory registers default calculation models`() {
        val core = ThermoraCoreFactory.createDefault()

        val models = core.availableModels()

        assertEquals(3, models.size)

        assertEquals(
            setOf(
                CalculationModelId("sealed-housing-legacy"),
                CalculationModelId("internal-mixing-legacy"),
                CalculationModelId("external-cooling-legacy")
            ),
            models.map { it.id }.toSet()
        )
    }

    @Test
    fun `finds registered calculation model`() {
        val core = ThermoraCoreFactory.createDefault()

        val descriptor = core.findModel(modelId)

        assertNotNull(descriptor)
        assertEquals("1.0.0", descriptor.version)
    }

    @Test
    fun `executes calculation through application core`() {
        val core = ThermoraCoreFactory.createDefault()

        val result = core.calculate(
            modelId = modelId,
            input = referenceInput(),
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        val success =
            assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)

        assertEquals(
            323.8726142430732,
            success.value.enclosureTemperature.kelvin,
            1e-9
        )

        assertEquals(
            351.09906309240495,
            success.value.junctionTemperature.kelvin,
            1e-9
        )

        assertTrue(
            success.diagnostics.any {
                it.code == "SEALED_LEGACY_APPLICABILITY_UNKNOWN"
            }
        )
    }


    @Test
    fun `creates core without calculation models`() {
        val core = ThermoraCoreFactory.create(emptyList())

        assertTrue(core.availableModels().isEmpty())
        assertEquals(
            null,
            core.findModel(modelId)
        )
    }

    @Test
    fun `creates core with custom calculation models`() {
        val core = ThermoraCoreFactory.create(
            listOf(SealedHousingLegacyModel())
        )

        assertEquals(1, core.availableModels().size)
        assertNotNull(core.findModel(modelId))

        val result = core.calculate(
            modelId = modelId,
            input = referenceInput(),
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)
    }

    @Test
    fun `propagates cancellation through application core`() {
        val cause = IllegalStateException("Cancellation reason")
        val exception = CancellationException("Calculation cancelled").apply {
            initCause(cause)
        }
        val model = object : CalculationModel<SealedHousingLegacyInput, SealedHousingLegacyOutput> {
            override val descriptor = SealedHousingLegacyModel().descriptor
            override val inputType = SealedHousingLegacyInput::class
            override val outputType = SealedHousingLegacyOutput::class

            override fun validate(input: SealedHousingLegacyInput): ValidationReport = ValidationReport.valid()

            override fun calculate(input: SealedHousingLegacyInput): CalculationResult<SealedHousingLegacyOutput> {
                throw exception
            }
        }
        val core = ThermoraCoreFactory.create(listOf(model))

        val thrown = assertFailsWith<CancellationException> {
            core.calculate(modelId, referenceInput(), SealedHousingLegacyOutput::class)
        }

        assertSame(exception, thrown)
        assertSame(cause, thrown.cause)
    }

    @Test
    fun `rejects duplicate calculation model identifiers`() {
        val exception = assertFailsWith<IllegalStateException> {
            ThermoraCoreFactory.create(
                listOf(
                    SealedHousingLegacyModel(),
                    SealedHousingLegacyModel()
                )
            )
        }

        assertTrue(
            exception.message?.contains("Failed to register model") == true
        )
    }


    @Test
    fun `default core registers all three legacy models`() {
        val core = ThermoraCoreFactory.createDefault()

        val modelIds = core.availableModels()
            .map { it.id.value }
            .toSet()

        assertEquals(
            setOf(
                "sealed-housing-legacy",
                "internal-mixing-legacy",
                "external-cooling-legacy"
            ),
            modelIds
        )

        assertNotNull(
            core.findModel(
                CalculationModelId("internal-mixing-legacy")
            )
        )
    }


    private fun externalInput(): ExternalCoolingLegacyInput {
        val input = referenceInput()
        return ExternalCoolingLegacyInput(input.ambient, input.source, input.heatedZone,
            input.criticalElement, input.elementWithRadiatorArea)
    }

    @Test
    fun `default core discovers and executes external cooling with all nine Excel outputs`() {
        val core = ThermoraCoreFactory.createDefault()
        val id = CalculationModelId("external-cooling-legacy")
        val descriptor = assertNotNull(core.findModel(id))
        assertEquals("1.0.0", descriptor.version)
        assertEquals(1, core.availableModels().count { it.id == id })
        val input = externalInput()

        val success = assertIs<CalculationResult.Success<ExternalCoolingLegacyOutput>>(
            core.calculate(id, input, ExternalCoolingLegacyOutput::class)
        )
        val output = success.value
        val expected = listOf(6.9496599897105886, 1.6161999976071137, 26.35835392600632,
            6.1298497502340279, 319.9496599897106, 339.35835392600632,
            329.65400695785843, 340.21891491007619, 347.09391491007619)
        val actual = listOf(output.enclosureRise.kelvin, output.enclosureToAmbientResistance.value.baseValue,
            output.heatedZoneRise.kelvin, output.heatedZoneToAmbientResistance.value.baseValue,
            output.enclosureTemperature.kelvin, output.heatedZoneTemperature.kelvin,
            output.internalAirTemperature.kelvin, output.elementTemperature.kelvin, output.junctionTemperature.kelvin)
        expected.zip(actual).forEach { (reference, value) -> assertEquals(reference, value, 1e-9) }
        assertEquals(ExternalCoolingLegacyModel().calculate(input), success)
        assertEquals(success, core.calculate(id, input, ExternalCoolingLegacyOutput::class))
        assertEquals("EXTERNAL_COOLING_LEGACY_APPLICABILITY_UNKNOWN", success.diagnostics.single().code)
        assertEquals(DiagnosticSeverity.WARNING, success.diagnostics.single().severity)
    }

    @Test
    fun `core preserves external cooling validation and numerical failure codes`() {
        val core = ThermoraCoreFactory.createDefault()
        val input = externalInput()
        val requests = listOf(
            input.copy(source = ThermalSource(Quantity(0.0, StandardUnits.WATT))) to
                "EXTERNAL_COOLING_INVALID_TOTAL_POWER",
            input.copy(elementWithRadiatorArea = Quantity(0.0, StandardUnits.SQUARE_METER)) to
                "EXTERNAL_COOLING_INVALID_ELEMENT_AREA",
            input.copy(elementWithRadiatorArea = Quantity(Double.MIN_VALUE, StandardUnits.SQUARE_METER)) to
                "EXTERNAL_COOLING_CALCULATION_ERROR"
        )
        requests.forEach { (request, code) ->
            val failure = assertIs<CalculationResult.Failure>(core.calculate(
                CalculationModelId("external-cooling-legacy"), request, ExternalCoolingLegacyOutput::class))
            assertEquals(code, failure.diagnostics.single().code)
            assertEquals(DiagnosticSeverity.ERROR, failure.diagnostics.single().severity)
            assertEquals(ExternalCoolingLegacyModel().calculate(request), failure)
        }
    }

}

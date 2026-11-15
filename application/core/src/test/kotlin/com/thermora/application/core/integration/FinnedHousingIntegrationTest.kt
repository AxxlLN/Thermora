package com.thermora.application.core.integration

import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.engine.execution.CalculationEngine
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.domain.geometry.FinGeometry
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.finnedhousing.legacy.FinnedHousingLegacyModel
import com.thermora.models.finnedhousing.legacy.input.ExternalProbeConfiguration
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.input.InternalProbeConfiguration
import com.thermora.models.finnedhousing.legacy.input.InternalWallGap
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingLegacyOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class FinnedHousingIntegrationTest {
    private val model = FinnedHousingLegacyModel()
    private fun length(value: Double) = Quantity(value, StandardUnits.METER)
    private fun resistance(value: Double) = ThermalResistance(Quantity(value, StandardUnits.KELVIN_PER_WATT))
    private fun input() = FinnedHousingLegacyInput(
        AmbientConditions(Temperature.fromKelvin(313.0)), ThermalSource(Quantity(4.3, StandardUnits.WATT)),
        EnclosureGeometry(length(0.121), length(0.078), length(0.039)),
        FinGeometry(length(0.003), length(0.025), length(0.039), 10, length(0.00533)),
        HeatSourceGeometry(length(0.112), length(0.069), length(0.0055), 0.5),
        CriticalElementThermalProperties(Quantity(1.25, StandardUnits.WATT), resistance(5.0), resistance(0.5)),
        Quantity(0.00423, StandardUnits.SQUARE_METER), 0.9,
        ExternalProbeConfiguration(listOf(5.0, 10.0, 15.0).map { TemperatureDelta.fromKelvin(it) }),
        InternalProbeConfiguration(
            listOf(0.003, 0.003, 0.003, 0.003, 0.0265, 0.004).mapIndexed { index, gap ->
                InternalWallGap(index + 1, length(gap), listOf(1.0, 1.0, 1.0, 1.0, 0.7, 1.3)[index]) },
            Temperature.fromKelvin(360.63098423378119),
            listOf(10.0, 20.0, 40.0).map { TemperatureDelta.fromKelvin(it) },
            listOf(5.0556031543490354, 4.5439787678362187, 4.0195022659471871).map { resistance(it) })
    )

    private fun engine(): CalculationEngine {
        val registry = CalculationModelRegistry()
        assertIs<CalculationResult.Success<Unit>>(registry.register(model))
        return CalculationEngine(registry)
    }

    private fun assertExcel(result: CalculationResult<FinnedHousingLegacyOutput>) {
        val output = assertIs<CalculationResult.Success<FinnedHousingLegacyOutput>>(result).value
        val final = output.finalResults
        val expected = listOf(7.6309842337811995, 1.774647496228186, 27.144910812935397, 6.312769956496604,
            320.63098423378119, 340.14491081293539, 330.38794752335832, 341.03115170746338, 347.90615170746338)
        val actual = listOf(final.enclosureRise.kelvin, final.enclosureToAmbientResistance.value.baseValue,
            final.heatedZoneRise.kelvin, final.heatedZoneToAmbientResistance.value.baseValue,
            final.enclosureTemperature.kelvin, final.heatedZoneTemperature.kelvin,
            final.internalAirTemperature.kelvin, final.elementTemperature.kelvin, final.junctionTemperature.kelvin)
        expected.zip(actual).forEach { (reference, value) -> assertEquals(reference, value, 1e-9) }
        assertEquals(3, output.externalHeatTransfer.probes.size)
        assertEquals(6, output.internalHeatTransfer.wallConvectionCoefficients.size)
        assertEquals(3, output.internalHeatTransfer.trialResults.size)
    }

    @Test
    fun `registry discovers the finned model and rejects duplicate registration`() {
        val registry = CalculationModelRegistry()
        assertIs<CalculationResult.Success<Unit>>(registry.register(model))
        assertEquals(model.descriptor, registry.find(model.descriptor.id))
        assertEquals(listOf(model.descriptor), registry.getAll())
        val failure = assertIs<CalculationResult.Failure>(registry.register(FinnedHousingLegacyModel()))
        assertEquals("CALCULATION_MODEL_ALREADY_REGISTERED", failure.diagnostics.single().code)
        assertEquals(listOf(model.descriptor), registry.getAll())
    }

    @Test
    fun `engine executes every output group and all nine final reference values`() {
        assertExcel(engine().calculate(model.descriptor.id, input(), FinnedHousingLegacyOutput::class))
    }

    @Test
    fun `default core discovers and executes the finned model through its public API`() {
        val core = ThermoraCoreFactory.createDefault()
        assertEquals(model.descriptor, assertNotNull(core.findModel(model.descriptor.id)))
        assertEquals(1, core.availableModels().count { it.id == model.descriptor.id })
        val result = core.calculate(model.descriptor.id, input(), FinnedHousingLegacyOutput::class)
        assertExcel(result)
        assertEquals(model.calculate(input()), result)
    }

    @Test
    fun `packing warnings precede calculation warnings and are not duplicated`() {
        val original = input()
        val request = original.copy(fins = original.fins.copy(countPerSide = 11))
        val expectedCodes = listOf("FINNED_HOUSING_INVALID_FIN_PACKING",
            "FINNED_HOUSING_LEGACY_APPLICABILITY_UNKNOWN", "FINNED_HOUSING_LEGACY_FIXED_CORRELATIONS")
        val result = assertIs<CalculationResult.Success<FinnedHousingLegacyOutput>>(
            engine().calculate(model.descriptor.id, request, FinnedHousingLegacyOutput::class))
        assertEquals(expectedCodes, result.diagnostics.map { it.code })
        assertEquals(List(3) { DiagnosticSeverity.WARNING }, result.diagnostics.map { it.severity })
        assertEquals(result, ThermoraCoreFactory.createDefault().calculate(model.descriptor.id,
            request, FinnedHousingLegacyOutput::class))
    }

    @Test
    fun `validation errors retain their codes and prevent incomplete output`() {
        val original = input()
        listOf(
            original.copy(source = ThermalSource(Quantity(0.0, StandardUnits.WATT))) to "FINNED_HOUSING_INVALID_TOTAL_POWER",
            original.copy(internalProbes = original.internalProbes.copy(referenceZoneTemperature = Temperature.fromKelvin(315.0))) to
                "FINNED_HOUSING_INVALID_REFERENCE_TEMPERATURE"
        ).forEach { (request, code) ->
            val result = assertIs<CalculationResult.Failure>(engine().calculate(model.descriptor.id,
                request, FinnedHousingLegacyOutput::class))
            assertEquals(code, result.diagnostics.single().code)
            assertEquals(DiagnosticSeverity.ERROR, result.diagnostics.single().severity)
            assertEquals(result, ThermoraCoreFactory.createDefault().calculate(model.descriptor.id,
                request, FinnedHousingLegacyOutput::class))
        }
    }

    @Test
    fun `numerical failures preserve validation warnings through engine and core`() {
        val original = input()
        val request = original.copy(fins = original.fins.copy(width = length(0.01), spacing = length(0.0001)))
        val failure = assertIs<CalculationResult.Failure>(engine().calculate(model.descriptor.id,
            request, FinnedHousingLegacyOutput::class))
        assertEquals(listOf("FINNED_HOUSING_INVALID_FIN_PACKING", "FINNED_HOUSING_CALCULATION_ERROR"),
            failure.diagnostics.map { it.code })
        assertEquals(listOf(DiagnosticSeverity.WARNING, DiagnosticSeverity.ERROR), failure.diagnostics.map { it.severity })
        assertEquals("L5", failure.diagnostics.last().details["cell"])
        assertEquals(failure, ThermoraCoreFactory.createDefault().calculate(model.descriptor.id,
            request, FinnedHousingLegacyOutput::class))
    }

    private class WrongInput : CalculationInput
    private class WrongOutput : CalculationOutput

    @Test
    fun `engine rejects incompatible input and expected output types`() {
        val engine = engine()
        val wrongInput = assertIs<CalculationResult.Failure>(engine.calculate(model.descriptor.id,
            WrongInput(), FinnedHousingLegacyOutput::class))
        assertEquals("CALCULATION_INPUT_TYPE_MISMATCH", wrongInput.diagnostics.single().code)
        val wrongOutput = assertIs<CalculationResult.Failure>(engine.calculate(model.descriptor.id,
            input(), WrongOutput::class))
        assertEquals("CALCULATION_OUTPUT_TYPE_MISMATCH", wrongOutput.diagnostics.single().code)
    }

    @Test
    fun `custom core supports the finned model without other physical models`() {
        val core = ThermoraCoreFactory.create(listOf(model))
        assertEquals(listOf(model.descriptor), core.availableModels())
        assertExcel(core.calculate(model.descriptor.id, input(), FinnedHousingLegacyOutput::class))
    }

    @Test
    fun `factory rejects duplicate finned model identifiers`() {
        assertFailsWith<IllegalStateException> { ThermoraCoreFactory.create(listOf(model, FinnedHousingLegacyModel())) }
    }
}

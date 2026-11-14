
package com.thermora.application.core.workflow

import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.application.core.ThermoraCore
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.internalmixing.legacy.InternalMixingLegacyModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LegacyThermalWorkflowTest {

    private class ControlledModel<I : CalculationInput, O : CalculationOutput>(
        private val delegate: CalculationModel<I, O>,
        private val calls: MutableList<String>,
        private val validation: ValidationReport = ValidationReport.valid(),
        private val calculation: (I) -> CalculationResult<O> = { delegate.calculate(it) }
    ) : CalculationModel<I, O> by delegate {
        var validationCalls = 0
            private set
        var calculationCalls = 0
            private set

        override fun validate(input: I): ValidationReport {
            validationCalls++
            calls.add("${descriptor.id.value}:validate")
            return validation
        }

        override fun calculate(input: I): CalculationResult<O> {
            calculationCalls++
            calls.add("${descriptor.id.value}:calculate")
            return calculation(input)
        }
    }

    private fun workflowApis(): List<(ThermoraCore, SealedHousingLegacyInput) -> CalculationResult<LegacyThermalWorkflowOutput>> =
        listOf(
            { core, input -> LegacyThermalWorkflow(core).execute(input) },
            { core, input -> LegacyThermalWorkflow(core).execute(LegacyThermalWorkflowInput(input, input.criticalElement)) },
            { core, input -> core.calculateLegacyThermal(input) },
            { core, input -> core.calculateLegacyThermal(LegacyThermalWorkflowInput(input, input.criticalElement)) }
        )

    private fun diagnostic(code: String, severity: DiagnosticSeverity, source: String) =
        Diagnostic(code, code, severity, source, mapOf("stage" to source))

    @Test
    fun `first stage validation error prevents both calculation and second stage entry`() {
        workflowApis().forEach { execute ->
            val calls = mutableListOf<String>()
            val error = diagnostic("SEALED_INPUT_ERROR", DiagnosticSeverity.ERROR, "sealed-housing-legacy")
            val sealed = ControlledModel(SealedHousingLegacyModel(), calls, ValidationReport.of(error))
            val mixing = ControlledModel(InternalMixingLegacyModel(), calls)
            val core = ThermoraCoreFactory.create(listOf(sealed, mixing))

            val result = assertIs<CalculationResult.Failure>(execute(core, referenceInput()))

            assertEquals(listOf(error), result.diagnostics)
            assertEquals(listOf("sealed-housing-legacy:validate"), calls)
            assertEquals(1, sealed.validationCalls)
            assertEquals(0, sealed.calculationCalls)
            assertEquals(0, mixing.validationCalls)
            assertEquals(0, mixing.calculationCalls)
        }
    }

    @Test
    fun `first stage calculation failure preserves diagnostics and never enters second stage`() {
        workflowApis().forEach { execute ->
            val calls = mutableListOf<String>()
            val warning = diagnostic("SEALED_INPUT_WARNING", DiagnosticSeverity.WARNING, "sealed-housing-legacy")
            val error = diagnostic("SEALED_EXECUTION_ERROR", DiagnosticSeverity.ERROR, "sealed-housing-legacy")
            val sealed = ControlledModel(SealedHousingLegacyModel(), calls, ValidationReport.of(warning)) {
                CalculationResult.Failure(listOf(error))
            }
            val mixing = ControlledModel(InternalMixingLegacyModel(), calls)
            val core = ThermoraCoreFactory.create(listOf(sealed, mixing))

            val result = assertIs<CalculationResult.Failure>(execute(core, referenceInput()))

            assertEquals(listOf(warning, error), result.diagnostics)
            assertEquals(listOf("sealed-housing-legacy:validate", "sealed-housing-legacy:calculate"), calls)
            assertEquals(1, sealed.calculationCalls)
            assertEquals(0, mixing.validationCalls)
            assertEquals(0, mixing.calculationCalls)
        }
    }

    @Test
    fun `second stage failure retains every earlier diagnostic in execution order`() {
        workflowApis().forEach { execute ->
            val calls = mutableListOf<String>()
            val beforeSealed = diagnostic("SEALED_INPUT_INFO", DiagnosticSeverity.INFO, "sealed-housing-legacy")
            val beforeMixing = diagnostic("MIXING_INPUT_WARNING", DiagnosticSeverity.WARNING, "internal-mixing-legacy")
            val error = diagnostic("MIXING_EXECUTION_ERROR", DiagnosticSeverity.ERROR, "internal-mixing-legacy")
            val info = diagnostic("MIXING_EXECUTION_INFO", DiagnosticSeverity.INFO, "internal-mixing-legacy")
            val sealedDelegate = SealedHousingLegacyModel()
            val sealedDiagnostics = assertIs<CalculationResult.Success<*>>(sealedDelegate.calculate(referenceInput())).diagnostics
            val sealed = ControlledModel(sealedDelegate, calls, ValidationReport.of(beforeSealed))
            val mixing = ControlledModel(InternalMixingLegacyModel(), calls, ValidationReport.of(beforeMixing)) {
                CalculationResult.Failure(listOf(error, info))
            }
            val core = ThermoraCoreFactory.create(listOf(sealed, mixing))

            val result = assertIs<CalculationResult.Failure>(execute(core, referenceInput()))

            assertEquals(listOf(beforeSealed) + sealedDiagnostics + listOf(beforeMixing, error, info), result.diagnostics)
            assertEquals(listOf("sealed-housing-legacy:validate", "sealed-housing-legacy:calculate",
                "internal-mixing-legacy:validate", "internal-mixing-legacy:calculate"), calls)
            assertEquals(1, sealed.validationCalls)
            assertEquals(1, sealed.calculationCalls)
            assertEquals(1, mixing.validationCalls)
            assertEquals(1, mixing.calculationCalls)
        }
    }

    @Test
    fun `second stage validation failure preserves first stage warnings without calculating mixing`() {
        workflowApis().forEach { execute ->
            val calls = mutableListOf<String>()
            val error = diagnostic("MIXING_INPUT_ERROR", DiagnosticSeverity.ERROR, "internal-mixing-legacy")
            val sealedDelegate = SealedHousingLegacyModel()
            val sealedDiagnostics = assertIs<CalculationResult.Success<*>>(sealedDelegate.calculate(referenceInput())).diagnostics
            val sealed = ControlledModel(sealedDelegate, calls)
            val mixing = ControlledModel(InternalMixingLegacyModel(), calls, ValidationReport.of(error))
            val core = ThermoraCoreFactory.create(listOf(sealed, mixing))

            val result = assertIs<CalculationResult.Failure>(execute(core, referenceInput()))

            assertEquals(sealedDiagnostics + error, result.diagnostics)
            assertEquals(listOf("sealed-housing-legacy:validate", "sealed-housing-legacy:calculate",
                "internal-mixing-legacy:validate"), calls)
            assertEquals(1, mixing.validationCalls)
            assertEquals(0, mixing.calculationCalls)
        }
    }

    @Test
    fun `all workflow APIs preserve ordered model warnings and validation messages on success`() {
        val input = referenceInput()
        val expected = assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(
            ThermoraCoreFactory.createDefault().calculateLegacyThermal(input)
        )
        assertEquals(listOf(
            "SEALED_LEGACY_APPLICABILITY_UNKNOWN" to DiagnosticSeverity.WARNING,
            "INTERNAL_MIXING_LEGACY_APPLICABILITY_UNKNOWN" to DiagnosticSeverity.WARNING,
            "INTERNAL_MIXING_LEGACY_JUNCTION_USES_ZONE" to DiagnosticSeverity.WARNING
        ), expected.diagnostics.map { it.code to it.severity })
        workflowApis().forEach { execute ->
            val calls = mutableListOf<String>()
            val sealedInfo = diagnostic("SEALED_INPUT_INFO", DiagnosticSeverity.INFO, "sealed-housing-legacy")
            val mixingWarning = diagnostic("MIXING_INPUT_WARNING", DiagnosticSeverity.WARNING, "internal-mixing-legacy")
            val sealed = ControlledModel(SealedHousingLegacyModel(), calls, ValidationReport.of(sealedInfo))
            val mixing = ControlledModel(InternalMixingLegacyModel(), calls, ValidationReport.of(mixingWarning))
            val core = ThermoraCoreFactory.create(listOf(sealed, mixing))

            val result = assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(execute(core, input))

            assertEquals(expected.value, result.value)
            assertEquals(listOf(sealedInfo, expected.diagnostics[0], mixingWarning) + expected.diagnostics.drop(1),
                result.diagnostics)
            assertEquals(listOf("sealed-housing-legacy:validate", "sealed-housing-legacy:calculate",
                "internal-mixing-legacy:validate", "internal-mixing-legacy:calculate"), calls)
            assertEquals(1, sealed.validationCalls)
            assertEquals(1, sealed.calculationCalls)
            assertEquals(1, mixing.validationCalls)
            assertEquals(1, mixing.calculationCalls)
        }
    }

    private fun referenceInput(
        totalPower: Double = 4.3
    ) = SealedHousingLegacyInput(
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
        elementWithRadiatorArea = Quantity(
            0.00423,
            StandardUnits.SQUARE_METER
        )
    )

    @Test
    fun `executes both legacy models and reproduces Excel values`() {
        val workflow = LegacyThermalWorkflow(
            ThermoraCoreFactory.createDefault()
        )

        val result = workflow.execute(referenceInput())

        val success =
            assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(
                result
            )

        val sealed = success.value.sealedHousing
        val mixing = success.value.internalMixing

        // First stage: sealed housing
        assertEquals(
            323.8726142430732,
            sealed.enclosureTemperature.kelvin,
            1e-9
        )

        // Second stage: internal mixing
        assertEquals(
            24.282434969840896,
            mixing.heatedZoneRise.kelvin,
            1e-9
        )

        assertEquals(
            337.2824349698409,
            mixing.heatedZoneTemperature.kelvin,
            1e-9
        )

        assertEquals(
            344.1574349698409,
            mixing.junctionTemperature.kelvin,
            1e-9
        )
    }

    @Test
    fun `passes enclosure boundary conditions between stages`() {
        val workflow = LegacyThermalWorkflow(
            ThermoraCoreFactory.createDefault()
        )

        val result = workflow.execute(referenceInput())

        val success =
            assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(
                result
            )

        assertEquals(
            success.value.sealedHousing.enclosureTemperature.kelvin,
            success.value.internalMixing.enclosureTemperature.kelvin,
            1e-12
        )
    }

    @Test
    fun `combines diagnostics from both calculation stages`() {
        val workflow = LegacyThermalWorkflow(
            ThermoraCoreFactory.createDefault()
        )

        val result = workflow.execute(referenceInput())

        val success =
            assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(
                result
            )

        val codes = success.diagnostics.map { it.code }

        assertEquals(3, codes.size)

        assertEquals(
            setOf(
                "SEALED_LEGACY_APPLICABILITY_UNKNOWN",
                "INTERNAL_MIXING_LEGACY_APPLICABILITY_UNKNOWN",
                "INTERNAL_MIXING_LEGACY_JUNCTION_USES_ZONE"
            ),
            codes.toSet()
        )
    }

    @Test
    fun `stops workflow when first stage fails`() {
        val workflow = LegacyThermalWorkflow(
            ThermoraCoreFactory.createDefault()
        )

        val result = workflow.execute(
            referenceInput(totalPower = 0.0)
        )

        val failure =
            assertIs<CalculationResult.Failure>(result)

        assertTrue(
            failure.diagnostics.any {
                it.code == "INVALID_TOTAL_POWER"
            }
        )
    }

    @Test
    fun `returns failure when second model is not registered`() {
        val core = ThermoraCoreFactory.create(
            listOf(SealedHousingLegacyModel())
        )

        val workflow = LegacyThermalWorkflow(core)

        val result = workflow.execute(referenceInput())

        val failure =
            assertIs<CalculationResult.Failure>(result)

        assertTrue(failure.diagnostics.isNotEmpty())
        assertTrue(
            failure.diagnostics.any {
                it.severity ==
                        com.thermora.foundation.common.diagnostic.DiagnosticSeverity.ERROR
            }
        )
    }


    @Test
    fun `executes complete legacy calculation through core extension`() {
        val core = ThermoraCoreFactory.createDefault()

        val result = core.calculateLegacyThermal(referenceInput())

        val success =
            assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(
                result
            )

        assertEquals(
            323.8726142430732,
            success.value.sealedHousing.enclosureTemperature.kelvin,
            1e-9
        )

        assertEquals(
            344.1574349698409,
            success.value.internalMixing.junctionTemperature.kelvin,
            1e-9
        )

        assertEquals(3, success.diagnostics.size)
    }

    @Test
    fun `TH-05 explicit common parameters preserve original workflow results`() {
        val input = referenceInput()
        val workflow = LegacyThermalWorkflow(ThermoraCoreFactory.createDefault())

        assertEquals(
            workflow.execute(input),
            workflow.execute(LegacyThermalWorkflowInput(input, input.criticalElement))
        )
    }

    @Test
    fun `TH-05 independent mixing resistances leave sealed outputs unchanged`() {
        val input = referenceInput()
        val workflow = LegacyThermalWorkflow(ThermoraCoreFactory.createDefault())
        val original = assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(workflow.execute(input))

        listOf(10.0 to 0.5, 5.0 to 2.0).forEach { (transition, radiator) ->
            val mixingElement = input.criticalElement.copy(
                transitionToCaseResistance = ThermalResistance(Quantity(transition, StandardUnits.KELVIN_PER_WATT)),
                caseToRadiatorResistance = ThermalResistance(Quantity(radiator, StandardUnits.KELVIN_PER_WATT))
            )
            val changed = assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(
                workflow.execute(LegacyThermalWorkflowInput(input, mixingElement))
            )

            assertEquals(original.value.sealedHousing, changed.value.sealedHousing)
            assertEquals(
                original.value.internalMixing.copy(junctionTemperature = changed.value.internalMixing.junctionTemperature),
                changed.value.internalMixing
            )
            assertEquals(
                original.value.internalMixing.heatedZoneTemperature.kelvin +
                    (transition + radiator) * input.criticalElement.power.baseValue,
                changed.value.internalMixing.junctionTemperature.kelvin,
                1e-9
            )
            assertEquals(original.diagnostics, changed.diagnostics)
        }
    }

    @Test
    fun `TH-05 core extension accepts independent second stage parameters`() {
        val core = ThermoraCoreFactory.createDefault()
        val input = referenceInput()
        val mixingElement = input.criticalElement.copy(
            transitionToCaseResistance = ThermalResistance(Quantity(10.0, StandardUnits.KELVIN_PER_WATT))
        )
        val workflowInput = LegacyThermalWorkflowInput(input, mixingElement)
        val result = assertIs<CalculationResult.Success<LegacyThermalWorkflowOutput>>(core.calculateLegacyThermal(workflowInput))

        assertEquals(LegacyThermalWorkflow(core).execute(workflowInput), result)
        assertEquals(351.09906309240495, result.value.sealedHousing.junctionTemperature.kelvin, 1e-9)
        assertEquals(350.4074349698409, result.value.internalMixing.junctionTemperature.kelvin, 1e-9)
        assertEquals(core.calculateLegacyThermal(input),
            core.calculateLegacyThermal(LegacyThermalWorkflowInput(input, input.criticalElement)))
    }

    @Test
    fun `TH-05 new workflow input preserves first stage failure behavior`() {
        val input = referenceInput(totalPower = 0.0)
        val core = ThermoraCoreFactory.createDefault()

        assertEquals(core.calculateLegacyThermal(input),
            core.calculateLegacyThermal(LegacyThermalWorkflowInput(input, input.criticalElement)))
    }

}

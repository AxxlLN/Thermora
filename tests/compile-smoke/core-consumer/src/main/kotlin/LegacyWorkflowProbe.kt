import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.application.core.workflow.LegacyThermalWorkflow
import com.thermora.application.core.workflow.LegacyThermalWorkflowInput
import com.thermora.application.core.workflow.LegacyThermalWorkflowOutput
import com.thermora.application.core.workflow.calculateLegacyThermal
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput

fun calculateLegacy(input: SealedHousingLegacyInput): CalculationResult<LegacyThermalWorkflowOutput> =
    ThermoraCoreFactory.createDefault().calculateLegacyThermal(input)

fun executeLegacy(input: SealedHousingLegacyInput): CalculationResult<LegacyThermalWorkflowOutput> =
    LegacyThermalWorkflow(ThermoraCoreFactory.createDefault()).execute(input)

fun sealedResult(output: LegacyThermalWorkflowOutput): SealedHousingLegacyOutput = output.sealedHousing

fun mixingResult(output: LegacyThermalWorkflowOutput): InternalMixingLegacyOutput = output.internalMixing

fun temperatureDifference(output: LegacyThermalWorkflowOutput): Double =
    mixingResult(output).heatedZoneTemperature.kelvin - sealedResult(output).heatedZoneTemperature.kelvin

fun calculateIndependentLegacy(input: LegacyThermalWorkflowInput): CalculationResult<LegacyThermalWorkflowOutput> =
    ThermoraCoreFactory.createDefault().calculateLegacyThermal(input)

fun executeIndependentLegacy(input: LegacyThermalWorkflowInput): CalculationResult<LegacyThermalWorkflowOutput> =
    LegacyThermalWorkflow(ThermoraCoreFactory.createDefault()).execute(input)

fun independentWorkflowInput(
    sealedInput: SealedHousingLegacyInput,
    mixingElement: com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
): LegacyThermalWorkflowInput = LegacyThermalWorkflowInput(sealedInput, mixingElement)

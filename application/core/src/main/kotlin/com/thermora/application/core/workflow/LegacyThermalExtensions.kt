package com.thermora.application.core.workflow

import com.thermora.application.core.ThermoraCore
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput

/**
 * Executes the complete legacy thermal calculation workflow.
 *
 * Stages:
 * 1. Sealed housing calculation
 * 2. Internal mixing calculation
 *
 * Both stages use the registered models through ThermoraCore.
 */
fun ThermoraCore.calculateLegacyThermal(
    input: SealedHousingLegacyInput
): CalculationResult<LegacyThermalWorkflowOutput> =
    LegacyThermalWorkflow(this).execute(input)


fun ThermoraCore.calculateLegacyThermal(
    input: LegacyThermalWorkflowInput
): CalculationResult<LegacyThermalWorkflowOutput> =
    LegacyThermalWorkflow(this).execute(input)

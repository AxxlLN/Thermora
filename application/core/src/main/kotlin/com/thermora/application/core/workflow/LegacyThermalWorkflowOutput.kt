package com.thermora.application.core.workflow

import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput

/**
 * Combined result of the legacy thermal calculation workflow.
 *
 * Contains the results of both calculation stages.
 */
data class LegacyThermalWorkflowOutput(
    val sealedHousing: SealedHousingLegacyOutput,
    val internalMixing: InternalMixingLegacyOutput
)

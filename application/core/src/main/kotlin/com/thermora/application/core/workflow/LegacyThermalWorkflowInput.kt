package com.thermora.application.core.workflow

import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput

data class LegacyThermalWorkflowInput(
    val sealedHousingInput: SealedHousingLegacyInput,
    val internalMixingCriticalElement: CriticalElementThermalProperties
)

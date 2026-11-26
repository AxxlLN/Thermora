package com.thermora.application.core.configuration

import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.models.externalcooling.legacy.ExternalCoolingLegacyModel
import com.thermora.models.finnedhousing.legacy.FinnedHousingLegacyModel
import com.thermora.models.internalmixing.legacy.InternalMixingLegacyModel
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel

import com.thermora.models.perforatedhousing.legacy.PerforatedHousingLegacyModel

import com.thermora.models.forcedcooling.legacy.ForcedCoolingLegacyModel

internal object DefaultModelConfiguration {

    fun models(): List<CalculationModel<out CalculationInput, out CalculationOutput>> =
        listOf(
            SealedHousingLegacyModel(),
            InternalMixingLegacyModel(),
            ExternalCoolingLegacyModel(),
            FinnedHousingLegacyModel(),
            PerforatedHousingLegacyModel(),
            ForcedCoolingLegacyModel()
        )
}

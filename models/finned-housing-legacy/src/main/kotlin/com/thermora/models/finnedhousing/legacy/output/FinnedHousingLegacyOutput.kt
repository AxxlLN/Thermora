package com.thermora.models.finnedhousing.legacy.output

import com.thermora.calculation.api.model.CalculationOutput

data class FinnedHousingLegacyOutput(
    val finalResults: FinnedHousingFinalResults,
    val geometryResults: FinnedHousingGeometryResults,
    val externalHeatTransfer: ExternalHeatTransferResults,
    val internalHeatTransfer: InternalHeatTransferResults
) : CalculationOutput

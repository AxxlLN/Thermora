import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingLegacyOutput

fun calculateFinnedHousing(input: FinnedHousingLegacyInput): CalculationResult<FinnedHousingLegacyOutput> =
    ThermoraCoreFactory.createDefault().calculate(
        CalculationModelId("finned-housing-legacy"), input, FinnedHousingLegacyOutput::class
    )

fun finnedTrialPower(output: FinnedHousingLegacyOutput): Double =
    output.externalHeatTransfer.probes.first().dissipatedPower.baseValue

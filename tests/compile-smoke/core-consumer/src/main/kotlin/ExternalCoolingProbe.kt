import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput

fun calculateExternalCooling(input: ExternalCoolingLegacyInput): CalculationResult<ExternalCoolingLegacyOutput> =
    ThermoraCoreFactory.createDefault().calculate(
        CalculationModelId("external-cooling-legacy"), input, ExternalCoolingLegacyOutput::class
    )

fun externalCoolingTemperature(output: ExternalCoolingLegacyOutput): Double = output.junctionTemperature.kelvin

import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.calculation.engine.execution.CalculationEngine
import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.foundation.common.result.CalculationResult

class Input : CalculationInput
class Output : CalculationOutput

fun calculate(): CalculationResult<Output> = CalculationEngine(CalculationModelRegistry())
    .calculate(CalculationModelId("smoke"), Input(), Output::class)

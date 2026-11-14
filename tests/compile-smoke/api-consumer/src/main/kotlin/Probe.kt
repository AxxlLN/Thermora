import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.common.result.CalculationResult

class Input : CalculationInput
class Output : CalculationOutput

fun calculate(model: CalculationModel<Input, Output>): CalculationResult<Output> =
    model.calculate(Input())

fun diagnostics(result: CalculationResult<Output>) = when (result) {
    is CalculationResult.Success -> result.diagnostics
    is CalculationResult.Failure -> result.diagnostics
}

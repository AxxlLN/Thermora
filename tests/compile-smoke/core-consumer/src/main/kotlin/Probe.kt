import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.common.result.CalculationResult

class Input : CalculationInput
class Output : CalculationOutput

object Model : CalculationModel<Input, Output> {
    override val descriptor = CalculationModelDescriptor(CalculationModelId("smoke"), "Smoke", "1")
    override val inputType = Input::class
    override val outputType = Output::class
    override fun validate(input: Input): ValidationReport = ValidationReport.valid()

    override fun calculate(input: Input): CalculationResult<Output> = CalculationResult.Success(Output())
}

val defaults = ThermoraCoreFactory.createDefault().availableModels()
val core = ThermoraCoreFactory.create(listOf(Model))
val result: CalculationResult<Output> = core.calculate(Model.descriptor.id, Input(), Output::class)
val diagnostics = when (result) {
    is CalculationResult.Success -> result.diagnostics
    is CalculationResult.Failure -> result.diagnostics
}

import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput

val input = SealedHousingLegacyInput(
    ambient = AmbientConditions(Temperature.fromKelvin(313.0)),
    source = ThermalSource(Quantity(4.3, StandardUnits.WATT)),
    heatedZone = HeatSourceGeometry(
        Quantity(0.112, StandardUnits.METER),
        Quantity(0.069, StandardUnits.METER),
        Quantity(0.0055, StandardUnits.METER),
        0.5
    ),
    criticalElement = CriticalElementThermalProperties(
        Quantity(1.25, StandardUnits.WATT),
        ThermalResistance(Quantity(5.0, StandardUnits.KELVIN_PER_WATT)),
        ThermalResistance(Quantity(0.5, StandardUnits.KELVIN_PER_WATT))
    ),
    elementWithRadiatorArea = Quantity(0.00423, StandardUnits.SQUARE_METER)
)

val result: CalculationResult<SealedHousingLegacyOutput> = ThermoraCoreFactory.createDefault().calculate(
    CalculationModelId("sealed-housing-legacy"), input, SealedHousingLegacyOutput::class
)

fun temperature(result: CalculationResult<SealedHousingLegacyOutput>): Temperature? = when (result) {
    is CalculationResult.Success -> result.value.enclosureTemperature
    is CalculationResult.Failure -> null
}

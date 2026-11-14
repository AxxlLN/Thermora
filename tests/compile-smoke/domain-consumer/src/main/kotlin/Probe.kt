import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits

val ambient = AmbientConditions(Temperature.fromKelvin(313.0))
val source = ThermalSource(Quantity(4.3, StandardUnits.WATT))
val power = source.power.baseValue
val dimension = source.power.unit.dimension

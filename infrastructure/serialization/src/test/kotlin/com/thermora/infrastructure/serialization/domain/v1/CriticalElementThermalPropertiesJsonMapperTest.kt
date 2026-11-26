package com.thermora.infrastructure.serialization.domain.v1
import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.temperature.Temperature
import kotlin.test.*

class CriticalElementThermalPropertiesJsonMapperTest {
 private val fixture="""{"power":{"value":0.0,"unit":{"kind":"standard","id":"W"}},"transitionToCaseResistance":{"value":5.0,"unit":{"kind":"standard","id":"K/W"}},"caseToRadiatorResistance":{"value":0.5,"unit":{"kind":"standard","id":"K/W"}}}"""
 private fun read(text:String) = StrictJsonReader.read(text) { r -> r.withReadFailures("$") { r.nextToken(); CriticalElementThermalPropertiesJsonMapper.read(r,"$") } }
 @Test fun `literal fixture round trips without computed properties`() {
  assertEquals(CriticalElementThermalProperties(Quantity(0.0,StandardUnits.WATT),ThermalResistance(Quantity(5.0,StandardUnits.KELVIN_PER_WATT)),ThermalResistance(Quantity(0.5,StandardUnits.KELVIN_PER_WATT))),read(fixture))
  assertEquals(fixture,StrictJsonWriter.write { CriticalElementThermalPropertiesJsonMapper.write(it,CriticalElementThermalProperties(Quantity(0.0,StandardUnits.WATT),ThermalResistance(Quantity(5.0,StandardUnits.KELVIN_PER_WATT)),ThermalResistance(Quantity(0.5,StandardUnits.KELVIN_PER_WATT)))) })
 }
 @Test fun `required unknown null and wrong root remain PAYLOAD`() {
  for(text in listOf("{}","null","[]",fixture.dropLast(1)+",\"extra\":1}")) {
   assertEquals(JsonFailureKind.PAYLOAD,assertFailsWith<JsonReadFailure> { read(text) }.kind)
  }
 }
}

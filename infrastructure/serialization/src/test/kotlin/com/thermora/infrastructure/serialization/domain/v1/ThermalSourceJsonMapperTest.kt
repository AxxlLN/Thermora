package com.thermora.infrastructure.serialization.domain.v1
import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.temperature.Temperature
import kotlin.test.*

class ThermalSourceJsonMapperTest {
 private val fixture="""{"power":{"value":0.0,"unit":{"kind":"standard","id":"W"}}}"""
 private fun read(text:String) = StrictJsonReader.read(text) { r -> r.withReadFailures("$") { r.nextToken(); ThermalSourceJsonMapper.read(r,"$") } }
 @Test fun `literal fixture round trips without computed properties`() {
  assertEquals(ThermalSource(Quantity(0.0,StandardUnits.WATT)),read(fixture))
  assertEquals(fixture,StrictJsonWriter.write { ThermalSourceJsonMapper.write(it,ThermalSource(Quantity(0.0,StandardUnits.WATT))) })
 }
 @Test fun `required unknown null and wrong root remain PAYLOAD`() {
  for(text in listOf("{}","null","[]",fixture.dropLast(1)+",\"extra\":1}")) {
   assertEquals(JsonFailureKind.PAYLOAD,assertFailsWith<JsonReadFailure> { read(text) }.kind)
  }
 }
}

package com.thermora.infrastructure.serialization.domain.v1
import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.domain.environment.*
import com.thermora.foundation.domain.geometry.*
import com.thermora.foundation.domain.thermal.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.temperature.Temperature
import kotlin.test.*

class HeatSourceGeometryJsonMapperTest {
 private val fixture="""{"length":{"value":1.0,"unit":{"kind":"standard","id":"m"}},"width":{"value":2.0,"unit":{"kind":"standard","id":"m"}},"height":{"value":3.0,"unit":{"kind":"standard","id":"m"}},"volumeFillCoefficient":0.5}"""
 private fun read(text:String) = StrictJsonReader.read(text) { r -> r.withReadFailures("$") { r.nextToken(); HeatSourceGeometryJsonMapper.read(r,"$") } }
 @Test fun `literal fixture round trips without computed properties`() {
  assertEquals(HeatSourceGeometry(Quantity(1.0,StandardUnits.METER),Quantity(2.0,StandardUnits.METER),Quantity(3.0,StandardUnits.METER),0.5),read(fixture))
  assertEquals(fixture,StrictJsonWriter.write { HeatSourceGeometryJsonMapper.write(it,HeatSourceGeometry(Quantity(1.0,StandardUnits.METER),Quantity(2.0,StandardUnits.METER),Quantity(3.0,StandardUnits.METER),0.5)) })
 }
 @Test fun `required unknown null and wrong root remain PAYLOAD`() {
  for(text in listOf("{}","null","[]",fixture.dropLast(1)+",\"extra\":1}")) {
   assertEquals(JsonFailureKind.PAYLOAD,assertFailsWith<JsonReadFailure> { read(text) }.kind)
  }
 }
}

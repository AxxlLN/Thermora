package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class PhysicalDimensionJsonMapperTest {
 private val fixture = """{"length":2,"mass":1,"time":-3,"electricCurrent":0,"thermodynamicTemperature":0,"amountOfSubstance":0,"luminousIntensity":0}"""
 @Test fun `independent fixture read write and determinism`() {
  val expected = PhysicalDimension.POWER
  assertEquals(expected, decode(fixture, PhysicalDimensionJsonMapper::read))
  assertEquals(fixture, encode(expected, PhysicalDimensionJsonMapper::write))
  assertEquals(fixture, encode(decode(fixture, PhysicalDimensionJsonMapper::read), PhysicalDimensionJsonMapper::write))
 }
 @Test fun `missing unknown null and wrong root are rejected`() {
  for (json in listOf("{}", "null", "[]", fixture.dropLast(1) + ",\"unknown\":0}")) {
   assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(json, PhysicalDimensionJsonMapper::read) }.kind)
  }
 }
 @Test fun `nested position is preserved for consecutive values`() {
  StrictJsonReader.read("[$fixture,$fixture]") { r ->
   r.nextToken()
   repeat(2) { r.nextToken(); assertEquals(PhysicalDimension.POWER, PhysicalDimensionJsonMapper.read(r, "$[" + it + "]")) }
   assertEquals(JsonToken.END_ARRAY, r.nextToken())
  }
 }
 @Test fun `all Int bounds and reordered properties are accepted`() {
 val value = PhysicalDimension(Int.MIN_VALUE, Int.MAX_VALUE, -1, 2, 3, 4, 5)
 assertEquals(value, decode(encode(value, PhysicalDimensionJsonMapper::write), PhysicalDimensionJsonMapper::read))
 val reversed = """{"luminousIntensity":0,"amountOfSubstance":0,"thermodynamicTemperature":0,"electricCurrent":0,"time":-3,"mass":1,"length":2}"""
 assertEquals(PhysicalDimension.POWER, decode(reversed, PhysicalDimensionJsonMapper::read))
 }
 @Test fun `bad integer forms range duplicates and missing zero are rejected`() {
 for (token in listOf("1.0", "1e0", "2147483648", "-2147483649", "null", "\"1\"", "true")) {
 assertFailsWith<JsonReadFailure> { decode(fixture.replace("\"length\":2", "\"length\":" + token), PhysicalDimensionJsonMapper::read) }
 }
 assertFailsWith<JsonReadFailure> { decode(fixture.replace("\"electricCurrent\":0,", ""), PhysicalDimensionJsonMapper::read) }
 assertFailsWith<JsonReadFailure> { decode(fixture.dropLast(1) + ",\"length\":2}", PhysicalDimensionJsonMapper::read) }
 }
}

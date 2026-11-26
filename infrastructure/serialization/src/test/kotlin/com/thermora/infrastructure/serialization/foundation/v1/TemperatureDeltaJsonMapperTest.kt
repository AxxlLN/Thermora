package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class TemperatureDeltaJsonMapperTest {
 private val fixture = """{"kelvinDelta":-10.0}"""
 @Test fun `independent fixture read write and determinism`() {
  val expected = TemperatureDelta.fromKelvin(-10.0)
  assertEquals(expected, decode(fixture, TemperatureDeltaJsonMapper::read))
  assertEquals(fixture, encode(expected, TemperatureDeltaJsonMapper::write))
  assertEquals(fixture, encode(decode(fixture, TemperatureDeltaJsonMapper::read), TemperatureDeltaJsonMapper::write))
 }
 @Test fun `missing unknown null and wrong root are rejected`() {
  for (json in listOf("{}", "null", "[]", fixture.dropLast(1) + ",\"unknown\":0}")) {
   assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(json, TemperatureDeltaJsonMapper::read) }.kind)
  }
 }
 @Test fun `nested position is preserved for consecutive values`() {
  StrictJsonReader.read("[$fixture,$fixture]") { r ->
   r.nextToken()
   repeat(2) { r.nextToken(); assertEquals(TemperatureDelta.fromKelvin(-10.0), TemperatureDeltaJsonMapper.read(r, "$[" + it + "]")) }
   assertEquals(JsonToken.END_ARRAY, r.nextToken())
  }
 }
 @Test fun `numeric grammar type overflow and underflow remain strict`() {
   for (token in listOf("\"1\"", "true", "null", "NaN", "Infinity", "1e999", "1e-999", "+1", "01")) {
    val json = fixture.replace(Regex("\"kelvinDelta\":[-0-9.]+"), "\"kelvinDelta\":" + token)
    assertFailsWith<JsonReadFailure>(token) { decode(json, TemperatureDeltaJsonMapper::read) }
   }
   assertFailsWith<JsonReadFailure> { decode(fixture.dropLast(1) + ",\"kelvinDelta\":1}", TemperatureDeltaJsonMapper::read) }
 }
 @Test fun `binary64 fields preserve raw bits`() {
   for (v in listOf(0.0, -0.0, Double.MIN_VALUE, 2 * Double.MIN_VALUE, java.lang.Double.MIN_NORMAL, Double.MAX_VALUE)) {
    val value = TemperatureDelta.fromKelvin(v)
    assertEquals(v.toRawBits(), decode(encode(value, TemperatureDeltaJsonMapper::write), TemperatureDeltaJsonMapper::read).kelvin.toRawBits())
   }
 }
 @Test fun `negative intervals do not use absolute offset`() {
 assertEquals(-273.15, decode("""{"kelvinDelta":-273.15}""", TemperatureDeltaJsonMapper::read).kelvin)
 }
}

package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class TemperatureJsonMapperTest {
 private val fixture = """{"kelvin":313.0}"""
 @Test fun `independent fixture read write and determinism`() {
  val expected = Temperature.fromKelvin(313.0)
  assertEquals(expected, decode(fixture, TemperatureJsonMapper::read))
  assertEquals(fixture, encode(expected, TemperatureJsonMapper::write))
  assertEquals(fixture, encode(decode(fixture, TemperatureJsonMapper::read), TemperatureJsonMapper::write))
 }
 @Test fun `missing unknown null and wrong root are rejected`() {
  for (json in listOf("{}", "null", "[]", fixture.dropLast(1) + ",\"unknown\":0}")) {
   assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(json, TemperatureJsonMapper::read) }.kind)
  }
 }
 @Test fun `nested position is preserved for consecutive values`() {
  StrictJsonReader.read("[$fixture,$fixture]") { r ->
   r.nextToken()
   repeat(2) { r.nextToken(); assertEquals(Temperature.fromKelvin(313.0), TemperatureJsonMapper.read(r, "$[" + it + "]")) }
   assertEquals(JsonToken.END_ARRAY, r.nextToken())
  }
 }
 @Test fun `numeric grammar type overflow and underflow remain strict`() {
   for (token in listOf("\"1\"", "true", "null", "NaN", "Infinity", "1e999", "1e-999", "+1", "01")) {
    val json = fixture.replace(Regex("\"kelvin\":[-0-9.]+"), "\"kelvin\":" + token)
    assertFailsWith<JsonReadFailure>(token) { decode(json, TemperatureJsonMapper::read) }
   }
   assertFailsWith<JsonReadFailure> { decode(fixture.dropLast(1) + ",\"kelvin\":1}", TemperatureJsonMapper::read) }
 }
 @Test fun `binary64 fields preserve raw bits`() {
   for (v in listOf(0.0, -0.0, Double.MIN_VALUE, 2 * Double.MIN_VALUE, java.lang.Double.MIN_NORMAL, Double.MAX_VALUE)) {
    val value = Temperature.fromKelvin(v)
    assertEquals(v.toRawBits(), decode(encode(value, TemperatureJsonMapper::write), TemperatureJsonMapper::read).kelvin.toRawBits())
   }
 }
 @Test fun `below zero is VALUE and source scale is not invented`() {
 assertEquals(JsonFailureKind.VALUE, assertFailsWith<JsonReadFailure> { decode("""{"kelvin":-1}""", TemperatureJsonMapper::read) }.kind)
 assertEquals(Temperature.of(40.0, TemperatureUnit.CELSIUS), decode("""{"kelvin":313.15}""", TemperatureJsonMapper::read))
 }
}

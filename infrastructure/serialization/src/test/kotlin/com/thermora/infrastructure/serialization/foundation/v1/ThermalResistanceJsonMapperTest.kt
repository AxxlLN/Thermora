package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class ThermalResistanceJsonMapperTest {
 private val fixture = """{"value":5.0,"unit":{"kind":"standard","id":"K/W"}}"""
 @Test fun `independent fixture read write and determinism`() {
  val expected = ThermalResistance(Quantity(5.0, StandardUnits.KELVIN_PER_WATT))
  assertEquals(expected, decode(fixture, ThermalResistanceJsonMapper::read))
  assertEquals(fixture, encode(expected, ThermalResistanceJsonMapper::write))
  assertEquals(fixture, encode(decode(fixture, ThermalResistanceJsonMapper::read), ThermalResistanceJsonMapper::write))
 }
 @Test fun `missing unknown null and wrong root are rejected`() {
  for (json in listOf("{}", "null", "[]", fixture.dropLast(1) + ",\"unknown\":0}")) {
   assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(json, ThermalResistanceJsonMapper::read) }.kind)
  }
 }
 @Test fun `nested position is preserved for consecutive values`() {
  StrictJsonReader.read("[$fixture,$fixture]") { r ->
   r.nextToken()
   repeat(2) { r.nextToken(); assertEquals(ThermalResistance(Quantity(5.0, StandardUnits.KELVIN_PER_WATT)), ThermalResistanceJsonMapper.read(r, "$[" + it + "]")) }
   assertEquals(JsonToken.END_ARRAY, r.nextToken())
  }
 }
 @Test fun `numeric grammar type overflow and underflow remain strict`() {
   for (token in listOf("\"1\"", "true", "null", "NaN", "Infinity", "1e999", "1e-999", "+1", "01")) {
    val json = fixture.replace(Regex("\"value\":[-0-9.]+"), "\"value\":" + token)
    assertFailsWith<JsonReadFailure>(token) { decode(json, ThermalResistanceJsonMapper::read) }
   }
   assertFailsWith<JsonReadFailure> { decode(fixture.dropLast(1) + ",\"value\":1}", ThermalResistanceJsonMapper::read) }
 }
 @Test fun `nonpositive and wrong dimension resistance are VALUE`() {
 for (q in listOf(Quantity(0.0, StandardUnits.KELVIN_PER_WATT), Quantity(-1.0, StandardUnits.KELVIN_PER_WATT), Quantity(1.0, StandardUnits.METER))) {
 assertEquals(JsonFailureKind.VALUE, assertFailsWith<JsonReadFailure> { decode(encode(q, QuantityJsonMapper::write), ThermalResistanceJsonMapper::read) }.kind)
 }
 }
}

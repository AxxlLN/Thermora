package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class QuantityJsonMapperTest {
 private val fixture = """{"value":121.0,"unit":{"kind":"standard","id":"mm"}}"""
 @Test fun `independent fixture read write and determinism`() {
  val expected = Quantity(121.0, StandardUnits.MILLIMETER)
  assertEquals(expected, decode(fixture, QuantityJsonMapper::read))
  assertEquals(fixture, encode(expected, QuantityJsonMapper::write))
  assertEquals(fixture, encode(decode(fixture, QuantityJsonMapper::read), QuantityJsonMapper::write))
 }
 @Test fun `missing unknown null and wrong root are rejected`() {
  for (json in listOf("{}", "null", "[]", fixture.dropLast(1) + ",\"unknown\":0}")) {
   assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(json, QuantityJsonMapper::read) }.kind)
  }
 }
 @Test fun `nested position is preserved for consecutive values`() {
  StrictJsonReader.read("[$fixture,$fixture]") { r ->
   r.nextToken()
   repeat(2) { r.nextToken(); assertEquals(Quantity(121.0, StandardUnits.MILLIMETER), QuantityJsonMapper.read(r, "$[" + it + "]")) }
   assertEquals(JsonToken.END_ARRAY, r.nextToken())
  }
 }
 @Test fun `numeric grammar type overflow and underflow remain strict`() {
   for (token in listOf("\"1\"", "true", "null", "NaN", "Infinity", "1e999", "1e-999", "+1", "01")) {
    val json = fixture.replace(Regex("\"value\":[-0-9.]+"), "\"value\":" + token)
    assertFailsWith<JsonReadFailure>(token) { decode(json, QuantityJsonMapper::read) }
   }
   assertFailsWith<JsonReadFailure> { decode(fixture.dropLast(1) + ",\"value\":1}", QuantityJsonMapper::read) }
 }
 @Test fun `binary64 fields preserve raw bits`() {
   for (v in listOf(0.0, -0.0, Double.MIN_VALUE, 2 * Double.MIN_VALUE, java.lang.Double.MIN_NORMAL, Double.MAX_VALUE)) {
    val value = Quantity(v, StandardUnits.METER)
    assertEquals(v.toRawBits(), decode(encode(value, QuantityJsonMapper::write), QuantityJsonMapper::read).value.toRawBits())
   }
 }
 @Test fun `affine temperature dimension and conversion overflow are VALUE`() {
 for (unit in listOf(MeasurementUnit("affine", PhysicalDimension.LENGTH, 1.0, 1.0), MeasurementUnit("K", PhysicalDimension.TEMPERATURE, 1.0), StandardUnits.KILOWATT)) {
 val json = "{\"value\":" + Double.MAX_VALUE + ",\"unit\":" + encode(unit, MeasurementUnitJsonMapper::write) + "}"
 assertEquals(JsonFailureKind.VALUE, assertFailsWith<JsonReadFailure> { decode(json, QuantityJsonMapper::read) }.kind)
 }
 }
 @Test fun `raw values and legal conversion underflow are preserved`() {
 val q = Quantity(-Double.MIN_VALUE, MeasurementUnit("tiny", PhysicalDimension.LENGTH, Double.MIN_VALUE, -0.0))
 val restored = decode(encode(q, QuantityJsonMapper::write), QuantityJsonMapper::read)
 assertEquals(q.value.toRawBits(), restored.value.toRawBits())
 assertEquals(q.unit.offsetToBase.toRawBits(), restored.unit.offsetToBase.toRawBits())
 assertEquals(q.baseValue.toRawBits(), restored.baseValue.toRawBits())
 }
}

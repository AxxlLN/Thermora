package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class MeasurementUnitJsonMapperTest {
 private fun custom(symbol: String = "custom", scale: Double = 2.0, offset: Double = -0.0) = MeasurementUnit(symbol, PhysicalDimension.LENGTH, scale, offset)
 @Test fun `custom affine and signed descriptor bits roundtrip`() {
 for (u in listOf(custom(), custom(offset = 273.15), custom(scale = Double.MIN_VALUE), custom(scale = Double.MAX_VALUE))) {
 val json = encode(u, MeasurementUnitJsonMapper::write)
 val restored = decode(json, MeasurementUnitJsonMapper::read)
 assertEquals(u, restored)
 assertEquals(u.offsetToBase.toRawBits(), restored.offsetToBase.toRawBits())
 assertEquals(json, encode(restored, MeasurementUnitJsonMapper::write))
 }
 }
 @Test fun `kind can follow all custom fields and canonicalization is allowed`() {
 val json = """{"offsetToBase":0.0,"scaleToBase":0.001,"dimension":{"length":1,"mass":0,"time":0,"electricCurrent":0,"thermodynamicTemperature":0,"amountOfSubstance":0,"luminousIntensity":0},"symbol":"mm","kind":"custom"}"""
 assertEquals(StandardUnits.MILLIMETER, decode(json, MeasurementUnitJsonMapper::read))
 assertEquals("""{"kind":"standard","id":"mm"}""", encode(decode(json, MeasurementUnitJsonMapper::read), MeasurementUnitJsonMapper::write))
 }
 @Test fun `variants and required fields are strictly checked`() {
 val valid = encode(custom(), MeasurementUnitJsonMapper::write)
 for (json in listOf("{}", "null", "[]", """{"kind":"standard"}""", """{"kind":"standard","id":"MM"}""", """{"kind":"standard","id":"mm","symbol":"mm"}""", valid.dropLast(1) + ",\"id\":\"mm\"}", valid.replace("\"offsetToBase\":-0.0", "\"extra\":0"), valid.replace("\"kind\":\"custom\",", ""), """{"kind":"unknown"}""")) {
 assertFailsWith<JsonReadFailure>(json) { decode(json, MeasurementUnitJsonMapper::read) }
 }
 }
 @Test fun `invalid scales symbols duplicates and types are rejected`() {
 val valid = encode(custom(), MeasurementUnitJsonMapper::write)
 for (json in listOf(valid.replace("\"scaleToBase\":2.0", "\"scaleToBase\":0"), valid.replace("\"symbol\":\"custom\"", "\"symbol\":\" \""), valid.replace("\"symbol\":\"custom\"", "\"symbol\":null"), valid.dropLast(1) + ",\"kind\":\"custom\"}")) {
 assertFailsWith<JsonReadFailure>(json) { decode(json, MeasurementUnitJsonMapper::read) }
 }
 }
 @Test fun `symbol scalar limit and Unicode are enforced`() {
 for (symbol in listOf("a".repeat(128), "😀".repeat(128), "é", "e\u0301")) {
 val u = custom(symbol)
 assertEquals(u, decode(encode(u, MeasurementUnitJsonMapper::write), MeasurementUnitJsonMapper::read))
 }
 for (symbol in listOf("a".repeat(129), "😀".repeat(129), "\uD800")) assertFailsWith<IllegalArgumentException> { encode(custom(symbol), MeasurementUnitJsonMapper::write) }
 val json = """{"kind":"custom","symbol":"\uD800","dimension":{"length":1,"mass":0,"time":0,"electricCurrent":0,"thermodynamicTemperature":0,"amountOfSubstance":0,"luminousIntensity":0},"scaleToBase":2.0,"offsetToBase":0.0}"""
 val error = assertFailsWith<JsonReadFailure> { decode(json, MeasurementUnitJsonMapper::read) }
 assertEquals(JsonFailureKind.PAYLOAD, error.kind)
 assertEquals("$.symbol", error.path)
 }
}

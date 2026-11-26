package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

class UnitCatalogV1Test {
  @Test fun `frozen descriptor 0`() {
   val expected = MeasurementUnit("m", PhysicalDimension(1, 0, 0, 0, 0, 0, 0), 1.0)
   assertEquals(expected, UnitCatalogV1.find("m"))
   assertEquals(expected, StandardUnits.METER)
   assertEquals("m", UnitCatalogV1.idOf(expected))
   assertEquals("{\"kind\":\"standard\",\"id\":\"m\"}", encode(expected, MeasurementUnitJsonMapper::write))
   assertEquals(expected, decode("{\"id\":\"m\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
  }
 @Test fun `frozen descriptor 1`() {
  val expected = MeasurementUnit("mm", PhysicalDimension(1, 0, 0, 0, 0, 0, 0), 0.001)
  assertEquals(expected, UnitCatalogV1.find("mm"))
  assertEquals(expected, StandardUnits.MILLIMETER)
  assertEquals("mm", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"mm\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"mm\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 2`() {
  val expected = MeasurementUnit("cm", PhysicalDimension(1, 0, 0, 0, 0, 0, 0), 0.01)
  assertEquals(expected, UnitCatalogV1.find("cm"))
  assertEquals(expected, StandardUnits.CENTIMETER)
  assertEquals("cm", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"cm\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"cm\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 3`() {
  val expected = MeasurementUnit("m²", PhysicalDimension(2, 0, 0, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("m2"))
  assertEquals(expected, StandardUnits.SQUARE_METER)
  assertEquals("m2", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"m2\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"m2\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 4`() {
  val expected = MeasurementUnit("cm²", PhysicalDimension(2, 0, 0, 0, 0, 0, 0), 0.0001)
  assertEquals(expected, UnitCatalogV1.find("cm2"))
  assertEquals(expected, StandardUnits.SQUARE_CENTIMETER)
  assertEquals("cm2", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"cm2\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"cm2\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 5`() {
  val expected = MeasurementUnit("m³", PhysicalDimension(3, 0, 0, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("m3"))
  assertEquals(expected, StandardUnits.CUBIC_METER)
  assertEquals("m3", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"m3\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"m3\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 6`() {
  val expected = MeasurementUnit("kg", PhysicalDimension(0, 1, 0, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("kg"))
  assertEquals(expected, StandardUnits.KILOGRAM)
  assertEquals("kg", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"kg\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"kg\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 7`() {
  val expected = MeasurementUnit("g", PhysicalDimension(0, 1, 0, 0, 0, 0, 0), 0.001)
  assertEquals(expected, UnitCatalogV1.find("g"))
  assertEquals(expected, StandardUnits.GRAM)
  assertEquals("g", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"g\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"g\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 8`() {
  val expected = MeasurementUnit("s", PhysicalDimension(0, 0, 1, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("s"))
  assertEquals(expected, StandardUnits.SECOND)
  assertEquals("s", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"s\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"s\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 9`() {
  val expected = MeasurementUnit("min", PhysicalDimension(0, 0, 1, 0, 0, 0, 0), 60.0)
  assertEquals(expected, UnitCatalogV1.find("min"))
  assertEquals(expected, StandardUnits.MINUTE)
  assertEquals("min", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"min\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"min\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 10`() {
  val expected = MeasurementUnit("Pa", PhysicalDimension(-1, 1, -2, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("Pa"))
  assertEquals(expected, StandardUnits.PASCAL)
  assertEquals("Pa", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"Pa\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"Pa\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 11`() {
  val expected = MeasurementUnit("kPa", PhysicalDimension(-1, 1, -2, 0, 0, 0, 0), 1000.0)
  assertEquals(expected, UnitCatalogV1.find("kPa"))
  assertEquals(expected, StandardUnits.KILOPASCAL)
  assertEquals("kPa", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"kPa\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"kPa\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 12`() {
  val expected = MeasurementUnit("MPa", PhysicalDimension(-1, 1, -2, 0, 0, 0, 0), 1000000.0)
  assertEquals(expected, UnitCatalogV1.find("MPa"))
  assertEquals(expected, StandardUnits.MEGAPASCAL)
  assertEquals("MPa", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"MPa\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"MPa\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 13`() {
  val expected = MeasurementUnit("W", PhysicalDimension(2, 1, -3, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("W"))
  assertEquals(expected, StandardUnits.WATT)
  assertEquals("W", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"W\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"W\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 14`() {
  val expected = MeasurementUnit("kW", PhysicalDimension(2, 1, -3, 0, 0, 0, 0), 1000.0)
  assertEquals(expected, UnitCatalogV1.find("kW"))
  assertEquals(expected, StandardUnits.KILOWATT)
  assertEquals("kW", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"kW\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"kW\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 15`() {
  val expected = MeasurementUnit("J", PhysicalDimension(2, 1, -2, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("J"))
  assertEquals(expected, StandardUnits.JOULE)
  assertEquals("J", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"J\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"J\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 16`() {
  val expected = MeasurementUnit("W/(m·K)", PhysicalDimension(1, 1, -3, 0, -1, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("W/(m*K)"))
  assertEquals(expected, StandardUnits.WATT_PER_METER_KELVIN)
  assertEquals("W/(m*K)", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"W/(m*K)\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"W/(m*K)\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 17`() {
  val expected = MeasurementUnit("J/(kg·K)", PhysicalDimension(2, 0, -2, 0, -1, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("J/(kg*K)"))
  assertEquals(expected, StandardUnits.JOULE_PER_KILOGRAM_KELVIN)
  assertEquals("J/(kg*K)", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"J/(kg*K)\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"J/(kg*K)\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 18`() {
  val expected = MeasurementUnit("kg/m³", PhysicalDimension(-3, 1, 0, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("kg/m3"))
  assertEquals(expected, StandardUnits.KILOGRAM_PER_CUBIC_METER)
  assertEquals("kg/m3", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"kg/m3\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"kg/m3\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 19`() {
  val expected = MeasurementUnit("W/m²", PhysicalDimension(0, 1, -3, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("W/m2"))
  assertEquals(expected, StandardUnits.WATT_PER_SQUARE_METER)
  assertEquals("W/m2", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"W/m2\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"W/m2\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 20`() {
  val expected = MeasurementUnit("m/s", PhysicalDimension(1, 0, -1, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("m/s"))
  assertEquals(expected, StandardUnits.METER_PER_SECOND)
  assertEquals("m/s", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"m/s\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"m/s\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 21`() {
  val expected = MeasurementUnit("m³/s", PhysicalDimension(3, 0, -1, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("m3/s"))
  assertEquals(expected, StandardUnits.CUBIC_METER_PER_SECOND)
  assertEquals("m3/s", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"m3/s\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"m3/s\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 22`() {
  val expected = MeasurementUnit("m²/s", PhysicalDimension(2, 0, -1, 0, 0, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("m2/s"))
  assertEquals(expected, StandardUnits.SQUARE_METER_PER_SECOND)
  assertEquals("m2/s", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"m2/s\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"m2/s\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 23`() {
  val expected = MeasurementUnit("W/(m²·K)", PhysicalDimension(0, 1, -3, 0, -1, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("W/(m2*K)"))
  assertEquals(expected, StandardUnits.WATT_PER_SQUARE_METER_KELVIN)
  assertEquals("W/(m2*K)", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"W/(m2*K)\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"W/(m2*K)\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `frozen descriptor 24`() {
  val expected = MeasurementUnit("K/W", PhysicalDimension(-2, -1, 3, 0, 1, 0, 0), 1.0)
  assertEquals(expected, UnitCatalogV1.find("K/W"))
  assertEquals(expected, StandardUnits.KELVIN_PER_WATT)
  assertEquals("K/W", UnitCatalogV1.idOf(expected))
  assertEquals("{\"kind\":\"standard\",\"id\":\"K/W\"}", encode(expected, MeasurementUnitJsonMapper::write))
  assertEquals(expected, decode("{\"id\":\"K/W\",\"kind\":\"standard\"}", MeasurementUnitJsonMapper::read))
 }
 @Test fun `catalog is complete unique and immutable`() {
 assertEquals(25, UnitCatalogV1.entries.size)
 assertEquals(25, UnitCatalogV1.entries.values.toSet().size)
 assertFailsWith<UnsupportedOperationException> { (UnitCatalogV1.entries as MutableMap).clear() }
 for (id in listOf("MM", " mm", "unknown", "w")) assertNull(UnitCatalogV1.find(id))
 }
 @Test fun `symbol alone and signed offset do not select standard`() {
 assertNull(UnitCatalogV1.idOf(MeasurementUnit("mm", PhysicalDimension.LENGTH, 2.0)))
 assertNull(UnitCatalogV1.idOf(StandardUnits.MILLIMETER.copy(offsetToBase = -0.0)))
 }
}

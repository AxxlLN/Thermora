package com.thermora.infrastructure.serialization.domain.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.domain.geometry.EnclosureGeometry
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.*

class DomainBoundaryTest {
    @Test fun `enclosure serialization never evaluates volume`() {
        val length=Quantity(Double.MAX_VALUE,StandardUnits.METER)
        val value=EnclosureGeometry(length,length,length)
        assertFailsWith<IllegalArgumentException> { value.volume }
        val json=StrictJsonWriter.write { EnclosureGeometryJsonMapper.write(it,value) }
        val restored=StrictJsonReader.read(json) { r -> r.nextToken();EnclosureGeometryJsonMapper.read(r,"$") }
        assertEquals(value,restored)
        assertFalse(json.contains("volume"))
    }
    @Test fun `ambient signed zero survives the common domain mapper`() {
        val restored=StrictJsonReader.read("""{"temperature":{"kelvin":-0.0}}""") { r -> r.nextToken();AmbientConditionsJsonMapper.read(r,"$") }
        assertEquals(Long.MIN_VALUE,restored.temperature.kelvin.toRawBits())
        assertEquals("""{"temperature":{"kelvin":-0.0}}""",StrictJsonWriter.write { AmbientConditionsJsonMapper.write(it,restored) })
    }
    @Test fun `source constructor error is typed VALUE without model validation`() {
        val failure=assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("""{"power":{"value":-1,"unit":{"kind":"standard","id":"W"}}}""") { r -> r.nextToken();ThermalSourceJsonMapper.read(r,"$") }
        }
        assertEquals(JsonFailureKind.VALUE,failure.kind)
        assertEquals("$",failure.path)
        assertNotNull(failure.cause)
    }
}

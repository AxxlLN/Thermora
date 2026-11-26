package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.domain.thermal.ThermalResistance
import kotlin.test.*

class FoundationContractTest {
    @Test fun `each custom member is required and null is never missing`() {
        val fields = listOf(
            "\"kind\":\"custom\"", "\"symbol\":\"x\"",
            "\"dimension\":" + encode(PhysicalDimension.LENGTH, PhysicalDimensionJsonMapper::write),
            "\"scaleToBase\":1.0", "\"offsetToBase\":0.0"
        )
        for (index in fields.indices) {
            val missing = fields.filterIndexed { i, _ -> i != index }.joinToString(",", "{", "}")
            assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(missing, MeasurementUnitJsonMapper::read) }.kind)
            val explicitNull = fields.mapIndexed { i, value -> if (i == index) value.substringBefore(':') + ":null" else value }.joinToString(",", "{", "}")
            assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(explicitNull, MeasurementUnitJsonMapper::read) }.kind)
        }
    }

    @Test fun `symbol length is enforced on decode as well as encode`() {
        for (symbol in listOf("a".repeat(129), "😀".repeat(129))) {
            val json = "{\"kind\":\"custom\",\"symbol\":\"$symbol\",\"dimension\":" +
                encode(PhysicalDimension.LENGTH, PhysicalDimensionJsonMapper::write) + ",\"scaleToBase\":1,\"offsetToBase\":0}"
            assertEquals(JsonFailureKind.PAYLOAD, assertFailsWith<JsonReadFailure> { decode(json, MeasurementUnitJsonMapper::read) }.kind)
        }
    }

    @Test fun `quantity and flattened resistance accept reversed fields`() {
        val json = """{"unit":{"id":"K/W","kind":"standard"},"value":5.0}"""
        assertEquals(Quantity(5.0, StandardUnits.KELVIN_PER_WATT), decode(json, QuantityJsonMapper::read))
        assertEquals(ThermalResistance(Quantity(5.0, StandardUnits.KELVIN_PER_WATT)), decode(json, ThermalResistanceJsonMapper::read))
    }

    @Test fun `resistance boundaries retain the original binary64 value`() {
        for (value in listOf(Double.MIN_VALUE, java.lang.Double.MIN_NORMAL, Double.MAX_VALUE)) {
            val r = ThermalResistance(Quantity(value, StandardUnits.KELVIN_PER_WATT))
            assertEquals(value.toRawBits(), decode(encode(r, ThermalResistanceJsonMapper::write), ThermalResistanceJsonMapper::read).value.value.toRawBits())
        }
    }

    @Test fun `custom descriptor keeps nontrivial value without SI reconstruction`() {
        val unit = MeasurementUnit("custom-length", PhysicalDimension.LENGTH, 0.0013412363482906942)
        val q = Quantity(25.54549694645235, unit)
        val restored = decode(encode(q, QuantityJsonMapper::write), QuantityJsonMapper::read)
        assertEquals(q.value.toRawBits(), restored.value.toRawBits())
        assertEquals(q.unit.scaleToBase.toRawBits(), restored.unit.scaleToBase.toRawBits())
    }
}

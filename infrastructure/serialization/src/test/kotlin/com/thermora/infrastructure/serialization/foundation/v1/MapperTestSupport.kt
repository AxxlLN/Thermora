package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.unit.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.*
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.fasterxml.jackson.core.JsonToken
import kotlin.test.*

internal fun <T> decode(json: String, read: (StrictJsonReader, String) -> T): T = StrictJsonReader.read(json) { r ->
 r.withReadFailures("$") { r.nextToken(); read(r, "$") }
}
internal fun <T> encode(value: T, write: (StrictJsonWriter, T) -> Unit): String = StrictJsonWriter.write { write(it, value) }

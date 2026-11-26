package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.domain.thermal.ThermalResistance

internal object ThermalResistanceJsonMapper {
    fun read(reader: StrictJsonReader, path: String): ThermalResistance {
        val quantity = QuantityJsonMapper.read(reader, path)
        return try { ThermalResistance(quantity) }
        catch (e: IllegalArgumentException) { throw JsonReadFailure.value(path, "Invalid thermal resistance", e) }
    }
    fun write(writer: StrictJsonWriter, value: ThermalResistance) = QuantityJsonMapper.write(writer, value.value)
}

package com.thermora.infrastructure.serialization.foundation.v1

import com.thermora.infrastructure.serialization.json.*
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.fasterxml.jackson.core.JsonToken
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

class AuditRegressionTest {
    private val dimension = """{"length":1,"mass":0,"time":0,"electricCurrent":0,"thermodynamicTemperature":0,"amountOfSubstance":0,"luminousIntensity":0}"""
    private fun unit(symbol: String, scale: String = "1.0", offset: String = "0.0") =
        """{"kind":"custom","symbol":"$symbol","dimension":$dimension,"scaleToBase":$scale,"offsetToBase":$offset}"""

    private fun <T> failure(json: String, mapper: (StrictJsonReader, String) -> T, kind: JsonFailureKind, path: String, cause: Boolean = false) {
        val error = assertFailsWith<JsonReadFailure>(json.take(100)) { decode(json, mapper) }
        assertEquals(kind, error.kind)
        assertEquals(path, error.path)
        if (cause) assertNotNull(error.cause)
    }

    @Test fun `FS01 high surrogate targets only symbol`() {
        failure(unit("\\uD800"), MeasurementUnitJsonMapper::read, JsonFailureKind.PAYLOAD, "$.symbol", true)
    }
    @Test fun `FS01 low surrogate targets only symbol`() {
        failure(unit("\\uDC00"), MeasurementUnitJsonMapper::read, JsonFailureKind.PAYLOAD, "$.symbol", true)
    }
    @Test fun `FS01 valid surrogate pair is decoded`() {
        assertEquals("😀", decode(unit("\\uD83D\\uDE00"), MeasurementUnitJsonMapper::read).symbol)
    }
    @Test fun `symbol ASCII boundary exercises specialized limit`() {
        assertEquals("x".repeat(128), decode(unit("x".repeat(128)), MeasurementUnitJsonMapper::read).symbol)
        failure(unit("x".repeat(129)), MeasurementUnitJsonMapper::read, JsonFailureKind.PAYLOAD, "$.symbol", true)
    }
    @Test fun `supplementary boundary differs from UTF16 limit`() {
        val symbol = "😀".repeat(128)
        val value = decode(unit(symbol), MeasurementUnitJsonMapper::read)
        assertEquals(256, value.symbol.length)
        assertEquals(symbol, value.symbol)
        assertEquals(value, decode(encode(value, MeasurementUnitJsonMapper::write), MeasurementUnitJsonMapper::read))
        failure(unit("😀".repeat(129)), MeasurementUnitJsonMapper::read, JsonFailureKind.PAYLOAD, "$.symbol", true)
    }
    @Test fun `overall UTF16 limit fails at root before symbol parsing`() {
        failure(" ".repeat(JsonInputLimits.MAX_JSON_BYTES + 1), MeasurementUnitJsonMapper::read, JsonFailureKind.PAYLOAD, "$", true)
    }
    @Test fun `FS02 every dimension member including zero is required`() {
        val members = listOf("\"length\":1", "\"mass\":0", "\"time\":0", "\"electricCurrent\":0", "\"thermodynamicTemperature\":0", "\"amountOfSubstance\":0", "\"luminousIntensity\":0")
        for (i in members.indices) {
            val json = members.filterIndexed { index, _ -> index != i }.joinToString(",", "{", "}")
            failure(json, PhysicalDimensionJsonMapper::read, JsonFailureKind.PAYLOAD, "$." + members[i].substringAfter('"').substringBefore('"'))
        }
    }
    @Test fun `all distinct dimension exponents have independent decoder fixture`() {
        val json = """{"length":1,"mass":-2,"time":3,"electricCurrent":-4,"thermodynamicTemperature":5,"amountOfSubstance":-6,"luminousIntensity":7}"""
        assertEquals(PhysicalDimension(1,-2,3,-4,5,-6,7), decode(json, PhysicalDimensionJsonMapper::read))
    }
    @Test fun `all distinct dimension exponents have independent encoder fixture`() {
        val expected = """{"length":1,"mass":-2,"time":3,"electricCurrent":-4,"thermodynamicTemperature":5,"amountOfSubstance":-6,"luminousIntensity":7}"""
        assertEquals(expected, encode(PhysicalDimension(1,-2,3,-4,5,-6,7), PhysicalDimensionJsonMapper::write))
    }
    @Test fun `reversed distinct dimension fixture and integer extrema`() {
        val json = """{"luminousIntensity":7,"amountOfSubstance":-6,"thermodynamicTemperature":5,"electricCurrent":-4,"time":3,"mass":-2,"length":1}"""
        assertEquals(PhysicalDimension(1,-2,3,-4,5,-6,7), decode(json, PhysicalDimensionJsonMapper::read))
        val bounds = """{"length":-2147483648,"mass":2147483647,"time":-2147483648,"electricCurrent":2147483647,"thermodynamicTemperature":-2147483648,"amountOfSubstance":2147483647,"luminousIntensity":-2147483648}"""
        val value = PhysicalDimension(Int.MIN_VALUE,Int.MAX_VALUE,Int.MIN_VALUE,Int.MAX_VALUE,Int.MIN_VALUE,Int.MAX_VALUE,Int.MIN_VALUE)
        assertEquals(value, decode(bounds, PhysicalDimensionJsonMapper::read))
        assertEquals(bounds, encode(value, PhysicalDimensionJsonMapper::write))
    }
    @Test fun `unit semantic error paths follow contract`() {
        failure("""{"kind":"standard","id":"MM"}""", MeasurementUnitJsonMapper::read, JsonFailureKind.VALUE, "$.id")
        failure("""{"kind":"other"}""", MeasurementUnitJsonMapper::read, JsonFailureKind.VALUE, "$.kind")
        failure(unit(" "), MeasurementUnitJsonMapper::read, JsonFailureKind.VALUE, "$.symbol")
        failure(unit("x", "0"), MeasurementUnitJsonMapper::read, JsonFailureKind.VALUE, "$.scaleToBase")
        failure(unit("x", offset = "1e999"), MeasurementUnitJsonMapper::read, JsonFailureKind.VALUE, "$.offsetToBase", true)
        failure(unit("x", offset = "null"), MeasurementUnitJsonMapper::read, JsonFailureKind.PAYLOAD, "$.offsetToBase", true)
    }
    @Test fun `temperature and interval structure kinds and paths`() {
        failure("{}", TemperatureJsonMapper::read, JsonFailureKind.PAYLOAD, "$.kelvin")
        failure("""{"kelvin":null}""", TemperatureJsonMapper::read, JsonFailureKind.PAYLOAD, "$.kelvin", true)
        failure("""{"kelvin":1,"kelvin":2}""", TemperatureJsonMapper::read, JsonFailureKind.PAYLOAD, "$.kelvin", true)
        failure("""{"kelvin":1,"extra":2}""", TemperatureJsonMapper::read, JsonFailureKind.PAYLOAD, "$.extra")
        failure("""{"kelvin":-1}""", TemperatureJsonMapper::read, JsonFailureKind.VALUE, "$.kelvin", true)
        for ((token, kind) in listOf("1e999" to JsonFailureKind.VALUE,"1e-999" to JsonFailureKind.VALUE,"\"1\"" to JsonFailureKind.PAYLOAD)) {
            failure("{\"kelvinDelta\":$token}", TemperatureDeltaJsonMapper::read, kind, "$.kelvinDelta", true)
        }
    }
    @Test fun `dimension error kind and exact paths`() {
        failure(dimension.replace("\"length\":1", "\"length\":2147483648"), PhysicalDimensionJsonMapper::read, JsonFailureKind.VALUE, "$.length", true)
        failure(dimension.replace("\"length\":1", "\"length\":1e0"), PhysicalDimensionJsonMapper::read, JsonFailureKind.PAYLOAD, "$.length", true)
        failure(dimension.replace("\"length\":1", "\"length\":null"), PhysicalDimensionJsonMapper::read, JsonFailureKind.PAYLOAD, "$.length", true)
    }
    @Test fun `quantity and resistance invariants keep existing object paths`() {
        failure("{\"value\":1,\"unit\":" + unit("affine",offset="1") + "}", QuantityJsonMapper::read, JsonFailureKind.VALUE,"$",true)
        failure("""{"value":0,"unit":{"kind":"standard","id":"K/W"}}""", ThermalResistanceJsonMapper::read, JsonFailureKind.VALUE,"$",true)
        failure("""{"value":1,"unit":{"kind":"standard","id":"m"}}""", ThermalResistanceJsonMapper::read, JsonFailureKind.VALUE,"$",true)
        failure("""{"unit":{"kind":"standard","id":"m"}}""", QuantityJsonMapper::read, JsonFailureKind.PAYLOAD,"$.value")
    }
    @Test fun `independent nested quantity errors retain full paths`() {
        val json = """{"item":{"value":1,"unit":{"kind":"custom","symbol":"x","dimension":{"length":1,"mass":0,"time":0,"electricCurrent":0,"thermodynamicTemperature":0,"amountOfSubstance":0,"luminousIntensity":0},"scaleToBase":0,"offsetToBase":0}}}"""
        val error = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read(json) { r -> r.nextToken(); JsonObjectFields(r,"$").required("item") { child,path -> QuantityJsonMapper.read(child,path) }.read() }
        }
        assertEquals(JsonFailureKind.VALUE,error.kind)
        assertEquals("$.item.unit.scaleToBase",error.path)
    }
    @Test fun `decimal midpoint fixtures use fixed IEEE754 patterns`() {
        val fixtures = listOf(
            "1.00000000000000011102230246251565404236316680908203125" to 0x3ff0000000000000L,
            "1.00000000000000011102230246251565404236316680908203124" to 0x3ff0000000000000L,
            "1.00000000000000011102230246251565404236316680908203126" to 0x3ff0000000000001L,
            "1.00000000000000033306690738754696212708950042724609375" to 0x3ff0000000000002L,
            "0.0" to 0L, "-0.0" to Long.MIN_VALUE,
            "-4.9e-324" to (Long.MIN_VALUE + 1),
            "2.225073858507201e-308" to 0x000fffffffffffffL,
            "2.2250738585072014e-308" to 0x0010000000000000L,
            "1.7976931348623157e308" to 0x7fefffffffffffffL,
            "-1.7976931348623157e308" to -0x0010000000000001L
        )
        for ((token,bits) in fixtures) {
            val value = decode("{\"kelvinDelta\":$token}",TemperatureDeltaJsonMapper::read)
            assertEquals(bits,value.kelvin.toRawBits(),token)
            assertEquals(bits,decode(encode(value,TemperatureDeltaJsonMapper::write),TemperatureDeltaJsonMapper::read).kelvin.toRawBits(),token)
        }
    }
    @Test fun `catalog rejects duplicate IDs before Map loses the first entry`() {
        assertFailsWith<IllegalStateException> { UnitCatalogV1.buildEntries(listOf("x" to StandardUnits.METER,"x" to StandardUnits.SECOND)) }
        assertFailsWith<IllegalStateException> { UnitCatalogV1.buildEntries(listOf("x" to StandardUnits.METER,"x" to StandardUnits.METER)) }
        assertFailsWith<IllegalStateException> { UnitCatalogV1.buildEntries(listOf("x" to StandardUnits.METER,"y" to StandardUnits.METER)) }
        val entries = UnitCatalogV1.buildEntries(listOf("x" to StandardUnits.METER))
        assertEquals(StandardUnits.METER,entries["x"])
        assertFailsWith<UnsupportedOperationException> { (entries as MutableMap).clear() }
    }
    @Test fun `parallel independent readers writers and catalog operations are deterministic`() {
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        try {
            val futures = (0 until 64).map { index -> executor.submit(Callable {
                assertTrue(start.await(10,TimeUnit.SECONDS))
                val value = Quantity(index.toDouble(),StandardUnits.MILLIMETER)
                val json = encode(value,QuantityJsonMapper::write)
                assertEquals(value,decode(json,QuantityJsonMapper::read))
                assertEquals("mm",UnitCatalogV1.idOf(StandardUnits.MILLIMETER))
                assertEquals(StandardUnits.MILLIMETER,UnitCatalogV1.find("mm"))
                json
            }) }
            start.countDown()
            futures.forEachIndexed { i,f -> assertEquals("{\"value\":$i.0,\"unit\":{\"kind\":\"standard\",\"id\":\"mm\"}}",f.get(15,TimeUnit.SECONDS)) }
        } finally {
            start.countDown(); executor.shutdownNow()
            assertTrue(executor.awaitTermination(10,TimeUnit.SECONDS))
        }
    }
    private fun <T> nestedContracts(json: String, expected: T, read: (StrictJsonReader,String)->T) {
        StrictJsonReader.read("{\"first\":$json,\"second\":$json}") { r ->
            r.nextToken()
            JsonObjectFields(r,"$")
                .required("first") { child,path -> assertEquals(expected,read(child,path)); child.expect(JsonToken.END_OBJECT) }
                .required("second") { child,path -> assertEquals(expected,read(child,path)); child.expect(JsonToken.END_OBJECT) }
                .read()
            r.expect(JsonToken.END_OBJECT)
        }
        StrictJsonReader.read("[$json,$json]") { r ->
            r.nextToken()
            repeat(2) { index ->
                assertEquals(JsonToken.START_OBJECT,r.nextToken())
                assertEquals(expected,read(r,"$[" + index + "]"))
                r.expect(JsonToken.END_OBJECT)
            }
            assertEquals(JsonToken.END_ARRAY,r.nextToken())
        }
    }
    @Test fun `all mappers preserve object array and neighboring value positions`() {
        nestedContracts(dimension,PhysicalDimension.LENGTH,PhysicalDimensionJsonMapper::read)
        nestedContracts("""{"kind":"standard","id":"m"}""",StandardUnits.METER,MeasurementUnitJsonMapper::read)
        nestedContracts("""{"value":1,"unit":{"kind":"standard","id":"m"}}""",Quantity(1.0,StandardUnits.METER),QuantityJsonMapper::read)
        nestedContracts("""{"kelvin":313}""",com.thermora.foundation.units.temperature.Temperature.fromKelvin(313.0),TemperatureJsonMapper::read)
        nestedContracts("""{"kelvinDelta":-1}""",TemperatureDelta.fromKelvin(-1.0),TemperatureDeltaJsonMapper::read)
        nestedContracts("""{"value":1,"unit":{"kind":"standard","id":"K/W"}}""",com.thermora.foundation.domain.thermal.ThermalResistance(Quantity(1.0,StandardUnits.KELVIN_PER_WATT)),ThermalResistanceJsonMapper::read)
    }
    @Test fun `interval unknown missing duplicate and null paths are explicit`() {
        failure("{}",TemperatureDeltaJsonMapper::read,JsonFailureKind.PAYLOAD,"$.kelvinDelta")
        failure("""{"kelvinDelta":null}""",TemperatureDeltaJsonMapper::read,JsonFailureKind.PAYLOAD,"$.kelvinDelta",true)
        failure("""{"kelvinDelta":1,"kelvinDelta":1}""",TemperatureDeltaJsonMapper::read,JsonFailureKind.PAYLOAD,"$.kelvinDelta",true)
        failure("""{"kelvinDelta":1,"extra":0}""",TemperatureDeltaJsonMapper::read,JsonFailureKind.PAYLOAD,"$.extra")
    }
    @Test fun `nested unit failure inside an array keeps its element index`() {
        val error = assertFailsWith<JsonReadFailure> {
            StrictJsonReader.read("""[{"kind":"standard","id":"unknown"}]""") { r ->
                r.nextToken(); r.nextToken(); MeasurementUnitJsonMapper.read(r,"$[0]")
            }
        }
        assertEquals(JsonFailureKind.VALUE,error.kind)
        assertEquals("$[0].id",error.path)
    }

    private fun <T> errorMatrix(json: String, key: String, token: String, read: (StrictJsonReader,String)->T) {
        failure(json.dropLast(1)+",\"extra\":0}",read,JsonFailureKind.PAYLOAD,"$.extra")
        failure(json.dropLast(1)+",\"$key\":0}",read,JsonFailureKind.PAYLOAD,"$."+key,true)
        failure(json.replace("\"$key\":$token", "\"$key\":null"),read,JsonFailureKind.PAYLOAD,"$."+key,true)
        failure(json.replace("\"$key\":$token", "\"$key\":true"),read,JsonFailureKind.PAYLOAD,"$."+key,true)
        failure(json.replace("\"$key\":$token,", ""),read,JsonFailureKind.PAYLOAD,"$."+key)
    }
    @Test fun `object field error matrix checks category and path across compound mappers`() {
        errorMatrix(dimension,"length","1",PhysicalDimensionJsonMapper::read)
        errorMatrix("""{"kind":"standard","id":"m"}""","kind","\"standard\"",MeasurementUnitJsonMapper::read)
        errorMatrix("""{"value":1,"unit":{"kind":"standard","id":"m"}}""","value","1",QuantityJsonMapper::read)
        errorMatrix("""{"value":1,"unit":{"kind":"standard","id":"K/W"}}""","value","1",com.thermora.infrastructure.serialization.foundation.v1.ThermalResistanceJsonMapper::read)
    }

}

package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.finnedhousing.legacy.FinnedHousingLegacyTestFixtures
import com.thermora.models.finnedhousing.legacy.input.ExternalProbeConfiguration
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.input.InternalWallGap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FinnedHousingLegacyValidatorTest {
    private val validator = FinnedHousingLegacyValidator()

    private fun input() = FinnedHousingLegacyTestFixtures.excelInput()

    private fun withWalls(walls: List<InternalWallGap>): FinnedHousingLegacyInput {
        val original = input()
        return original.copy(internalProbes = original.internalProbes.copy(wallGaps = walls))
    }

    private fun assertError(request: FinnedHousingLegacyInput, code: String, source: String) {
        val report = validator.validate(request)
        assertFalse(report.isValid)
        assertFalse(report.hasWarnings)
        val diagnostic = report.diagnostics.single()
        assertEquals(code, diagnostic.code)
        assertEquals(source, diagnostic.source)
        assertEquals(DiagnosticSeverity.ERROR, diagnostic.severity)
        assertTrue(diagnostic.message.isNotBlank())
    }

    private fun assertPackingWarning(request: FinnedHousingLegacyInput) {
        val report = validator.validate(request)
        assertTrue(report.isValid)
        assertTrue(report.hasWarnings)
        val diagnostic = report.diagnostics.single()
        assertEquals("FINNED_HOUSING_INVALID_FIN_PACKING", diagnostic.code)
        assertEquals("fins", diagnostic.source)
        assertEquals(DiagnosticSeverity.WARNING, diagnostic.severity)
    }

    @Test
    fun `Excel fixture passes without errors or warnings`() {
        val report = validator.validate(input())
        assertTrue(report.isValid)
        assertFalse(report.hasWarnings)
        assertTrue(report.diagnostics.isEmpty())
    }

    @Test
    fun `fixture retains all recorded probe inputs`() {
        val request = input()
        assertEquals(313.0, request.ambient.temperature.kelvin)
        assertEquals(4.3, request.source.power.baseValue)
        assertEquals(1.25, request.criticalElement.power.baseValue)
        assertEquals(0.00423, request.elementWithRadiatorArea.baseValue)
        assertEquals(listOf(0.121, 0.078, 0.039), listOf(request.enclosure.length.baseValue,
            request.enclosure.width.baseValue, request.enclosure.height.baseValue))
        assertEquals(listOf(0.003, 0.025, 0.039, 0.00533), listOf(request.fins.width.baseValue,
            request.fins.height.baseValue, request.fins.length.baseValue, request.fins.spacing.baseValue))
        assertEquals(10, request.fins.countPerSide)
        assertEquals(0.9, request.enclosureEmissivity)
        assertEquals(listOf(0.112, 0.069, 0.0055, 0.5), listOf(request.heatedZone.length.baseValue,
            request.heatedZone.width.baseValue, request.heatedZone.height.baseValue, request.heatedZone.volumeFillCoefficient))
        assertEquals(listOf(5.0, 0.5), listOf(request.criticalElement.transitionToCaseResistance.value.baseValue,
            request.criticalElement.caseToRadiatorResistance.value.baseValue))
        assertEquals(listOf(5.0, 10.0, 15.0), request.externalProbes.enclosureRises.map { it.kelvin })
        assertEquals((1..6).toList(), request.internalProbes.wallGaps.map { it.wallIndex })
        assertEquals(listOf(0.003, 0.003, 0.003, 0.003, 0.0265, 0.004),
            request.internalProbes.wallGaps.map { it.gap.baseValue })
        assertEquals(listOf(1.0, 1.0, 1.0, 1.0, 0.7, 1.3),
            request.internalProbes.wallGaps.map { it.convectionMultiplier })
        assertEquals(360.63098423378119, request.internalProbes.referenceZoneTemperature.kelvin)
        assertEquals(listOf(10.0, 20.0, 40.0), request.internalProbes.trialZoneRises.map { it.kelvin })
        assertEquals(listOf(5.0556031543490354, 4.5439787678362187, 4.0195022659471871),
            request.internalProbes.trialResistances.map { it.value.baseValue })
    }

    @Test
    fun `zero total power is a model validation error`() {
        assertError(input().copy(source = ThermalSource(Quantity(0.0, StandardUnits.WATT))),
            "FINNED_HOUSING_INVALID_TOTAL_POWER", "source.power")
    }

    @Test
    fun `negative total power is rejected by Foundation`() {
        assertFailsWith<IllegalArgumentException> { ThermalSource(Quantity(-1.0, StandardUnits.WATT)) }
    }

    @Test
    fun `element area must have area dimension`() {
        listOf(StandardUnits.METER, StandardUnits.WATT).forEach { unit ->
            assertError(input().copy(elementWithRadiatorArea = Quantity(1.0, unit)),
                "FINNED_HOUSING_INVALID_ELEMENT_AREA", "elementWithRadiatorArea")
        }
    }

    @Test
    fun `element area must be positive`() {
        listOf(0.0, -0.0, -1.0).forEach { area ->
            assertError(input().copy(elementWithRadiatorArea = Quantity(area, StandardUnits.SQUARE_METER)),
                "FINNED_HOUSING_INVALID_ELEMENT_AREA", "elementWithRadiatorArea")
        }
    }

    @Test
    fun `emissivity must be above zero`() {
        listOf(-1.0, -Double.MIN_VALUE, -0.0, 0.0).forEach { emissivity ->
            assertError(input().copy(enclosureEmissivity = emissivity),
                "FINNED_HOUSING_INVALID_EMISSIVITY", "enclosureEmissivity")
        }
    }

    @Test
    fun `emissivity must not exceed one`() {
        listOf(Math.nextUp(1.0), 2.0, Double.MAX_VALUE).forEach { emissivity ->
            assertError(input().copy(enclosureEmissivity = emissivity),
                "FINNED_HOUSING_INVALID_EMISSIVITY", "enclosureEmissivity")
        }
    }

    @Test
    fun `emissivity must be finite`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { emissivity ->
            assertError(input().copy(enclosureEmissivity = emissivity),
                "FINNED_HOUSING_INVALID_EMISSIVITY", "enclosureEmissivity")
        }
    }

    @Test
    fun `both allowed emissivity boundaries are valid`() {
        listOf(Double.MIN_VALUE, 1.0).forEach { emissivity ->
            assertTrue(validator.validate(input().copy(enclosureEmissivity = emissivity)).isValid)
        }
    }

    @Test
    fun `missing or extra walls are rejected`() {
        val walls = input().internalProbes.wallGaps
        listOf(emptyList(), walls.dropLast(1), walls + walls.first()).forEach { changed ->
            assertError(withWalls(changed), "FINNED_HOUSING_INVALID_WALL_CONFIGURATION", "internalProbes.wallGaps")
        }
    }

    @Test
    fun `duplicate wall indices are rejected`() {
        val walls = input().internalProbes.wallGaps.toMutableList()
        walls[5] = walls[5].copy(wallIndex = 1)
        assertError(withWalls(walls), "FINNED_HOUSING_INVALID_WALL_CONFIGURATION", "internalProbes.wallGaps")
    }

    @Test
    fun `wall indices outside one through six are rejected`() {
        listOf(Int.MIN_VALUE, 0, 7, Int.MAX_VALUE).forEach { index ->
            val walls = input().internalProbes.wallGaps.toMutableList()
            walls[0] = walls[0].copy(wallIndex = index)
            assertError(withWalls(walls), "FINNED_HOUSING_INVALID_WALL_CONFIGURATION", "internalProbes.wallGaps")
        }
    }

    @Test
    fun `wall ordering does not affect validity`() {
        assertTrue(validator.validate(withWalls(input().internalProbes.wallGaps.reversed())).isValid)
    }

    @Test
    fun `wall gaps must be positive`() {
        listOf(0.0, -1.0).forEach { gap ->
            val walls = input().internalProbes.wallGaps.toMutableList()
            walls[0] = walls[0].copy(gap = Quantity(gap, StandardUnits.METER))
            assertError(withWalls(walls), "FINNED_HOUSING_INVALID_WALL_PARAMETERS", "internalProbes.wallGaps[1]")
        }
    }

    @Test
    fun `wall gaps must have length dimension`() {
        val walls = input().internalProbes.wallGaps.toMutableList()
        walls[0] = walls[0].copy(gap = Quantity(1.0, StandardUnits.SQUARE_METER))
        assertError(withWalls(walls), "FINNED_HOUSING_INVALID_WALL_PARAMETERS", "internalProbes.wallGaps[1]")
    }

    @Test
    fun `wall multipliers must be finite and positive`() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { multiplier ->
            val walls = input().internalProbes.wallGaps.toMutableList()
            walls[0] = walls[0].copy(convectionMultiplier = multiplier)
            assertError(withWalls(walls), "FINNED_HOUSING_INVALID_WALL_PARAMETERS", "internalProbes.wallGaps[1]")
        }
    }

    @Test
    fun `positive wall multipliers are not restricted to the reference values`() {
        listOf(Double.MIN_VALUE, 2.0, Double.MAX_VALUE).forEach { multiplier ->
            val walls = input().internalProbes.wallGaps.map { it.copy(convectionMultiplier = multiplier) }
            assertTrue(validator.validate(withWalls(walls)).isValid)
        }
    }

    @Test
    fun `exactly three external probes are required`() {
        listOf(0, 1, 2, 4).forEach { size ->
            val probes = ExternalProbeConfiguration(List(size) { TemperatureDelta.fromKelvin(5.0) })
            assertError(input().copy(externalProbes = probes),
                "FINNED_HOUSING_INVALID_EXTERNAL_PROBES", "externalProbes.enclosureRises")
        }
    }

    @Test
    fun `external probe rises must be positive`() {
        listOf(0.0, -1.0).forEach { rise ->
            val probes = ExternalProbeConfiguration(listOf(rise, 10.0, 15.0).map { TemperatureDelta.fromKelvin(it) })
            assertError(input().copy(externalProbes = probes),
                "FINNED_HOUSING_INVALID_EXTERNAL_PROBES", "externalProbes.enclosureRises")
        }
    }

    @Test
    fun `exactly three internal rises and resistances are required independently`() {
        val original = input()
        listOf(0, 1, 2, 4).forEach { size ->
            assertError(original.copy(internalProbes = original.internalProbes.copy(
                trialZoneRises = List(size) { TemperatureDelta.fromKelvin(10.0) })),
                "FINNED_HOUSING_INVALID_INTERNAL_PROBES", "internalProbes")
            assertError(original.copy(internalProbes = original.internalProbes.copy(
                trialResistances = List(size) { original.internalProbes.trialResistances.first() })),
                "FINNED_HOUSING_INVALID_INTERNAL_PROBES", "internalProbes")
        }
    }

    @Test
    fun `internal probe rises must be positive`() {
        val original = input()
        listOf(0.0, -1.0).forEach { rise ->
            assertError(original.copy(internalProbes = original.internalProbes.copy(
                trialZoneRises = listOf(rise, 20.0, 40.0).map { TemperatureDelta.fromKelvin(it) })),
                "FINNED_HOUSING_INVALID_INTERNAL_RISE", "internalProbes.trialZoneRises")
        }
    }

    @Test
    fun `nonpositive and nonfinite resistances are rejected by Foundation`() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { resistance ->
            assertFailsWith<IllegalArgumentException> {
                ThermalResistance(Quantity(resistance, StandardUnits.KELVIN_PER_WATT))
            }
        }
        assertFailsWith<IllegalArgumentException> { ThermalResistance(Quantity(1.0, StandardUnits.WATT)) }
    }

    @Test
    fun `nonfinite quantities and temperatures are rejected by Foundation`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { Quantity(value, StandardUnits.SQUARE_METER) }
            assertFailsWith<IllegalArgumentException> { Quantity(value, StandardUnits.METER) }
            assertFailsWith<IllegalArgumentException> { Quantity(value, StandardUnits.WATT) }
            assertFailsWith<IllegalArgumentException> { Temperature.fromKelvin(value) }
            assertFailsWith<IllegalArgumentException> { TemperatureDelta.fromKelvin(value) }
        }
    }

    @Test
    fun `invalid fin geometry is rejected by Foundation`() {
        val fins = input().fins
        listOf(0.0, -1.0).forEach { value ->
            assertFailsWith<IllegalArgumentException> { fins.copy(width = Quantity(value, StandardUnits.METER)) }
            assertFailsWith<IllegalArgumentException> { fins.copy(height = Quantity(value, StandardUnits.METER)) }
            assertFailsWith<IllegalArgumentException> { fins.copy(length = Quantity(value, StandardUnits.METER)) }
            assertFailsWith<IllegalArgumentException> { fins.copy(spacing = Quantity(value, StandardUnits.METER)) }
        }
        assertFailsWith<IllegalArgumentException> { fins.copy(countPerSide = 0) }
        assertFailsWith<IllegalArgumentException> { fins.copy(countPerSide = -1) }
        assertFailsWith<IllegalArgumentException> { fins.copy(width = Quantity(1.0, StandardUnits.SQUARE_METER)) }
    }

    @Test
    fun `oversized fin packing only warns and leaves the input valid`() {
        val original = input()
        assertPackingWarning(original.copy(fins = original.fins.copy(countPerSide = 11)))
    }

    @Test
    fun `fin packing comparison handles an exact boundary without an epsilon`() {
        val original = input()
        val request = original.copy(
            enclosure = original.enclosure.copy(width = Quantity(0.078125, StandardUnits.METER)),
            fins = original.fins.copy(countPerSide = 2, width = Quantity(0.03125, StandardUnits.METER),
                spacing = Quantity(0.015625, StandardUnits.METER))
        )
        assertTrue(validator.validate(request).diagnostics.isEmpty())
        assertPackingWarning(request.copy(enclosure = request.enclosure.copy(
            width = Quantity(Math.nextDown(0.078125), StandardUnits.METER))))
    }

    @Test
    fun `overflowing occupied width produces a warning instead of an exception`() {
        val original = input()
        val request = original.copy(
            enclosure = original.enclosure.copy(width = Quantity(Double.MAX_VALUE, StandardUnits.METER)),
            fins = original.fins.copy(countPerSide = Int.MAX_VALUE,
                width = Quantity(Double.MAX_VALUE, StandardUnits.METER),
                spacing = Quantity(Double.MAX_VALUE, StandardUnits.METER))
        )
        assertPackingWarning(request)
    }

    @Test
    fun `one fin has no interfin spacing contribution`() {
        val original = input()
        val request = original.copy(fins = original.fins.copy(countPerSide = 1,
            spacing = Quantity(Double.MAX_VALUE, StandardUnits.METER)))
        assertTrue(validator.validate(request).diagnostics.isEmpty())
    }

    @Test
    fun `packing does not warn because of intermediate rounding`() {
        val original = input()
        val request = original.copy(
            enclosure = original.enclosure.copy(width = Quantity(0.24237341685453387, StandardUnits.METER)),
            fins = original.fins.copy(countPerSide = 509,
                width = Quantity(0.0002787593681302051, StandardUnits.METER),
                spacing = Quantity(0.0001978049182603533, StandardUnits.METER))
        )
        assertTrue(validator.validate(request).diagnostics.isEmpty())
    }

    @Test
    fun `reference zone temperature must exceed ambient before future heat transfer calculations`() {
        val original = input()
        listOf(0.0, 300.0, 313.0).forEach { temperature ->
            assertError(original.copy(internalProbes = original.internalProbes.copy(
                referenceZoneTemperature = Temperature.fromKelvin(temperature))),
                "FINNED_HOUSING_INVALID_REFERENCE_TEMPERATURE", "internalProbes.referenceZoneTemperature")
        }
    }

    @Test
    fun `validation preserves model errors together with engineering warnings`() {
        val original = input()
        val request = original.copy(source = ThermalSource(Quantity(0.0, StandardUnits.WATT)),
            fins = original.fins.copy(countPerSide = 11), enclosureEmissivity = 0.0)
        val report = validator.validate(request)
        assertFalse(report.isValid)
        assertTrue(report.hasWarnings)
        assertEquals(listOf("FINNED_HOUSING_INVALID_TOTAL_POWER", "FINNED_HOUSING_INVALID_EMISSIVITY",
            "FINNED_HOUSING_INVALID_FIN_PACKING"), report.diagnostics.map { it.code })
        assertEquals(listOf(DiagnosticSeverity.ERROR, DiagnosticSeverity.ERROR, DiagnosticSeverity.WARNING),
            report.diagnostics.map { it.severity })
        assertEquals(report, validator.validate(request))
    }

    @Test
    fun `equivalent physical units do not change validation`() {
        val original = input()
        val request = original.copy(
            source = ThermalSource(original.source.power.convertTo(StandardUnits.KILOWATT)),
            elementWithRadiatorArea = original.elementWithRadiatorArea.convertTo(StandardUnits.SQUARE_CENTIMETER),
            enclosure = original.enclosure.copy(width = original.enclosure.width.convertTo(StandardUnits.MILLIMETER)),
            fins = original.fins.copy(width = original.fins.width.convertTo(StandardUnits.MILLIMETER),
                spacing = original.fins.spacing.convertTo(StandardUnits.MILLIMETER)),
            internalProbes = original.internalProbes.copy(wallGaps = original.internalProbes.wallGaps.map {
                it.copy(gap = it.gap.convertTo(StandardUnits.MILLIMETER))
            })
        )
        assertEquals(validator.validate(original), validator.validate(request))
    }

    @Test
    fun `positive probe values do not gain unconfirmed ordering restrictions`() {
        val original = input()
        val rises = listOf(20.0, 10.0, 10.0).map { TemperatureDelta.fromKelvin(it) }
        val request = original.copy(externalProbes = ExternalProbeConfiguration(rises),
            internalProbes = original.internalProbes.copy(trialZoneRises = rises))
        assertTrue(validator.validate(request).isValid)
    }
}

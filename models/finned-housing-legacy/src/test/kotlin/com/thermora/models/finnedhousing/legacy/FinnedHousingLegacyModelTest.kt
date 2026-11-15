package com.thermora.models.finnedhousing.legacy

import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureUnit
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingLegacyOutput
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FinnedHousingLegacyModelTest {
    private val model = FinnedHousingLegacyModel()
    private fun input() = FinnedHousingLegacyTestFixtures.excelInput()
    private fun success(request: FinnedHousingLegacyInput = input()) =
        assertIs<CalculationResult.Success<FinnedHousingLegacyOutput>>(model.calculate(request))

    private fun values(output: FinnedHousingLegacyOutput): List<Double> {
        val final = output.finalResults
        val geometry = output.geometryResults
        val internal = output.internalHeatTransfer
        return listOf(final.enclosureRise.kelvin, final.enclosureToAmbientResistance.value.baseValue,
            final.heatedZoneRise.kelvin, final.heatedZoneToAmbientResistance.value.baseValue,
            final.enclosureTemperature.kelvin, final.heatedZoneTemperature.kelvin,
            final.internalAirTemperature.kelvin, final.elementTemperature.kelvin, final.junctionTemperature.kelvin,
            geometry.finnedSurfaceArea.baseValue, geometry.smoothSideArea.baseValue, geometry.smoothFaceArea.baseValue,
            geometry.smoothSurfaceArea.baseValue, geometry.originalEnclosureArea.baseValue, geometry.heatedZoneArea.baseValue,
            geometry.heatedZoneFaceAreaXY.baseValue, geometry.heatedZoneFaceAreaXZ.baseValue, geometry.heatedZoneFaceAreaYZ.baseValue,
            internal.enclosureTemperature.kelvin, internal.referenceZoneTemperature.kelvin, internal.meanGapTemperature.kelvin,
            internal.reducedEmissivity, internal.radiationCoefficient.baseValue, internal.zoneToEnclosureResistance.value.baseValue
        ) + internal.wallConvectionCoefficients.map { it.baseValue } +
            internal.trialResults.flatMap { listOf(it.zoneTemperature.kelvin, it.zoneRise.kelvin,
                it.resistance.value.baseValue, it.dissipatedPower.baseValue) } +
            output.externalHeatTransfer.probes.flatMap { listOf(it.enclosureRise.kelvin, it.smoothMeanTemperature.kelvin,
                it.smoothRadiationCoefficient.baseValue, it.smoothConvectionCoefficient.baseValue,
                it.channelParameter, it.channelCorrection, it.channelAirTemperature.kelvin, it.finnedMeanTemperature.kelvin,
                it.finnedRadiationCoefficient.baseValue, it.finnedConvectionCoefficient.baseValue,
                it.enclosureToAmbientResistance.value.baseValue, it.dissipatedPower.baseValue) }
    }

    @Test
    fun `model declares stable identity and runtime types`() {
        assertEquals("finned-housing-legacy", model.descriptor.id.value)
        assertEquals("1.0.0", model.descriptor.version)
        assertEquals(FinnedHousingLegacyInput::class, model.inputType)
        assertEquals(FinnedHousingLegacyOutput::class, model.outputType)
    }

    @Test
    fun `public model exposes all finite intermediate values and the nine Excel outputs`() {
        val result = success()
        val expected = listOf(7.6309842337811995, 1.774647496228186, 27.144910812935397,
            6.312769956496604, 320.63098423378119, 340.14491081293539,
            330.38794752335832, 341.03115170746338, 347.90615170746338)
        expected.zip(values(result.value).take(9)).forEach { (reference, value) -> assertEquals(reference, value, 1e-9) }
        assertTrue(values(result.value).all { it.isFinite() })
        assertEquals(listOf("FINNED_HOUSING_LEGACY_APPLICABILITY_UNKNOWN", "FINNED_HOUSING_LEGACY_FIXED_CORRELATIONS"),
            result.diagnostics.map { it.code })
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
    }

    @Test
    fun `output quantities retain their physical dimensions`() {
        val output = success().value
        assertEquals(PhysicalDimension.AREA, output.geometryResults.finnedSurfaceArea.unit.dimension)
        assertEquals(PhysicalDimension.THERMAL_RESISTANCE,
            output.finalResults.heatedZoneToAmbientResistance.value.unit.dimension)
        output.externalHeatTransfer.probes.forEach {
            listOf(it.smoothRadiationCoefficient, it.smoothConvectionCoefficient,
                it.finnedRadiationCoefficient, it.finnedConvectionCoefficient).forEach { coefficient ->
                assertEquals(PhysicalDimension.HEAT_TRANSFER_COEFFICIENT, coefficient.unit.dimension)
            }
            assertEquals(PhysicalDimension.POWER, it.dissipatedPower.unit.dimension)
        }
        output.internalHeatTransfer.wallConvectionCoefficients.forEach {
            assertEquals(PhysicalDimension.HEAT_TRANSFER_COEFFICIENT, it.unit.dimension)
        }
    }

    @Test
    fun `invalid inputs return their validation diagnostics before calculations`() {
        val original = input()
        listOf(original.copy(source = ThermalSource(Quantity(0.0, StandardUnits.WATT))),
            original.copy(elementWithRadiatorArea = Quantity(1.0, StandardUnits.METER)),
            original.copy(enclosureEmissivity = Double.NaN),
            original.copy(externalProbes = original.externalProbes.copy(enclosureRises = emptyList()))
        ).forEach { request ->
            val validation = model.validate(request)
            assertFalse(validation.isValid)
            assertEquals(validation.diagnostics, assertIs<CalculationResult.Failure>(model.calculate(request)).diagnostics)
        }
    }

    @Test
    fun `public validation completes the comparison with calculated enclosure temperature`() {
        val original = input()
        listOf(315.0, 320.63098423378119).forEach { reference ->
            val request = original.copy(internalProbes = original.internalProbes.copy(
                referenceZoneTemperature = Temperature.fromKelvin(reference)))
            val report = model.validate(request)
            assertFalse(report.isValid)
            assertEquals("FINNED_HOUSING_INVALID_REFERENCE_TEMPERATURE", report.diagnostics.single().code)
            assertEquals(report.diagnostics, assertIs<CalculationResult.Failure>(model.calculate(request)).diagnostics)
        }
    }

    @Test
    fun `changed fins alter geometry and trial transfer but preserve fixed final results`() {
        val original = input()
        val before = success(original).value
        val after = success(original.copy(fins = original.fins.copy(height = Quantity(0.02, StandardUnits.METER)))).value
        assertNotEquals(before.geometryResults.finnedSurfaceArea, after.geometryResults.finnedSurfaceArea)
        assertNotEquals(before.geometryResults.smoothSurfaceArea, after.geometryResults.smoothSurfaceArea)
        assertNotEquals(before.externalHeatTransfer.probes[0].finnedRadiationCoefficient,
            after.externalHeatTransfer.probes[0].finnedRadiationCoefficient)
        assertNotEquals(before.externalHeatTransfer.probes[0].dissipatedPower,
            after.externalHeatTransfer.probes[0].dissipatedPower)
        assertEquals(before.finalResults, after.finalResults)
        assertEquals(before.internalHeatTransfer, after.internalHeatTransfer)
    }

    @Test
    fun `manual trial resistance changes its power without changing the computed gap resistance`() {
        val original = input()
        val resistances = original.internalProbes.trialResistances.toMutableList()
        resistances[0] = ThermalResistance(Quantity(10.111206308698071, StandardUnits.KELVIN_PER_WATT))
        val before = success(original).value
        val after = success(original.copy(internalProbes = original.internalProbes.copy(trialResistances = resistances))).value
        assertEquals(0.9890016774158384, after.internalHeatTransfer.trialResults[0].dissipatedPower.baseValue, 1e-12)
        assertEquals(before.internalHeatTransfer.zoneToEnclosureResistance, after.internalHeatTransfer.zoneToEnclosureResistance)
        assertEquals(before.finalResults, after.finalResults)
    }

    @Test
    fun `manual reference temperature is not replaced by the final zone temperature`() {
        val original = input()
        val reference = Temperature.fromKelvin(371.0)
        val before = success(original).value
        val after = success(original.copy(internalProbes = original.internalProbes.copy(referenceZoneTemperature = reference))).value
        assertEquals(reference, after.internalHeatTransfer.referenceZoneTemperature)
        assertNotEquals(after.finalResults.heatedZoneTemperature, after.internalHeatTransfer.referenceZoneTemperature)
        assertNotEquals(before.internalHeatTransfer.radiationCoefficient, after.internalHeatTransfer.radiationCoefficient)
        assertEquals(before.finalResults, after.finalResults)
    }

    @Test
    fun `wall order does not change the indexed six wall calculation`() {
        val original = input()
        assertEquals(success(original), success(original.copy(internalProbes = original.internalProbes.copy(
            wallGaps = original.internalProbes.wallGaps.reversed()))))
    }

    @Test
    fun `repeatability is independent of preceding requests`() {
        val original = input()
        val expected = success(original)
        success(original.copy(source = ThermalSource(Quantity(8.6, StandardUnits.WATT))))
        assertEquals(expected, success(original))
        assertEquals(expected, assertIs<CalculationResult.Success<FinnedHousingLegacyOutput>>(
            FinnedHousingLegacyModel().calculate(original)))
    }

    @Test
    fun `one model instance supports independent parallel calculations`() {
        val expected = success()
        val executor = Executors.newFixedThreadPool(4)
        try {
            val futures = (1..8).map { executor.submit<CalculationResult<FinnedHousingLegacyOutput>> { model.calculate(input()) } }
            futures.forEach { assertEquals(expected, it.get(10, TimeUnit.SECONDS)) }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `returned probe lists do not alias input lists`() {
        val original = input()
        val rises = original.externalProbes.enclosureRises.toMutableList()
        val trials = original.internalProbes.trialZoneRises.toMutableList()
        val resistances = original.internalProbes.trialResistances.toMutableList()
        val request = original.copy(externalProbes = original.externalProbes.copy(enclosureRises = rises),
            internalProbes = original.internalProbes.copy(trialZoneRises = trials, trialResistances = resistances))
        val result = success(request)
        rises.clear()
        trials.clear()
        resistances.clear()
        assertEquals(success(original), result)
    }

    @Test
    fun `packing mismatch remains a validation warning instead of blocking the model`() {
        val original = input()
        val request = original.copy(fins = original.fins.copy(countPerSide = 11))
        val validation = model.validate(request)
        assertTrue(validation.isValid)
        assertTrue(validation.hasWarnings)
        assertEquals("FINNED_HOUSING_INVALID_FIN_PACKING", validation.diagnostics.single().code)
        success(request)
    }

    @Test
    fun `negative channel correction returns a cell specific numerical failure`() {
        val original = input()
        val request = original.copy(fins = original.fins.copy(spacing = Quantity(0.0001, StandardUnits.METER)))
        val failure = assertIs<CalculationResult.Failure>(model.calculate(request))
        assertEquals("FINNED_HOUSING_CALCULATION_ERROR", failure.diagnostics.single().code)
        assertEquals("L5", failure.diagnostics.single().details["cell"])
    }

    @Test
    fun `correction above one warns without introducing an unproven hard limit`() {
        val original = input()
        val result = success(original.copy(fins = original.fins.copy(countPerSide = 1,
            spacing = Quantity(0.03, StandardUnits.METER))))
        assertTrue(result.value.externalHeatTransfer.probes.any { it.channelCorrection > 1.0 })
        assertTrue(result.value.externalHeatTransfer.probes.any { it.channelAirTemperature.kelvin < 313.0 })
        assertTrue(result.diagnostics.any { it.code == "FINNED_HOUSING_CHANNEL_CORRECTION_OUTSIDE_RANGE" })
        assertTrue(values(result.value).all { it.isFinite() })
    }

    @Test
    fun `unrepresentable mandatory intermediates and junction temperatures fail diagnostically`() {
        val original = input()
        val requests = listOf(
            original.copy(fins = original.fins.copy(length = Quantity(Double.MAX_VALUE, StandardUnits.METER))),
            original.copy(internalProbes = original.internalProbes.copy(referenceZoneTemperature = Temperature.fromKelvin(Double.MAX_VALUE))),
            original.copy(criticalElement = original.criticalElement.copy(
                transitionToCaseResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT)),
                caseToRadiatorResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT))))
        )
        requests.forEach { request ->
            val failure = assertIs<CalculationResult.Failure>(model.calculate(request))
            assertEquals("FINNED_HOUSING_CALCULATION_ERROR", failure.diagnostics.single().code)
            assertEquals(DiagnosticSeverity.ERROR, failure.diagnostics.single().severity)
            assertTrue(failure.diagnostics.single().details.getValue("cell").isNotBlank())
        }
    }

    @Test
    fun `equivalent SI and scaled input units preserve every numerical output`() {
        val original = input()
        val request = original.copy(
            ambient = AmbientConditions(Temperature.of(original.ambient.temperature.inUnit(TemperatureUnit.CELSIUS), TemperatureUnit.CELSIUS)),
            source = ThermalSource(original.source.power.convertTo(StandardUnits.KILOWATT)),
            elementWithRadiatorArea = original.elementWithRadiatorArea.convertTo(StandardUnits.SQUARE_CENTIMETER),
            enclosure = original.enclosure.copy(length = original.enclosure.length.convertTo(StandardUnits.MILLIMETER),
                width = original.enclosure.width.convertTo(StandardUnits.MILLIMETER),
                height = original.enclosure.height.convertTo(StandardUnits.MILLIMETER)),
            fins = original.fins.copy(width = original.fins.width.convertTo(StandardUnits.MILLIMETER),
                height = original.fins.height.convertTo(StandardUnits.MILLIMETER),
                length = original.fins.length.convertTo(StandardUnits.MILLIMETER),
                spacing = original.fins.spacing.convertTo(StandardUnits.MILLIMETER)),
            heatedZone = original.heatedZone.copy(length = original.heatedZone.length.convertTo(StandardUnits.MILLIMETER),
                width = original.heatedZone.width.convertTo(StandardUnits.MILLIMETER),
                height = original.heatedZone.height.convertTo(StandardUnits.MILLIMETER)),
            criticalElement = original.criticalElement.copy(power = original.criticalElement.power.convertTo(StandardUnits.KILOWATT)),
            internalProbes = original.internalProbes.copy(
                referenceZoneTemperature = Temperature.of(original.internalProbes.referenceZoneTemperature.inUnit(TemperatureUnit.CELSIUS), TemperatureUnit.CELSIUS),
                wallGaps = original.internalProbes.wallGaps.map {
                it.copy(gap = it.gap.convertTo(StandardUnits.MILLIMETER)) })
        )
        val before = values(success(original).value)
        val after = values(success(request).value)
        assertEquals(before.size, after.size)
        before.zip(after).forEach { (expected, actual) -> assertEquals(expected, actual, max(1e-11, abs(expected) * 1e-12)) }
    }

    @Test
    fun `zero element power keeps zero junction heating even with maximum resistances`() {
        val original = input()
        val result = success(original.copy(criticalElement = original.criticalElement.copy(
            power = Quantity(0.0, StandardUnits.WATT),
            transitionToCaseResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT)),
            caseToRadiatorResistance = ThermalResistance(Quantity(Double.MAX_VALUE, StandardUnits.KELVIN_PER_WATT)))))
        assertEquals(333.35868310970153, result.value.finalResults.elementTemperature.kelvin, 1e-12)
        assertEquals(result.value.finalResults.elementTemperature, result.value.finalResults.junctionTemperature)
    }

    @Test
    fun `small zone rises and underflowing denominators retain independently computed element temperatures`() {
        val original = input()
        listOf(
            listOf(1e-24, 2.5e-25, 1e-18, 313.0000237788193),
            listOf(1e-200, 2.5e-201, 1e-200, 2.5225630139173217e27),
            listOf(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, 3.0848663796669844e46)
        ).forEach { (power, elementPower, area, expected) ->
            val request = original.copy(source = ThermalSource(Quantity(power, StandardUnits.WATT)),
                criticalElement = original.criticalElement.copy(power = Quantity(elementPower, StandardUnits.WATT)),
                elementWithRadiatorArea = Quantity(area, StandardUnits.SQUARE_METER))
            assertEquals(expected, success(request).value.finalResults.elementTemperature.kelvin, max(1e-12, abs(expected) * 1e-13))
        }
    }

    @Test
    fun `a mandatory geometry area lost to underflow is reported rather than silently set to zero`() {
        val original = input()
        val request = original.copy(heatedZone = original.heatedZone.copy(length = Quantity(1e-200, StandardUnits.METER),
            width = Quantity(1e-200, StandardUnits.METER), height = Quantity(1e-200, StandardUnits.METER)))
        val failure = assertIs<CalculationResult.Failure>(model.calculate(request))
        assertEquals("FINNED_HOUSING_CALCULATION_ERROR", failure.diagnostics.single().code)
        assertEquals("Исходные данные!V12", failure.diagnostics.single().details["cell"])
    }

    @Test
    fun `input emissivity changes reduced internal emissivity without replacing the fixed outer value`() {
        val original = input()
        val before = success(original).value
        val after = success(original.copy(enclosureEmissivity = 0.5)).value
        assertEquals(0.5786136015744587, after.internalHeatTransfer.reducedEmissivity, 1e-14)
        assertNotEquals(before.internalHeatTransfer.radiationCoefficient, after.internalHeatTransfer.radiationCoefficient)
        assertEquals(before.externalHeatTransfer, after.externalHeatTransfer)
        assertEquals(before.finalResults, after.finalResults)
    }
}

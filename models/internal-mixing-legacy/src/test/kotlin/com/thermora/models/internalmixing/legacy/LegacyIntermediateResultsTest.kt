package com.thermora.models.internalmixing.legacy

import com.thermora.models.internalmixing.legacy.output.*

import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.*

class LegacyIntermediateResultsTest {
    private fun close(expected: Double, actual: Double) {
        assertTrue(actual.isFinite())
        assertEquals(expected, actual, maxOf(1e-15, kotlin.math.abs(expected) * 2e-12))
    }
    private fun quantities(expected: List<Double>, actual: List<Quantity>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (e, a) -> close(e, a.baseValue) }
    }

    @Test fun `explicit trace preserves every old final field`() {
        val model = InternalMixingLegacyModel()
        val full = assertIs<CalculationResult.Success<*>>(model.calculate(IntermediateTestFixtures.input())).value as InternalMixingLegacyOutput
        val minimal = assertIs<CalculationResult.Success<*>>(model.calculate(IntermediateTestFixtures.input(false))).value as InternalMixingLegacyOutput
        assertEquals(minimal, full.copy(geometryResults = null, heatTransferResults = null))
    }
    @Test fun `calls are repeatable and independent of previous inputs`() {
        val model = InternalMixingLegacyModel()
        val first = model.calculate(IntermediateTestFixtures.input())
        model.calculate(IntermediateTestFixtures.input(false))
        assertEquals(first, model.calculate(IntermediateTestFixtures.input()))
    }
    @Test fun `trace warning has stable severity without invalidating input`() {
        val model = InternalMixingLegacyModel()
        val input = IntermediateTestFixtures.input()
        assertTrue(model.validate(input).isValid)
        val result = assertIs<CalculationResult.Success<*>>(model.calculate(input))
        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(result.diagnostics.distinct(), result.diagnostics)
    }

    private fun output(input: com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput = IntermediateTestFixtures.input()) =
        assertIs<CalculationResult.Success<InternalMixingLegacyOutput>>(InternalMixingLegacyModel().calculate(input)).value
    @Test fun `geometry and flow reproduce OOXML cached values`() {
        val g = assertNotNull(output().geometryResults)
        quantities(listOf(0.034398, 0.0164515, 0.007728, 0.000308, 0.00018975,
            0.000325578, 0.0908495, 0.014334828480068684, 0.0009674922725, 0.55494559658865628),
            listOf(g.enclosureArea,g.heatedZoneArea,g.zoneFaceAreaXY,g.zoneFaceAreaXZ,g.zoneFaceAreaYZ,
                g.freeAirVolume,g.wettedArea,g.hydraulicDiameter,g.fanVolumeFlow,g.airVelocity))
        assertEquals(PhysicalDimension.AREA, g.enclosureArea.unit.dimension)
        assertEquals(PhysicalDimension.VOLUME, g.freeAirVolume.unit.dimension)
        assertEquals(PhysicalDimension.LENGTH, g.hydraulicDiameter.unit.dimension)
    }
    @Test fun `all three physical probes reproduce independent Excel values`() {
        val probes = assertNotNull(output().heatTransferResults).probes
        assertEquals(3, probes.size)
        val expected = listOf(
            listOf(333.8726142430732,10.0,328.8726142430732,1000.0,1.0814118955283496,1.9226006004736993e-5,0.028548878393461364,0.72826789586885976,50.178268820937042,6.1920598191968326,56.370328640133877,1.2113756074869091,8.2550778950764592),
            listOf(343.8726142430732,20.0,333.8726142430732,1000.0,1.0650722699310058,1.978807301078374e-5,0.028918704530745611,0.72879225335798659,49.685346528343793,6.4831110375408576,56.168457565884651,1.2233935178640332,16.347969568220961),
            listOf(363.8726142430732,40.0,343.8726142430732,1000.0,1.0338247709249184,2.0935326237535689e-5,0.029655930695549386,0.72981890448668019,48.735222075044845,7.1008611785125613,55.836083253557405,1.2472443601878902,32.070700238704006))
        probes.zip(expected).forEach { (p,e) ->
            val actual = listOf(p.zoneTemperature.kelvin,p.zoneRiseAboveEnclosure.kelvin,p.meanTemperature.kelvin,
                p.specificHeatCapacity.baseValue,p.density.baseValue,p.kinematicViscosity.baseValue,p.thermalConductivity.baseValue,
                p.prandtlNumber,p.convectionCoefficient.baseValue,p.radiationCoefficient.baseValue,p.totalHeatTransferCoefficient.baseValue,
                p.convectionOnlyResistance.value.baseValue,p.dissipatedPower.baseValue)
            e.zip(actual).forEach { (x,y) -> close(x,y) }
            assertEquals(StandardUnits.WATT_PER_SQUARE_METER_KELVIN.dimension,p.convectionCoefficient.unit.dimension)
            assertEquals(StandardUnits.WATT.dimension,p.dissipatedPower.unit.dimension)
        }
    }
    @Test fun `C05 radiation changes totals but never convection only resistance or finals`() {
        val input = IntermediateTestFixtures.input()
        val a = output(input)
        val b = output(input.copy(heatTransfer = assertNotNull(input.heatTransfer).copy(reducedEmissivity = 0.5)))
        val pa = a.heatTransferResults!!.probes.first()
        val pb = b.heatTransferResults!!.probes.first()
        assertNotEquals(pa.radiationCoefficient,pb.radiationCoefficient)
        assertNotEquals(pa.totalHeatTransferCoefficient,pb.totalHeatTransferCoefficient)
        assertEquals(pa.convectionOnlyResistance,pb.convectionOnlyResistance)
        assertEquals(pa.dissipatedPower,pb.dissipatedPower)
        assertEquals(a.copy(heatTransferResults=null),b.copy(heatTransferResults=null))
    }
    @Test fun `invalid trace parameters produce validation failure`() {
        val input = IntermediateTestFixtures.input()
        val model = InternalMixingLegacyModel()
        for (config in listOf(assertNotNull(input.heatTransfer).copy(reducedEmissivity=Double.NaN),
            assertNotNull(input.heatTransfer).copy(zoneProbeRises=emptyList()), assertNotNull(input.heatTransfer).copy(zoneProbeRises=listOf(IntermediateTestFixtures.delta(0.0))))) {
            val bad = input.copy(heatTransfer=config)
            assertFalse(model.validate(bad).isValid)
            assertIs<CalculationResult.Failure>(model.calculate(bad))
        }
    }
    @Test fun `equivalent flow units preserve probe results`() {
        val input = IntermediateTestFixtures.input()
        val config = assertNotNull(input.heatTransfer)
        val fan = config.fan.copy(nominalVolumeFlowRate=config.fan.nominalVolumeFlowRate.convertTo(StandardUnits.CUBIC_METER_PER_SECOND))
        assertEquals(output(input),output(input.copy(heatTransfer=config.copy(fan=fan))))
    }
    @Test fun `nonpositive free volume gives diagnostic failure`() {
        val input = IntermediateTestFixtures.input()
        val enclosure = assertNotNull(input.heatTransfer).enclosure.copy(length=IntermediateTestFixtures.length(0.001))
        val result = InternalMixingLegacyModel().calculate(input.copy(heatTransfer=assertNotNull(input.heatTransfer).copy(enclosure=enclosure)))
        assertIs<CalculationResult.Failure>(result)
    }

    @Test fun `caller mutation of input collections cannot alter an existing result`() {
        val input=IntermediateTestFixtures.input()
        val original=assertNotNull(input.heatTransfer)
        val mutable=original.zoneProbeRises.toMutableList()
        val result=output(input.copy(heatTransfer=original.copy(zoneProbeRises=mutable)))
        mutable.clear()
        assertEquals(output(input),result)
    }
    @Test fun `old input constructor copy and destructuring remain available`() {
        val input=IntermediateTestFixtures.input(false)
        val old=com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput(input.sealedHousing,input.heatedZone,input.criticalElement,input.elementWithRadiatorArea)
        assertEquals(input,old.copy())
        val (first, second, third, fourth)=old
        assertEquals(input.component1(),first)
        assertEquals(input.component2(),second)
        assertEquals(input.component3(),third)
        assertEquals(input.component4(),fourth)
    }

    @Test fun `returned probe lists cannot be mutated through a mutable cast`() {
        val probes = output().heatTransferResults!!.probes
        assertFailsWith<UnsupportedOperationException> { (probes as MutableList).clear() }
    }

    @Test fun `all physical probe quantities carry the correct SI dimensions`() {
        output().heatTransferResults!!.probes.forEach { p ->
            assertEquals(StandardUnits.JOULE_PER_KILOGRAM_KELVIN.dimension,p.specificHeatCapacity.unit.dimension)
            assertEquals(StandardUnits.KILOGRAM_PER_CUBIC_METER.dimension,p.density.unit.dimension)
            assertEquals(StandardUnits.SQUARE_METER_PER_SECOND.dimension,p.kinematicViscosity.unit.dimension)
            assertEquals(StandardUnits.WATT_PER_METER_KELVIN.dimension,p.thermalConductivity.unit.dimension)
            listOf(p.convectionCoefficient,p.radiationCoefficient,p.totalHeatTransferCoefficient).forEach {
                assertEquals(StandardUnits.WATT_PER_SQUARE_METER_KELVIN.dimension,it.unit.dimension)
            }
            assertEquals(StandardUnits.KELVIN_PER_WATT.dimension,p.convectionOnlyResistance.value.unit.dimension)
        }
    }
}

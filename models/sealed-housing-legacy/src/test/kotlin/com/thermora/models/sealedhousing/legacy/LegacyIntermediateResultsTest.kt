package com.thermora.models.sealedhousing.legacy

import com.thermora.models.sealedhousing.legacy.output.*

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
        val model = SealedHousingLegacyModel()
        val full = assertIs<CalculationResult.Success<*>>(model.calculate(IntermediateTestFixtures.input())).value as SealedHousingLegacyOutput
        val minimal = assertIs<CalculationResult.Success<*>>(model.calculate(IntermediateTestFixtures.input(false))).value as SealedHousingLegacyOutput
        assertEquals(minimal, full.copy(geometryResults = null, externalHeatTransfer = null, internalHeatTransfer = null))
    }
    @Test fun `calls are repeatable and independent of previous inputs`() {
        val model = SealedHousingLegacyModel()
        val first = model.calculate(IntermediateTestFixtures.input())
        model.calculate(IntermediateTestFixtures.input(false))
        assertEquals(first, model.calculate(IntermediateTestFixtures.input()))
    }
    @Test fun `trace warning has stable severity without invalidating input`() {
        val model = SealedHousingLegacyModel()
        val input = IntermediateTestFixtures.input()
        assertTrue(model.validate(input).isValid)
        val result = assertIs<CalculationResult.Success<*>>(model.calculate(input))
        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(result.diagnostics.distinct(), result.diagnostics)
    }

    private fun output(input: com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput = IntermediateTestFixtures.input()) =
        assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(SealedHousingLegacyModel().calculate(input)).value
    @Test fun `geometry reproduces OOXML cached values`() {
        val g = assertNotNull(output().geometryResults)
        quantities(listOf(0.034398,0.0164515,0.007728,0.000308,0.00018975),
            listOf(g.enclosureArea,g.heatedZoneArea,g.zoneFaceAreaXY,g.zoneFaceAreaXZ,g.zoneFaceAreaYZ))
        assertEquals(PhysicalDimension.AREA,g.enclosureArea.unit.dimension)
    }
    @Test fun `invalid explicit configuration fails without successful partial trace`() {
        val input = IntermediateTestFixtures.input()
        val config = assertNotNull(input.heatTransfer)
        val bad = listOf(config.copy(enclosureEmissivity=Double.NaN),config.copy(enclosureEmissivity=Double.POSITIVE_INFINITY),config.copy(enclosureEmissivity=Double.NEGATIVE_INFINITY),config.copy(enclosureEmissivity=0.0),config.copy(enclosureEmissivity=1.1),config.copy(wallGaps=config.wallGaps.drop(1)),
            config.copy(enclosureProbeRises=emptyList()),config.copy(wallGaps=config.wallGaps.map { it.copy(wallIndex=1) }),config.copy(wallGaps=config.wallGaps.map { it.copy(gap=Quantity(0.0,StandardUnits.METER)) }),config.copy(zoneProbeResistances=emptyList()),
            config.copy(wallGaps=config.wallGaps.map { it.copy(gap=Quantity(1.0,StandardUnits.WATT)) }))
        bad.forEach {
            val i = input.copy(heatTransfer=it)
            assertFalse(SealedHousingLegacyModel().validate(i).isValid)
            assertIs<CalculationResult.Failure>(SealedHousingLegacyModel().calculate(i))
        }
    }
    @Test fun `manual resistance remains independent of computed reference resistance`() {
        val input = IntermediateTestFixtures.input()
        val a = output(input)
        val config = assertNotNull(input.heatTransfer)
        val b = output(input.copy(heatTransfer=config.copy(zoneProbeResistances=List(3) { IntermediateTestFixtures.resistance(10.0) })))
        assertEquals(assertNotNull(a.internalHeatTransfer).zoneToEnclosureResistance,assertNotNull(b.internalHeatTransfer).zoneToEnclosureResistance)
        assertNotEquals(assertNotNull(a.internalHeatTransfer).trials,assertNotNull(b.internalHeatTransfer).trials)
        assertEquals(a.copy(internalHeatTransfer=null),b.copy(internalHeatTransfer=null))
    }
    @Test fun `wall order does not alter results`() {
        val input = IntermediateTestFixtures.input()
        assertEquals(output(input),output(input.copy(heatTransfer=assertNotNull(input.heatTransfer).copy(wallGaps=assertNotNull(input.heatTransfer).wallGaps.reversed()))))
    }
    @Test fun `equivalent enclosure units preserve every intermediate value`() {
        val input = IntermediateTestFixtures.input()
        val c = assertNotNull(input.heatTransfer)
        val e = c.enclosure
        val unit = com.thermora.foundation.units.unit.MeasurementUnit("mm",PhysicalDimension.LENGTH,0.001)
        val converted = e.copy(length=e.length.convertTo(unit),width=e.width.convertTo(unit),height=e.height.convertTo(unit))
        val a=output(input)
        val b=output(input.copy(heatTransfer=c.copy(enclosure=converted)))
        close(a.geometryResults!!.enclosureArea.baseValue,b.geometryResults!!.enclosureArea.baseValue)
        close(assertNotNull(a.internalHeatTransfer).zoneToEnclosureResistance.value.baseValue,assertNotNull(b.internalHeatTransfer).zoneToEnclosureResistance.value.baseValue)
        assertEquals(a.enclosureTemperature,b.enclosureTemperature)
    }

    @Test fun `external convection and radiation reproduce Excel`() {
        val e = output().externalHeatTransfer!!
        close(318.0,e.meanTemperature.kelvin)
        close(1.3209679999999999,e.laminarAirFactor)
        close(1.5073400000000006,e.turbulentAirFactor)
        close(6.56559777384,e.radiationCoefficient.baseValue)
        quantities(listOf(3.1114776549359822,5.7784585020239678,5.28598766488836),e.laminarCoefficients)
        quantities(listOf(2.2714818140136064,4.218466226025269,3.2449740200194377),e.turbulentCoefficients)
        assertEquals(e.laminarCoefficients,e.selectedCoefficients)
        close(2.5523502985120694,e.enclosureToAmbientResistance.value.baseValue)
        quantities(listOf(3.9179575020833344,8.6595973728255906,19.631387047283333),e.trials.map { it.dissipatedPower })
    }
    @Test fun `internal six walls radiation and probes reproduce Excel`() {
        val i=output().internalHeatTransfer!!
        close(328.8726142430732,i.meanTemperature.kelvin)
        quantities(listOf(5.9342535682410595,5.4909968470815054,5.9342535682410595,5.4909968470815054,4.8496350462630593,7.0758510474648411),i.wallConvectionCoefficients)
        close(0.76737666355170309,i.reducedEmissivity)
        close(6.1920598191968326,i.radiationCoefficient.baseValue)
        close(5.0084308936934487,i.zoneToEnclosureResistance.value.baseValue)
        close(5.5,i.elementToSinkResistance.value.baseValue)
        quantities(listOf(1.996633319347956,4.4367812141866478,10.049455209936102),i.trials.map { it.dissipatedPower })
        i.trials.zip(listOf(10.0,20.0,40.0)).forEach { (p,r) -> close(r,p.zoneRise.kelvin);close(323.8726142430732+r,p.zoneTemperature.kelvin) }
    }
    @Test fun `C01 empty upper and side switch inputs retain laminar selection`() {
        val input=IntermediateTestFixtures.input()
        val e=output(input.copy(heatTransfer=assertNotNull(input.heatTransfer).copy(enclosureReferenceRise=IntermediateTestFixtures.delta(20000.0)))).externalHeatTransfer!!
        assertEquals(listOf(SealedHousingConvectionRegime.TURBULENT,SealedHousingConvectionRegime.LAMINAR,SealedHousingConvectionRegime.LAMINAR),e.regimes)
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
        val old=com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput(input.ambient,input.source,input.heatedZone,input.criticalElement,input.elementWithRadiatorArea)
        assertEquals(input,old.copy())
        val (first, second, third, fourth)=old
        assertEquals(input.component1(),first)
        assertEquals(input.component2(),second)
        assertEquals(input.component3(),third)
        assertEquals(input.component4(),fourth)
    }

    @Test fun `H01 wall three uses wall two gap in final ratio`() {
        val input=IntermediateTestFixtures.input()
        val c=assertNotNull(input.heatTransfer)
        val a=output(input).internalHeatTransfer!!.wallConvectionCoefficients
        val walls=c.wallGaps.map { if(it.wallIndex==2) it.copy(gap=IntermediateTestFixtures.length(0.006)) else it }
        val b=output(input.copy(heatTransfer=c.copy(wallGaps=walls))).internalHeatTransfer!!.wallConvectionCoefficients
        assertEquals(a[0],b[0])
        close(4.990092552740471,b[2].baseValue)
        assertNotEquals(a[3],b[3])
    }
    @Test fun `C02 zero element power is not masked by artificial finite threshold`() {
        val input=IntermediateTestFixtures.input()
        val bad=input.copy(criticalElement=input.criticalElement.copy(power=Quantity(0.0,StandardUnits.WATT)))
        assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(SealedHousingLegacyModel().calculate(bad.copy(heatTransfer=null)))
        val failure=assertIs<CalculationResult.Failure>(SealedHousingLegacyModel().calculate(bad))
        assertEquals("SEALED_LEGACY_CALCULATION_ERROR",failure.diagnostics.single().code)
        assertTrue(failure.diagnostics.single().message.contains("S13"))
    }

    @Test fun `returned probe lists cannot be mutated through a mutable cast`() {
        val probes = output().internalHeatTransfer!!.trials
        assertFailsWith<UnsupportedOperationException> { (probes as MutableList).clear() }
    }

    @Test fun `all transfer groups carry the correct dimensions and finite thresholds`() {
        val o=output()
        val e=o.externalHeatTransfer!!
        val i=o.internalHeatTransfer!!
        (e.laminarCoefficients+e.turbulentCoefficients+e.selectedCoefficients+i.wallConvectionCoefficients+
            listOf(e.radiationCoefficient,i.radiationCoefficient)).forEach {
            assertEquals(StandardUnits.WATT_PER_SQUARE_METER_KELVIN.dimension,it.unit.dimension)
        }
        (e.trials.map { it.dissipatedPower }+i.trials.map { it.dissipatedPower }).forEach {
            assertEquals(StandardUnits.WATT.dimension,it.unit.dimension)
        }
        assertTrue(e.lowerSwitchThreshold.kelvin.isFinite())
        assertTrue(e.upperSwitchThreshold.kelvin.isFinite())
        assertTrue(e.sideSwitchThresholdValue.isFinite())
    }
}

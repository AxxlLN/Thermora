package com.thermora.models.externalcooling.legacy

import com.thermora.models.externalcooling.legacy.output.*

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
        val model = ExternalCoolingLegacyModel()
        val full = assertIs<CalculationResult.Success<*>>(model.calculate(IntermediateTestFixtures.input())).value as ExternalCoolingLegacyOutput
        val minimal = assertIs<CalculationResult.Success<*>>(model.calculate(IntermediateTestFixtures.input(false))).value as ExternalCoolingLegacyOutput
        assertEquals(minimal, full.copy(geometryResults = null, externalHeatTransfer = null, internalHeatTransfer = null))
    }
    @Test fun `calls are repeatable and independent of previous inputs`() {
        val model = ExternalCoolingLegacyModel()
        val first = model.calculate(IntermediateTestFixtures.input())
        model.calculate(IntermediateTestFixtures.input(false))
        assertEquals(first, model.calculate(IntermediateTestFixtures.input()))
    }
    @Test fun `trace warning has stable severity without invalidating input`() {
        val model = ExternalCoolingLegacyModel()
        val input = IntermediateTestFixtures.input()
        assertTrue(model.validate(input).isValid)
        val result = assertIs<CalculationResult.Success<*>>(model.calculate(input))
        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(result.diagnostics.distinct(), result.diagnostics)
    }

    private fun output(input: com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput = IntermediateTestFixtures.input()) =
        assertIs<CalculationResult.Success<ExternalCoolingLegacyOutput>>(ExternalCoolingLegacyModel().calculate(input)).value
    @Test fun `geometry reproduces OOXML cached values`() {
        val g = assertNotNull(output().geometryResults)
        quantities(listOf(0.034398,0.0164515,0.007728,0.000308,0.00018975),
            listOf(g.enclosureArea,g.heatedZoneArea,g.zoneFaceAreaXY,g.zoneFaceAreaXZ,g.zoneFaceAreaYZ))
        assertEquals(PhysicalDimension.AREA,g.enclosureArea.unit.dimension)
    }
    @Test fun `invalid explicit configuration fails without successful partial trace`() {
        val input = IntermediateTestFixtures.input()
        val config = input.heatTransfer!!
        val bad = listOf(config.copy(enclosureEmissivity=Double.NaN),config.copy(enclosureEmissivity=Double.POSITIVE_INFINITY),config.copy(enclosureEmissivity=Double.NEGATIVE_INFINITY),config.copy(enclosureEmissivity=0.0),config.copy(enclosureEmissivity=1.1),config.copy(wallGaps=config.wallGaps.drop(1)),
            config.copy(enclosureProbeRises=emptyList()),config.copy(wallGaps=config.wallGaps.map { it.copy(wallIndex=1) }),config.copy(wallGaps=config.wallGaps.map { it.copy(gap=Quantity(0.0,StandardUnits.METER)) }),config.copy(zoneProbeResistances=emptyList()),
            config.copy(wallGaps=config.wallGaps.map { it.copy(gap=Quantity(1.0,StandardUnits.WATT)) }))
        bad.forEach {
            val i = input.copy(heatTransfer=it)
            assertFalse(ExternalCoolingLegacyModel().validate(i).isValid)
            assertIs<CalculationResult.Failure>(ExternalCoolingLegacyModel().calculate(i))
        }
    }
    @Test fun `manual resistance remains independent of computed reference resistance`() {
        val input = IntermediateTestFixtures.input()
        val a = output(input)
        val config = input.heatTransfer!!
        val b = output(input.copy(heatTransfer=config.copy(zoneProbeResistances=List(3) { IntermediateTestFixtures.resistance(10.0) })))
        assertEquals(a.internalHeatTransfer!!.zoneToEnclosureResistance,b.internalHeatTransfer!!.zoneToEnclosureResistance)
        assertNotEquals(a.internalHeatTransfer!!.trials,b.internalHeatTransfer!!.trials)
        assertEquals(a.copy(internalHeatTransfer=null),b.copy(internalHeatTransfer=null))
    }
    @Test fun `wall order does not alter results`() {
        val input = IntermediateTestFixtures.input()
        assertEquals(output(input),output(input.copy(heatTransfer=input.heatTransfer!!.copy(wallGaps=input.heatTransfer!!.wallGaps.reversed()))))
    }
    @Test fun `equivalent enclosure units preserve every intermediate value`() {
        val input = IntermediateTestFixtures.input()
        val c = input.heatTransfer!!
        val e = c.enclosure
        val unit = com.thermora.foundation.units.unit.MeasurementUnit("mm",PhysicalDimension.LENGTH,0.001)
        val converted = e.copy(length=e.length.convertTo(unit),width=e.width.convertTo(unit),height=e.height.convertTo(unit))
        val a=output(input)
        val b=output(input.copy(heatTransfer=c.copy(enclosure=converted)))
        close(a.geometryResults!!.enclosureArea.baseValue,b.geometryResults!!.enclosureArea.baseValue)
        close(a.internalHeatTransfer!!.zoneToEnclosureResistance.value.baseValue,b.internalHeatTransfer!!.zoneToEnclosureResistance.value.baseValue)
        assertEquals(a.enclosureTemperature,b.enclosureTemperature)
    }

    @Test fun `all external probes reproduce Excel transport properties and powers`() {
        val p=output().externalHeatTransfer!!.probes
        quantities(listOf(6.56559777384,6.88508333604,7.56453145644),p.map { it.radiationCoefficient })
        quantities(listOf(1.8030440030620731e-5,1.8575705264278131e-5,1.9689423823042719e-5),p.map { it.kinematicViscosity })
        quantities(listOf(0.027741807057028004,0.028113451352205502,0.028854220397222789),p.map { it.thermalConductivity })
        quantities(listOf(11.716422733413284,11.697820638609372,11.661550298214975),p.map { it.convectionCoefficient })
        quantities(listOf(6.2886494140849836,12.784294618399779,26.453550407864871),p.map { it.dissipatedPower })
        listOf(1.5901665590710987,1.5644195160533165,1.5120843661162267).zip(p).forEach { (e,v) -> close(e,v.enclosureToAmbientResistance.value.baseValue) }
        p.zip(listOf(10.0,20.0,40.0)).forEach { (v,r) -> close(r,v.enclosureRise.kelvin);close(313+r,v.enclosureTemperature.kelvin);close(313+r/2,v.meanTemperature.kelvin) }
    }
    @Test fun `all six internal walls C08 and manual trials reproduce Excel`() {
        val i=output().internalHeatTransfer!!
        quantities(listOf(7.0423277741470649,7.4701656337065376,7.0423277741470649,7.4701656337065376,5.7551837291810148,8.3970901789522365),i.wallConvectionCoefficients)
        close(0.78433667633210069,i.reducedEmissivity)
        close(6.3956894656864351,i.radiationCoefficient.baseValue)
        close(4.5073135876550134,i.zoneToEnclosureResistance.value.baseValue)
        quantities(listOf(1.9948532854486738,4.4372328685489242,10.058477227846447),i.trials.map { it.dissipatedPower })
    }
    @Test fun `airflow changes physical branch while final correlation stays independent`() {
        val input=IntermediateTestFixtures.input()
        val c=input.heatTransfer!!
        val a=output(input)
        val b=output(input.copy(heatTransfer=c.copy(airflow=c.airflow.copy(velocity=Quantity(2.0,StandardUnits.METER_PER_SECOND)))))
        assertNotEquals(a.externalHeatTransfer,b.externalHeatTransfer)
        assertEquals(a.copy(externalHeatTransfer=null),b.copy(externalHeatTransfer=null))
    }

    @Test fun `caller mutation of input collections cannot alter an existing result`() {
        val input=IntermediateTestFixtures.input()
        val original=input.heatTransfer!!
        val mutable=original.zoneProbeRises.toMutableList()
        val result=output(input.copy(heatTransfer=original.copy(zoneProbeRises=mutable)))
        mutable.clear()
        assertEquals(output(input),result)
    }
    @Test fun `old input constructor copy and destructuring remain available`() {
        val input=IntermediateTestFixtures.input(false)
        val old=com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput(input.ambient,input.source,input.heatedZone,input.criticalElement,input.elementWithRadiatorArea)
        assertEquals(input,old.copy())
        val (first, second, third, fourth)=old
        assertEquals(input.component1(),first)
        assertEquals(input.component2(),second)
        assertEquals(input.component3(),third)
        assertEquals(input.component4(),fourth)
    }

    @Test fun `zero airflow retains finite radiation only probes`() {
        val input=IntermediateTestFixtures.input()
        val c=input.heatTransfer!!
        val result=output(input.copy(heatTransfer=c.copy(airflow=c.airflow.copy(velocity=Quantity(0.0,StandardUnits.METER_PER_SECOND)))))
        result.externalHeatTransfer!!.probes.forEach {
            assertEquals(0.0,it.convectionCoefficient.baseValue)
            assertTrue(it.enclosureToAmbientResistance.value.baseValue.isFinite())
        }
    }
    @Test fun `overflow of explicit probe temperature returns numerical diagnostic`() {
        val input=IntermediateTestFixtures.input()
        val bad=input.copy(heatTransfer=input.heatTransfer!!.copy(enclosureProbeRises=List(3) { IntermediateTestFixtures.delta(Double.MAX_VALUE) }))
        val failure=assertIs<CalculationResult.Failure>(ExternalCoolingLegacyModel().calculate(bad))
        assertEquals("EXTERNAL_COOLING_CALCULATION_ERROR",failure.diagnostics.single().code)
        assertEquals(DiagnosticSeverity.ERROR,failure.diagnostics.single().severity)
    }

    @Test fun `returned probe lists cannot be mutated through a mutable cast`() {
        val probes = output().internalHeatTransfer!!.trials
        assertFailsWith<UnsupportedOperationException> { (probes as MutableList).clear() }
    }

    @Test fun `all transport properties carry correct dimensions`() {
        val o=output()
        o.externalHeatTransfer!!.probes.forEach { p ->
            assertEquals(StandardUnits.SQUARE_METER_PER_SECOND.dimension,p.kinematicViscosity.unit.dimension)
            assertEquals(StandardUnits.WATT_PER_METER_KELVIN.dimension,p.thermalConductivity.unit.dimension)
            listOf(p.convectionCoefficient,p.radiationCoefficient).forEach {
                assertEquals(StandardUnits.WATT_PER_SQUARE_METER_KELVIN.dimension,it.unit.dimension)
            }
            assertEquals(StandardUnits.WATT.dimension,p.dissipatedPower.unit.dimension)
        }
        o.internalHeatTransfer!!.wallConvectionCoefficients.forEach {
            assertEquals(StandardUnits.WATT_PER_SQUARE_METER_KELVIN.dimension,it.unit.dimension)
        }
        assertEquals(PhysicalDimension.LENGTH,o.geometryResults!!.flowLength.unit.dimension)
    }
}

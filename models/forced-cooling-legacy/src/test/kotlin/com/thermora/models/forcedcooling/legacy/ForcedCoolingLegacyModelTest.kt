package com.thermora.models.forcedcooling.legacy
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.forcedcooling.legacy.output.*
import kotlin.test.*
class ForcedCoolingLegacyModelTest {
 private val model=ForcedCoolingLegacyModel()
 private fun result(i:com.thermora.models.forcedcooling.legacy.input.ForcedCoolingLegacyInput=excelInput())=assertIs<CalculationResult.Success<ForcedCoolingLegacyOutput>>(model.calculate(i)).value
 private fun close(e:Double,a:Double)=assertTrue(kotlin.math.abs(e-a)<=1e-10+1e-12*kotlin.math.abs(e), "$e != $a")
 @Test fun `Excel cached geometry`() { val o=result()
  close(0.034398,o.geometry.enclosureArea.baseValue)
  close(0.0164515,o.geometry.heatedZoneArea.baseValue)
  close(0.000325578,o.geometry.freeAirVolume.baseValue)
 }
 @Test fun `Excel cached heat transfer`() { val o=result()
  close(0.35956534216838976,o.heatTransfer.airVelocity.baseValue)
  close(29.721197295235918,o.heatTransfer.convectionCoefficient.baseValue)
  close(6.024049937750439,o.heatTransfer.radiationCoefficient.baseValue)
  close(7.530062422188048,o.heatTransfer.radiationFunction.baseValue)
  close(314.1,o.heatTransfer.convectionMeanTemperature.kelvin)
  close(321.365,o.heatTransfer.densityMeanTemperature.kelvin)
 }
 @Test fun `Excel cached balance`() { val o=result()
  close(0.5639399999999953,o.airBalance.enclosurePower.baseValue)
  close(3.7360600000000046,o.airBalance.initialAirPower.baseValue)
  close(1.744318561370702,o.airBalance.airRise.kelvin)
  close(9.133255859400412,o.airBalance.calculatedZoneRise.kelvin)
  close(3.0900784311753506,o.airBalance.calculatedEnclosureRise.kelvin)
  close(6.71753057275574,o.airBalance.calculatedAirPower.baseValue)
 }
 @Test fun `Excel cached convergence`() { val o=result()
  close(-0.8900784311753611,o.convergence.enclosureDifference.kelvin)
  close(5.396744140599559,o.convergence.zoneDifference.kelvin)
  close(-2.981470572755735,o.convergence.airPowerDifference.baseValue)
  close(-2.109190595202278,o.convergence.enclosurePercent!!)
  close(9.896835027690376,o.convergence.zonePercent!!)
  close(-79.8025345619645,o.convergence.airPowerPercent!!)
 }
 @Test fun `Excel cached next`() { val o=result()
  close(315.28900784311753,o.nextStep.enclosureTemperature!!.kelvin)
  close(326.31406333810264,o.nextStep.zoneTemperature!!.kelvin)
 }
 @Test fun `Excel cached final`() { val o=result()
  close(316.09007843117536,o.finalResults.enclosureTemperature.kelvin)
  close(322.13325585940044,o.finalResults.heatedZoneTemperature.kelvin)
  close(314.7443185613707,o.finalResults.internalAirTemperature.kelvin)
  close(322.43144305546707,o.finalResults.elementTemperature.kelvin)
  close(329.30644305546707,o.finalResults.junctionTemperature.kelvin)
 }
 @Test fun `nonconverged manual state is successful with signed diagnostics`() {
  val s=assertIs<CalculationResult.Success<ForcedCoolingLegacyOutput>>(model.calculate(excelInput()))
  assertEquals(false, s.value.convergence.withinThreePercent);assertEquals(false, s.value.convergence.withinOnePercent)
  assertTrue(s.diagnostics.any { it.code=="FORCED_COOLING_NOT_CONVERGED" })
 }
 @Test fun `zero source wrong dimension and zero flow fail validation`() {
  val i=excelInput()
  for(bad in listOf(i.copy(source=i.source.copy(power=Quantity(0.0,StandardUnits.WATT))),i.copy(fanVolumeFlow=Quantity(0.0,StandardUnits.CUBIC_METER_PER_SECOND)),i.copy(componentLength=Quantity(1.0,StandardUnits.WATT)))) {
   assertFalse(model.validate(bad).isValid);assertIs<CalculationResult.Failure>(model.calculate(bad))
  }
 }
 @Test fun `radiation singular trial fails`() {
  assertIs<CalculationResult.Failure>(model.calculate(excelInput().copy(trialZoneTemperature=excelInput().trialEnclosureTemperature)))
 }
 @Test fun `equivalent units preserve result and input`() {
  val i=excelInput();val mm=i.copy(componentLength=i.componentLength.convertTo(StandardUnits.MILLIMETER))
  assertEquals(result(i),result(mm));assertEquals(i,excelInput());assertEquals(result(),result())
 }
 @Test fun `overflow is a numerical diagnostic failure`() {
  val f=assertIs<CalculationResult.Failure>(model.calculate(excelInput().copy(trialZoneTemperature=Temperature.fromKelvin(Double.MAX_VALUE))))
  assertTrue(f.diagnostics.any { it.details.containsKey("cell") })
 }
 @Test fun `legacy Celsius convergence at zero is nullable not an error`() {
  val i=excelInput().copy(ambient=excelInput().ambient.copy(temperature=Temperature.fromKelvin(265.0)),trialEnclosureTemperature=Temperature.fromKelvin(273.0),trialZoneTemperature=Temperature.fromKelvin(289.2))
  val passingOtherPercentages = i.copy(
   source = i.source.copy(power = Quantity(22.75378224362966, StandardUnits.WATT)),
   trialZoneTemperature = Temperature.fromKelvin(308.8036973956844)
  )
  for (input in listOf(i, passingOtherPercentages)) {
   val success = assertIs<CalculationResult.Success<ForcedCoolingLegacyOutput>>(model.calculate(input))
   val convergence = success.value.convergence
   if (input == passingOtherPercentages) {
    assertTrue(kotlin.math.abs(assertNotNull(convergence.zonePercent)) < 1.0)
    assertTrue(kotlin.math.abs(assertNotNull(convergence.airPowerPercent)) < 1.0)
   }
   assertNull(convergence.enclosurePercent)
   assertNull(convergence.withinThreePercent)
   assertNull(convergence.withinOnePercent)
   assertTrue(success.diagnostics.any { it.source == "P4" })
   assertTrue(success.diagnostics.any { it.code == "FORCED_COOLING_CONVERGENCE_UNDEFINED" && it.source == "P4:P6" })
   assertFalse(success.diagnostics.any { it.code == "FORCED_COOLING_NOT_CONVERGED" })
  }
 }
 @Test fun `independent fluid-property decomposition and dimensions`() {
  val o=result()
  close(1.76105083598219e-05,o.heatTransfer.kinematicViscosity.baseValue)
  close(1.1069054850052462,o.heatTransfer.airDensity.baseValue)
  assertEquals(com.thermora.foundation.units.dimension.PhysicalDimension.HEAT_TRANSFER_COEFFICIENT,o.heatTransfer.convectionCoefficient.unit.dimension)
 }
 @Test fun `original CFM conversion is equivalent without changing input unit`() {
  val i=excelInput();val cfm=com.thermora.foundation.units.unit.MeasurementUnit("CFM",com.thermora.foundation.units.dimension.PhysicalDimension.VOLUMETRIC_FLOW,0.00047194745)
  val request=i.copy(fanVolumeFlow=Quantity(2.05,cfm))
  assertEquals(result(i),result(request));assertEquals(2.05,request.fanVolumeFlow.value);assertEquals(cfm,request.fanVolumeFlow.unit)
 }


 @Test fun `G13 detects overflow of the denominator`() {
  val input = excelInput().copy(fanVolumeFlow = Quantity(1e305, StandardUnits.CUBIC_METER_PER_SECOND))
  assertTrue(model.validate(input).isValid)
  val diagnostic = assertIs<CalculationResult.Failure>(model.calculate(input)).diagnostics.single()
  assertEquals(mapOf("sheet" to "Продуваемый корпус", "cell" to "G13"), diagnostic.details)
 }

 @Test fun `area overflow identifies the source sheet and cell`() {
  val input = excelInput()
  val huge = Quantity(1e200, StandardUnits.METER)
  val changed = input.copy(enclosure = input.enclosure.copy(length = huge, width = huge))
  val diagnostic = assertIs<CalculationResult.Failure>(model.calculate(changed)).diagnostics.single()
  assertEquals(mapOf("sheet" to "Исходные данные", "cell" to "V11"), diagnostic.details)
 }

 @Test fun `equal trials identify radiation cell J8`() {
  val input = excelInput()
  val diagnostic = model.validate(input.copy(trialZoneTemperature = input.trialEnclosureTemperature)).diagnostics.single()
  assertEquals("trialZoneTemperature", diagnostic.source)
  assertEquals(mapOf("sheet" to "Продуваемый корпус", "cell" to "J8"), diagnostic.details)
 }
}

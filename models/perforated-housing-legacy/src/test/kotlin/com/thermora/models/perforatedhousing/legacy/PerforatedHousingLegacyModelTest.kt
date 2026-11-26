package com.thermora.models.perforatedhousing.legacy

import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits.METER
import com.thermora.foundation.units.unit.StandardUnits.MILLIMETER
import com.thermora.foundation.units.unit.StandardUnits.WATT
import com.thermora.foundation.units.unit.StandardUnits.SQUARE_METER
import com.thermora.foundation.units.unit.StandardUnits.KELVIN_PER_WATT
import com.thermora.foundation.units.unit.StandardUnits.WATT_PER_SQUARE_METER_KELVIN
import com.thermora.models.perforatedhousing.legacy.output.*
import kotlin.test.*

class PerforatedHousingLegacyModelTest {
 private val model = PerforatedHousingLegacyModel()
 private fun result(input: com.thermora.models.perforatedhousing.legacy.input.PerforatedHousingLegacyInput = excelInput()) = assertIs<CalculationResult.Success<PerforatedHousingLegacyOutput>>(model.calculate(input)).value
 private fun close(expected: Double, actual: Double) = assertTrue(kotlin.math.abs(expected-actual) <= 1e-10 + 1e-12*kotlin.math.abs(expected), "$expected != $actual")
 @Test fun `Excel cached geometry cells`() { val o=result()
  close(0.034398, o.geometry.enclosureArea.baseValue)
  close(0.0164515, o.geometry.heatedZoneArea.baseValue)
  close(0.000325578, o.geometry.freeAirVolume.baseValue)
 }
 @Test fun `Excel cached quadratic cells`() { val o=result()
  close(101.38791730312825, o.quadratic.legacyD)
  close(0.0508495, o.quadratic.a)
  close(5.404410699999999, o.quadratic.delta)
  close(141.96320226282694, o.quadratic.b)
  close(0.3326236004200034, o.quadratic.discriminant)
  close(320.47023476207494, o.quadratic.lowerRootTemperature.kelvin)
  close(331.8122399918558, o.quadratic.upperRootTemperature.kelvin)
  close(326.9176825410533, o.quadratic.secondAirTemperature.kelvin)
 }
 @Test fun `Excel cached radiation cells`() { val o=result()
  close(8.081535979692006, o.heatTransfer.radiationFunction.baseValue)
  close(1.7230759236419486, o.heatTransfer.radiationPower.baseValue)
 }
 @Test fun `Excel cached air cells`() { val o=result()
  close(327.9404695241499, o.airBalance.heatedZoneAirTemperature.kelvin)
  close(325.89489555795683, o.airBalance.outletAirTemperature.kelvin)
  close(9.878259227739703e-05, o.airBalance.massFlow.baseValue)
  close(0.0003694217120020382, o.airBalance.airMass.baseValue)
  close(0.2673979061546118, o.airBalance.exchangeRate.baseValue)
  close(1.2737912103612647, o.airBalance.airPower.baseValue)
  close(3.02447499999999, o.airBalance.enclosurePower.baseValue)
  close(4.298266210361255, o.airBalance.calculatedTotalPower.baseValue)
  close(319.66762420606693, o.airBalance.calculatedEnclosureTemperature.kelvin)
  close(11.945132762056788, o.airBalance.calculatedConvectionCoefficient.baseValue)
 }
 @Test fun `Excel cached convergence cells`() { val o=result()
  close(1.2323757939330378, o.convergence.enclosureDifference.kelvin)
  close(0.0017337896387452645, o.convergence.totalPowerDifference.baseValue)
  close(0.0017337896387450424, o.convergence.airPowerDifference.baseValue)
  close(2.5728095906744017, o.convergence.enclosurePercent!!)
  close(0.040320689273145686, o.convergence.totalPowerPercent!!)
  close(0.13592753091825163, o.convergence.airPowerPercent!!)
 }
 @Test fun `Excel cached next cells`() { val o=result()
  close(321.02323757939325, o.nextStep.enclosureTemperature!!.kelvin)
  close(337.10867244498337, o.nextStep.zoneTemperature!!.kelvin)
  close(11.945132762056788, o.nextStep.convectionCoefficient.baseValue)
 }
 @Test fun `Excel cached final cells`() { val o=result()
  close(319.66762420606693, o.finalResults.enclosureTemperature.kelvin)
  close(337.1, o.finalResults.heatedZoneTemperature.kelvin)
  close(323.6939586515641, o.finalResults.internalAirTemperature.kelvin)
  close(337.88682909313326, o.finalResults.elementTemperature.kelvin)
  close(344.76182909313326, o.finalResults.junctionTemperature.kelvin)
 }
 @Test fun `passes 3 percent but not 1 percent and preserves trial zone`() {
  val o=result();assertEquals(true, o.convergence.withinThreePercent);assertEquals(false, o.convergence.withinOnePercent)
  assertEquals(excelInput().trialZoneTemperature,o.finalResults.heatedZoneTemperature)
 }
 @Test fun `equivalent units and repeatability`() {
  val i=excelInput(); val mm=i.copy(enclosure=i.enclosure.copy(length=i.enclosure.length.convertTo(MILLIMETER)))
  assertEquals(result(i),result(mm));assertEquals(result(),result());assertEquals(i,excelInput())
 }
 @Test fun `zero source negative coefficient and wrong dimensions fail validation`() {
  val i=excelInput()
  for(bad in listOf(i.copy(source=i.source.copy(power=Quantity(0.0,WATT))), i.copy(convectionCoefficient=Quantity(-1.0,WATT_PER_SQUARE_METER_KELVIN)),i.copy(elementWithRadiatorArea=Quantity(1.0,METER)))) {
   assertFalse(model.validate(bad).isValid);assertIs<CalculationResult.Failure>(model.calculate(bad))
  }
 }
 @Test fun `singular radiation trial is rejected`() {
  assertIs<CalculationResult.Failure>(model.calculate(excelInput().copy(trialZoneTemperature=excelInput().trialEnclosureTemperature)))
 }
 @Test fun `unrepresentable arithmetic produces diagnostic failure not exception`() {
  val i=excelInput().copy(trialZoneTemperature=Temperature.fromKelvin(Double.MAX_VALUE))
  val f=assertIs<CalculationResult.Failure>(model.calculate(i));assertTrue(f.diagnostics.any { it.details["cell"]=="J9" })
 }
 @Test fun `undefined auxiliary convergence retains successful physical result`() {
  // H3=0 legacy Celsius, with valid positive Kelvin and a positive enclosure rise.
  val i=excelInput().copy(ambient=excelInput().ambient.copy(temperature=Temperature.fromKelvin(265.0)),trialEnclosureTemperature=Temperature.fromKelvin(273.0),trialZoneTemperature=Temperature.fromKelvin(289.2))
  val success=assertIs<CalculationResult.Success<PerforatedHousingLegacyOutput>>(model.calculate(i))
  assertNull(success.value.convergence.enclosurePercent)
  assertTrue(success.diagnostics.any { it.source=="P4" })
  assertNull(success.value.convergence.withinThreePercent)
  assertNull(success.value.convergence.withinOnePercent)
  assertTrue(success.diagnostics.any { it.code.endsWith("_CONVERGENCE_UNDEFINED") && it.source == "P4:P6" })
  assertFalse(success.diagnostics.any { it.code.endsWith("_NOT_CONVERGED") })
 }
 @Test fun `legacy warning remains explicit`() {
  val s=assertIs<CalculationResult.Success<*>>(model.calculate(excelInput()))
  assertTrue(s.diagnostics.any { it.code=="PERFORATED_HOUSING_LEGACY_CORRELATIONS" })
 }
 @Test fun `negative discriminant is a physical calculation failure`() {
  val f=assertIs<CalculationResult.Failure>(model.calculate(excelInput().copy(convectionCoefficient=Quantity(0.01,WATT_PER_SQUARE_METER_KELVIN))))
  assertTrue(f.diagnostics.any { it.details["cell"]=="I12" })
 }
 @Test fun `undefined N15 does not discard physical output`() {
  val i=excelInput().copy(source=excelInput().source.copy(power=Quantity(0.2,WATT)),firstIterationPower=Quantity(0.0,WATT))
  val s=assertIs<CalculationResult.Success<PerforatedHousingLegacyOutput>>(model.calculate(i))
  assertNull(s.value.nextStep.zoneTemperature)
  assertTrue(s.diagnostics.any { it.source=="N15" })
 }

 @Test fun `air balance has mass flow rather than volume flow dimension`() {
  val o=result();val d=com.thermora.foundation.units.dimension.PhysicalDimension
  assertEquals(d.MASS / d.TIME,o.airBalance.massFlow.unit.dimension)
  assertEquals(d.DIMENSIONLESS / d.TIME,o.airBalance.exchangeRate.unit.dimension)
 }


 @Test fun `Excel I12 zero gives one repeated root`() {
  val input = excelInput().copy(convectionCoefficient = Quantity(6.39686803084314, WATT_PER_SQUARE_METER_KELVIN))
  val output = result(input)
  assertEquals(0.0, output.quadratic.discriminant)
  close(326.14123737696536, output.quadratic.lowerRootTemperature.kelvin)
  assertEquals(output.quadratic.lowerRootTemperature, output.quadratic.upperRootTemperature)
  close(346.86033186163456, output.finalResults.enclosureTemperature.kelvin)
 }

 @Test fun `small positive Excel I12 gives two roots and selects the lower one`() {
  val input = excelInput().copy(convectionCoefficient = Quantity(6.3968680308495385, WATT_PER_SQUARE_METER_KELVIN))
  val output = result(input)
  assertEquals(7.105427357601002e-13, output.quadratic.discriminant)
  close(326.1412290884181, output.quadratic.lowerRootTemperature.kelvin)
  close(326.14124566551264, output.quadratic.upperRootTemperature.kelvin)
  close(346.8603033103403, output.finalResults.enclosureTemperature.kelvin)
  close(99.99971753559167, assertNotNull(output.convergence.airPowerPercent))
 }

 @Test fun `small negative Excel I12 has no real roots`() {
  val input = excelInput().copy(convectionCoefficient = Quantity(6.396868030843, WATT_PER_SQUARE_METER_KELVIN))
  val failure = assertIs<CalculationResult.Failure>(model.calculate(input))
  assertEquals("I12", failure.diagnostics.single().details["cell"])
 }

 @Test fun `C16 detects underflow of squared opening area`() {
  assertInvalidOpeningArithmetic(1e-200)
 }

 @Test fun `C16 detects overflow of opening area quotient`() {
  assertInvalidOpeningArithmetic(1e-153)
 }

 private fun assertInvalidOpeningArithmetic(area: Double) {
  val input = excelInput()
  val changed = input.copy(perforation = input.perforation.copy(openArea = Quantity(area, SQUARE_METER)))
  assertTrue(model.validate(changed).isValid)
  val diagnostic = assertIs<CalculationResult.Failure>(model.calculate(changed)).diagnostics.single()
  assertEquals(mapOf("sheet" to "Перфорированный корпус", "cell" to "C16"), diagnostic.details)
 }

 @Test fun `area overflow identifies the source sheet and cell`() {
  val input = excelInput()
  val huge = Quantity(1e200, METER)
  val changed = input.copy(enclosure = input.enclosure.copy(length = huge, width = huge))
  val diagnostic = assertIs<CalculationResult.Failure>(model.calculate(changed)).diagnostics.single()
  assertEquals(mapOf("sheet" to "Исходные данные", "cell" to "V11"), diagnostic.details)
 }
}

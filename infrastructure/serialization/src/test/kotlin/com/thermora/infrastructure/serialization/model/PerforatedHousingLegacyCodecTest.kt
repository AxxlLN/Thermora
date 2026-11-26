package com.thermora.infrastructure.serialization.model

import com.thermora.infrastructure.serialization.perforatedhousing.legacy.*
import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.temperature.Temperature
import kotlin.test.*

class PerforatedHousingLegacyCodecTest {
 private val input=PerforatedHousingLegacyInputCodec()
 private val output=PerforatedHousingLegacyOutputCodec()
 @Test fun `Input Minimal independent state`() = NewModelContractSupport.fixture(input,"PerforatedHousingLegacyInput","Minimal",PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputMinimal())
 @Test fun `Input Full independent state`() = NewModelContractSupport.fixture(input,"PerforatedHousingLegacyInput","Full",PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputFull())
 @Test fun `Input strict envelope and trailing`() = NewModelContractSupport.invalid(input,"PerforatedHousingLegacyInput")
 @Test fun `Output Minimal independent state`() = NewModelContractSupport.fixture(output,"PerforatedHousingLegacyOutput","Minimal",PerforatedHousingLegacyFixtures.PerforatedHousingLegacyOutputMinimal())
 @Test fun `Output Full independent state`() = NewModelContractSupport.fixture(output,"PerforatedHousingLegacyOutput","Full",PerforatedHousingLegacyFixtures.PerforatedHousingLegacyOutputFull())
 @Test fun `Output strict envelope and trailing`() = NewModelContractSupport.invalid(output,"PerforatedHousingLegacyOutput")
 @Test fun `negative corpus covers nested required fields`() = NewModelContractSupport.corpus("perforated-housing",listOf(input,output),549,74)
 @Test fun `public schema identifiers`() {
  assertEquals("thermora.perforated-housing.legacy.input",input.schema.id)
  assertEquals("thermora.perforated-housing.legacy.output",output.schema.id)
 }
 @Test fun `real full trace success and failure survive history`() {
  val value=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputFull()
  NewModelContractSupport.history("perforated-housing-legacy",value,value.copy(source=value.source.copy(power=Quantity(0.0,StandardUnits.WATT))),input,output)
 }
 @Test fun `undefined convergence and warning survive history`() {
  val value=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputFull()
  val trial = value.copy(
   ambient = value.ambient.copy(temperature = Temperature.fromKelvin(265.0)),
   trialEnclosureTemperature = Temperature.fromKelvin(273.0),
   trialZoneTemperature = Temperature.fromKelvin(289.2)
  )
  NewModelContractSupport.history("perforated-housing-legacy",trial,trial.copy(source=value.source.copy(power=Quantity(0.0,StandardUnits.WATT))),input,output) { restored, diagnostics ->
   assertNull(restored.convergence.withinThreePercent)
   assertNull(restored.convergence.withinOnePercent)
   assertTrue(diagnostics.any { it.code.endsWith("_CONVERGENCE_UNDEFINED") })
   assertFalse(diagnostics.any { it.code.endsWith("_NOT_CONVERGED") })
  }
 }
 @Test fun `model-invalid input is persisted without physical validation`() {
  val value=PerforatedHousingLegacyFixtures.PerforatedHousingLegacyInputFull().copy(elementWithRadiatorArea=Quantity(-0.0,StandardUnits.METER))
  val encoded=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(input.encode(value)).value
  val restored=assertIs<SnapshotCodecResult.Success<*>>(input.decode(encoded)).value
  assertEquals(value,restored)
 }
}

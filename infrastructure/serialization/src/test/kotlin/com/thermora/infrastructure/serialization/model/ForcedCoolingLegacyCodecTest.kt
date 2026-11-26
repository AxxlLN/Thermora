package com.thermora.infrastructure.serialization.model

import com.thermora.infrastructure.serialization.forcedcooling.legacy.*
import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.foundation.units.temperature.Temperature
import kotlin.test.*

class ForcedCoolingLegacyCodecTest {
 private val input=ForcedCoolingLegacyInputCodec()
 private val output=ForcedCoolingLegacyOutputCodec()
 @Test fun `Input Minimal independent state`() = NewModelContractSupport.fixture(input,"ForcedCoolingLegacyInput","Minimal",ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputMinimal())
 @Test fun `Input Full independent state`() = NewModelContractSupport.fixture(input,"ForcedCoolingLegacyInput","Full",ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull())
 @Test fun `Input strict envelope and trailing`() = NewModelContractSupport.invalid(input,"ForcedCoolingLegacyInput")
 @Test fun `Output Minimal independent state`() = NewModelContractSupport.fixture(output,"ForcedCoolingLegacyOutput","Minimal",ForcedCoolingLegacyFixtures.ForcedCoolingLegacyOutputMinimal())
 @Test fun `Output Full independent state`() = NewModelContractSupport.fixture(output,"ForcedCoolingLegacyOutput","Full",ForcedCoolingLegacyFixtures.ForcedCoolingLegacyOutputFull())
 @Test fun `Output strict envelope and trailing`() = NewModelContractSupport.invalid(output,"ForcedCoolingLegacyOutput")
 @Test fun `negative corpus covers nested required fields`() = NewModelContractSupport.corpus("forced-cooling",listOf(input,output),443,47)
 @Test fun `public schema identifiers`() {
  assertEquals("thermora.forced-cooling.legacy.input",input.schema.id)
  assertEquals("thermora.forced-cooling.legacy.output",output.schema.id)
 }
 @Test fun `real full trace success and failure survive history`() {
  val value=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull()
  NewModelContractSupport.history("forced-cooling-legacy",value,value.copy(source=value.source.copy(power=Quantity(0.0,StandardUnits.WATT))),input,output)
 }
 @Test fun `undefined convergence and warning survive history`() {
  val value=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull()
  val trial = value.copy(
   ambient = value.ambient.copy(temperature = Temperature.fromKelvin(265.0)),
   trialEnclosureTemperature = Temperature.fromKelvin(273.0),
   trialZoneTemperature = Temperature.fromKelvin(289.2)
  )
  NewModelContractSupport.history("forced-cooling-legacy",trial,trial.copy(source=value.source.copy(power=Quantity(0.0,StandardUnits.WATT))),input,output) { restored, diagnostics ->
   assertNull(restored.convergence.withinThreePercent)
   assertNull(restored.convergence.withinOnePercent)
   assertTrue(diagnostics.any { it.code.endsWith("_CONVERGENCE_UNDEFINED") })
   assertFalse(diagnostics.any { it.code.endsWith("_NOT_CONVERGED") })
  }
 }
 @Test fun `model-invalid input is persisted without physical validation`() {
  val value=ForcedCoolingLegacyFixtures.ForcedCoolingLegacyInputFull().copy(elementWithRadiatorArea=Quantity(-0.0,StandardUnits.METER))
  val encoded=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(input.encode(value)).value
  val restored=assertIs<SnapshotCodecResult.Success<*>>(input.decode(encoded)).value
  assertEquals(value,restored)
 }
}

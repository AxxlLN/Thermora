package com.thermora.application.core

import com.thermora.calculation.engine.registry.CalculationModelRegistry
import com.thermora.calculation.engine.execution.CalculationEngine
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import kotlin.test.*
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import com.thermora.models.internalmixing.legacy.InternalMixingLegacyModel
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput
import com.thermora.models.externalcooling.legacy.ExternalCoolingLegacyModel
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput

class LegacyIntermediateIntegrationTest {

    @Test fun `SealedHousing intermediate output survives Engine and Core`() {
        val model=SealedHousingLegacyModel()
        val input=SealedHousingIntermediateFixtures.input()
        val registry=CalculationModelRegistry()
        assertIs<CalculationResult.Success<Unit>>(registry.register(model))
        val direct=model.calculate(input)
        val engine=CalculationEngine(registry).calculate(model.descriptor.id,input,SealedHousingLegacyOutput::class)
        val core=ThermoraCoreFactory.createDefault()
        val facade=core.calculate(model.descriptor.id,input,SealedHousingLegacyOutput::class)
        assertEquals(direct,engine)
        assertEquals(direct,facade)
        val success=assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(facade)
        assertNotNull(success.value.geometryResults)
        assertTrue(success.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(success.diagnostics.distinct(),success.diagnostics)
        assertNotNull(core.findModel(model.descriptor.id))
    }
    @Test fun `SealedHousing invalid trace has identical direct and facade diagnostics`() {
        val input=SealedHousingIntermediateFixtures.input()
        val bad=input.copy(heatTransfer=input.heatTransfer!!.copy(enclosureEmissivity=Double.NaN))
        val model=SealedHousingLegacyModel()
        val direct=model.calculate(bad)
        assertIs<CalculationResult.Failure>(direct)
        val core=ThermoraCoreFactory.createDefault()
        assertEquals(direct,core.calculate(model.descriptor.id,bad,SealedHousingLegacyOutput::class))
    }

    @Test fun `InternalMixing intermediate output survives Engine and Core`() {
        val model=InternalMixingLegacyModel()
        val input=InternalMixingIntermediateFixtures.input()
        val registry=CalculationModelRegistry()
        assertIs<CalculationResult.Success<Unit>>(registry.register(model))
        val direct=model.calculate(input)
        val engine=CalculationEngine(registry).calculate(model.descriptor.id,input,InternalMixingLegacyOutput::class)
        val core=ThermoraCoreFactory.createDefault()
        val facade=core.calculate(model.descriptor.id,input,InternalMixingLegacyOutput::class)
        assertEquals(direct,engine)
        assertEquals(direct,facade)
        val success=assertIs<CalculationResult.Success<InternalMixingLegacyOutput>>(facade)
        assertNotNull(success.value.geometryResults)
        assertTrue(success.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(success.diagnostics.distinct(),success.diagnostics)
        assertNotNull(core.findModel(model.descriptor.id))
    }
    @Test fun `InternalMixing invalid trace has identical direct and facade diagnostics`() {
        val input=InternalMixingIntermediateFixtures.input()
        val bad=input.copy(heatTransfer=input.heatTransfer!!.copy(reducedEmissivity=Double.NaN))
        val model=InternalMixingLegacyModel()
        val direct=model.calculate(bad)
        assertIs<CalculationResult.Failure>(direct)
        val core=ThermoraCoreFactory.createDefault()
        assertEquals(direct,core.calculate(model.descriptor.id,bad,InternalMixingLegacyOutput::class))
    }

    @Test fun `ExternalCooling intermediate output survives Engine and Core`() {
        val model=ExternalCoolingLegacyModel()
        val input=ExternalCoolingIntermediateFixtures.input()
        val registry=CalculationModelRegistry()
        assertIs<CalculationResult.Success<Unit>>(registry.register(model))
        val direct=model.calculate(input)
        val engine=CalculationEngine(registry).calculate(model.descriptor.id,input,ExternalCoolingLegacyOutput::class)
        val core=ThermoraCoreFactory.createDefault()
        val facade=core.calculate(model.descriptor.id,input,ExternalCoolingLegacyOutput::class)
        assertEquals(direct,engine)
        assertEquals(direct,facade)
        val success=assertIs<CalculationResult.Success<ExternalCoolingLegacyOutput>>(facade)
        assertNotNull(success.value.geometryResults)
        assertTrue(success.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(success.diagnostics.distinct(),success.diagnostics)
        assertNotNull(core.findModel(model.descriptor.id))
    }
    @Test fun `ExternalCooling invalid trace has identical direct and facade diagnostics`() {
        val input=ExternalCoolingIntermediateFixtures.input()
        val bad=input.copy(heatTransfer=input.heatTransfer!!.copy(enclosureEmissivity=Double.NaN))
        val model=ExternalCoolingLegacyModel()
        val direct=model.calculate(bad)
        assertIs<CalculationResult.Failure>(direct)
        val core=ThermoraCoreFactory.createDefault()
        assertEquals(direct,core.calculate(model.descriptor.id,bad,ExternalCoolingLegacyOutput::class))
    }
}

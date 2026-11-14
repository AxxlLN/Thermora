
package com.thermora.application.core

import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.foundation.domain.environment.AmbientConditions
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.domain.thermal.ThermalSource
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.unit.StandardUnits
import com.thermora.models.sealedhousing.legacy.SealedHousingLegacyModel
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ThermoraCoreTest {

    private val modelId = CalculationModelId("sealed-housing-legacy")

    private fun referenceInput() = SealedHousingLegacyInput(
        ambient = AmbientConditions(
            Temperature.fromKelvin(313.0)
        ),
        source = ThermalSource(
            Quantity(4.3, StandardUnits.WATT)
        ),
        heatedZone = HeatSourceGeometry(
            length = Quantity(0.112, StandardUnits.METER),
            width = Quantity(0.069, StandardUnits.METER),
            height = Quantity(0.0055, StandardUnits.METER),
            volumeFillCoefficient = 0.5
        ),
        criticalElement = CriticalElementThermalProperties(
            power = Quantity(1.25, StandardUnits.WATT),
            transitionToCaseResistance = ThermalResistance(
                Quantity(5.0, StandardUnits.KELVIN_PER_WATT)
            ),
            caseToRadiatorResistance = ThermalResistance(
                Quantity(0.5, StandardUnits.KELVIN_PER_WATT)
            )
        ),
        elementWithRadiatorArea = Quantity(
            0.00423,
            StandardUnits.SQUARE_METER
        )
    )

    @Test
    fun `factory registers default calculation model`() {
        val core = ThermoraCoreFactory.createDefault()

        val models = core.availableModels()

        assertEquals(1, models.size)
        assertEquals(modelId, models.single().id)
    }

    @Test
    fun `finds registered calculation model`() {
        val core = ThermoraCoreFactory.createDefault()

        val descriptor = core.findModel(modelId)

        assertNotNull(descriptor)
        assertEquals("1.0.0", descriptor.version)
    }

    @Test
    fun `executes calculation through application core`() {
        val core = ThermoraCoreFactory.createDefault()

        val result = core.calculate(
            modelId = modelId,
            input = referenceInput(),
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        val success =
            assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)

        assertEquals(
            323.8726142430732,
            success.value.enclosureTemperature.kelvin,
            1e-9
        )

        assertEquals(
            351.09906309240495,
            success.value.junctionTemperature.kelvin,
            1e-9
        )

        assertTrue(
            success.diagnostics.any {
                it.code == "SEALED_LEGACY_APPLICABILITY_UNKNOWN"
            }
        )
    }


    @Test
    fun `creates core without calculation models`() {
        val core = ThermoraCoreFactory.create(emptyList())

        assertTrue(core.availableModels().isEmpty())
        assertEquals(
            null,
            core.findModel(modelId)
        )
    }

    @Test
    fun `creates core with custom calculation models`() {
        val core = ThermoraCoreFactory.create(
            listOf(SealedHousingLegacyModel())
        )

        assertEquals(1, core.availableModels().size)
        assertNotNull(core.findModel(modelId))

        val result = core.calculate(
            modelId = modelId,
            input = referenceInput(),
            expectedOutputType = SealedHousingLegacyOutput::class
        )

        assertIs<CalculationResult.Success<SealedHousingLegacyOutput>>(result)
    }

    @Test
    fun `rejects duplicate calculation model identifiers`() {
        val exception = assertFailsWith<IllegalStateException> {
            ThermoraCoreFactory.create(
                listOf(
                    SealedHousingLegacyModel(),
                    SealedHousingLegacyModel()
                )
            )
        }

        assertTrue(
            exception.message?.contains("Failed to register model") == true
        )
    }

}

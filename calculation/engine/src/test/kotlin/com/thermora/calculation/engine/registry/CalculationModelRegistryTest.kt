package com.thermora.calculation.engine.registry

import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.calculation.api.metadata.CalculationModelDescriptor
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.calculation.api.model.CalculationInput
import com.thermora.calculation.api.model.CalculationModel
import com.thermora.calculation.api.model.CalculationOutput
import com.thermora.foundation.common.result.CalculationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalculationModelRegistryTest {

    private data class TestInput(
        val value: Double
    ) : CalculationInput

    private data class TestOutput(
        val value: Double
    ) : CalculationOutput

    private class TestModel(
        override val descriptor: CalculationModelDescriptor
    ) : CalculationModel<TestInput, TestOutput> {

        override val inputType = TestInput::class
        override val outputType = TestOutput::class

        override fun validate(input: TestInput): ValidationReport = ValidationReport.valid()

        override fun calculate(
            input: TestInput
        ): CalculationResult<TestOutput> {
            return CalculationResult.Success(
                TestOutput(input.value * 2)
            )
        }
    }

    private fun createModel(
        id: String = "test.model"
    ) = TestModel(
        CalculationModelDescriptor(
            id = CalculationModelId(id),
            name = "Test model",
            version = "1.0.0"
        )
    )

    @Test
    fun `register should register model`() {
        val registry = CalculationModelRegistry()
        val model = createModel()

        val result = registry.register(model)

        assertIs<CalculationResult.Success<Unit>>(result)
        assertEquals(model.descriptor, registry.find(model.descriptor.id))
    }

    @Test
    fun `register should reject duplicate model ID`() {
        val registry = CalculationModelRegistry()

        registry.register(createModel())

        val result = registry.register(createModel())

        assertIs<CalculationResult.Failure>(result)
    }

    @Test
    fun `getAll should return registered models sorted by ID`() {
        val registry = CalculationModelRegistry()

        registry.register(createModel("z.model"))
        registry.register(createModel("a.model"))
        registry.register(createModel("m.model"))

        val ids = registry.getAll().map { it.id.value }

        assertEquals(
            listOf("a.model", "m.model", "z.model"),
            ids
        )
    }

    @Test
    fun `unregister should remove registered model`() {
        val registry = CalculationModelRegistry()
        val model = createModel()

        registry.register(model)

        assertTrue(registry.unregister(model.descriptor.id))
        assertNull(registry.find(model.descriptor.id))
        assertFalse(registry.unregister(model.descriptor.id))
    }

    @Test
    fun `find should return null for unknown model`() {
        val registry = CalculationModelRegistry()

        assertNull(
            registry.find(CalculationModelId("unknown.model"))
        )
    }
}

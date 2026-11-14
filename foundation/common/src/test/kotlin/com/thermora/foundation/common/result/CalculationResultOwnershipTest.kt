package com.thermora.foundation.common.result

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class CalculationResultOwnershipTest {

    private fun diagnostic(details: Map<String, String> = emptyMap()) =
        Diagnostic("E", "error", DiagnosticSeverity.ERROR, details = details)

    @Test
    fun `success snapshots the source list and nested diagnostic details`() {
        val details = mutableMapOf("key" to "value")
        val diagnostics = mutableListOf(diagnostic(details))
        val result = CalculationResult.Success(value = "ok", diagnostics = diagnostics)
        val hash = result.hashCode()
        details.clear()
        diagnostics.clear()

        assertEquals("ok", result.value)
        assertEquals(listOf(diagnostic(mapOf("key" to "value"))), result.diagnostics)
        assertEquals(hash, result.hashCode())
        assertSame(result.diagnostics, result.diagnostics)
    }

    @Test
    fun `failure snapshots the source list`() {
        val diagnostics = mutableListOf(diagnostic())
        val result = CalculationResult.Failure(diagnostics = diagnostics)
        diagnostics.clear()

        assertEquals(listOf(diagnostic()), result.diagnostics)
        assertSame(result.diagnostics, result.diagnostics)
    }

    @Test
    fun `result getters do not allow list or nested map mutation`() {
        val diagnostic = diagnostic(mapOf("key" to "value"))
        val success = CalculationResult.Success("ok", mutableListOf(diagnostic))
        val failure = CalculationResult.Failure(mutableListOf(diagnostic))

        listOf(success.diagnostics, failure.diagnostics).forEach { diagnostics ->
            assertFailsWith<UnsupportedOperationException> { (diagnostics as MutableList).clear() }
            assertFailsWith<UnsupportedOperationException> { (diagnostics.first().details as MutableMap).clear() }
            assertEquals(listOf(diagnostic), diagnostics)
        }
    }

    @Test
    fun `component methods preserve order and immutable diagnostic collections`() {
        val success = CalculationResult.Success(value = "ok", diagnostics = mutableListOf(diagnostic()))
        val failure = CalculationResult.Failure(mutableListOf(diagnostic()))
        val (value, successDiagnostics) = success
        val (failureDiagnostics) = failure

        assertEquals("ok", value)
        assertEquals(success.diagnostics, successDiagnostics)
        assertEquals(failure.diagnostics, failureDiagnostics)
        assertFailsWith<UnsupportedOperationException> { (successDiagnostics as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (failureDiagnostics as MutableList).clear() }
    }

    @Test
    fun `copy snapshots replacement diagnostics and keeps original results independent`() {
        val original = diagnostic()
        val replacement = diagnostic(mapOf("replacement" to "yes"))
        val diagnostics = mutableListOf(replacement)
        val success = CalculationResult.Success("original", listOf(original))
        val failure = CalculationResult.Failure(listOf(original))
        val successCopy = success.copy(value = "copy", diagnostics = diagnostics)
        val failureCopy = failure.copy(diagnostics = diagnostics)
        diagnostics.clear()

        assertEquals(listOf(original), success.diagnostics)
        assertEquals(listOf(original), failure.diagnostics)
        assertEquals("copy", successCopy.value)
        assertEquals(listOf(replacement), successCopy.diagnostics)
        assertEquals(listOf(replacement), failureCopy.diagnostics)
        assertEquals(success, success.copy())
        assertEquals(failure, failure.copy())
        assertFailsWith<UnsupportedOperationException> { (successCopy.component2() as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (failureCopy.component1() as MutableList).clear() }
    }

    @Test
    fun `result value semantics and rendering match former data classes`() {
        val diagnostic = diagnostic()
        val diagnostics = listOf(diagnostic)
        val success = CalculationResult.Success("ok", diagnostics)
        val failure = CalculationResult.Failure(diagnostics)

        assertEquals("ok".hashCode() * 31 + diagnostics.hashCode(), success.hashCode())
        assertEquals(diagnostics.hashCode(), failure.hashCode())
        assertEquals("Success(value=ok, diagnostics=[$diagnostic])", success.toString())
        assertEquals("Failure(diagnostics=[$diagnostic])", failure.toString())
        assertNotEquals(success, success.copy(value = "other"))
        assertNotEquals(failure, failure.copy(diagnostics = emptyList()))
        assertEquals(CalculationResult.Success<String?>(null), CalculationResult.Success<String?>(null).copy())
        assertEquals(CalculationResult.Success(Double.NaN), CalculationResult.Success(Double.NaN))
        assertNotEquals(CalculationResult.Success(-0.0), CalculationResult.Success(0.0))
    }

    @Test
    fun `generic success value retains its existing ownership semantics`() {
        val value = mutableListOf(1)
        val success = CalculationResult.Success(value)
        value.add(2)

        assertSame(value, success.value)
        assertEquals(listOf(1, 2), success.value)
        assertFailsWith<UnsupportedOperationException> { (success.diagnostics as MutableList).add(diagnostic()) }
    }

    @Test
    fun `parallel readers observe stable diagnostics after source collections change`() {
        val details = mutableMapOf("key" to "value")
        val diagnostics = mutableListOf(diagnostic(details))
        val success = CalculationResult.Success("ok", diagnostics)
        val failure = CalculationResult.Failure(diagnostics)
        val expected = listOf(diagnostic(mapOf("key" to "value")))
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(4)
        try {
            val readers = (1..4).map {
                executor.submit {
                    check(start.await(5, TimeUnit.SECONDS))
                    repeat(1000) {
                        assertEquals(expected, success.diagnostics)
                        assertEquals(expected, failure.diagnostics)
                    }
                }
            }
            details.clear()
            diagnostics.clear()
            start.countDown()
            readers.forEach { it.get(5, TimeUnit.SECONDS) }
        } finally {
            start.countDown()
            executor.shutdownNow()
        }
    }
}

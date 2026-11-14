package com.thermora.calculation.api.validation

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ValidationReportOwnershipTest {

    private val error = Diagnostic("E", "error", DiagnosticSeverity.ERROR)
    private val warning = Diagnostic("W", "warning", DiagnosticSeverity.WARNING)

    @Test
    fun `source mutation cannot change validity warnings or hashCode`() {
        val diagnostics = mutableListOf(error, warning)
        val report = ValidationReport(diagnostics = diagnostics)
        val hash = report.hashCode()
        diagnostics.clear()

        assertFalse(report.isValid)
        assertTrue(report.hasWarnings)
        assertEquals(listOf(error, warning), report.diagnostics)
        assertEquals(hash, report.hashCode())
    }

    @Test
    fun `getter and destructuring do not expose mutable diagnostic data`() {
        val details = mutableMapOf("key" to "value")
        val diagnostic = error.copy(details = details)
        val report = ValidationReport(mutableListOf(diagnostic))
        val (diagnostics) = report
        details.clear()

        assertFailsWith<UnsupportedOperationException> { (report.diagnostics as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (diagnostics as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (diagnostics.first().details as MutableMap).clear() }
        assertEquals(mapOf("key" to "value"), diagnostics.first().details)
        assertFalse(report.isValid)
    }

    @Test
    fun `copy snapshots replacement list and default copy stays immutable`() {
        val report = ValidationReport.of(error)
        val replacement = mutableListOf(warning)
        val copy = report.copy(diagnostics = replacement)
        replacement.clear()

        assertFalse(report.isValid)
        assertTrue(copy.isValid)
        assertTrue(copy.hasWarnings)
        assertEquals(listOf(warning), copy.diagnostics)
        assertEquals(report, report.copy())
        assertFailsWith<UnsupportedOperationException> { (copy.diagnostics as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (report.copy().component1() as MutableList).clear() }
    }

    @Test
    fun `factories and merge preserve order and immutable lists`() {
        val first = ValidationReport.of(warning)
        val second = ValidationReport.of(error)
        val merged = first.merge(second)

        assertEquals(listOf(warning, error), merged.diagnostics)
        assertFalse(merged.isValid)
        listOf(ValidationReport.valid(), ValidationReport(), first, merged).forEach { report ->
            assertFailsWith<UnsupportedOperationException> { (report.diagnostics as MutableList).add(error) }
        }
    }

    @Test
    fun `equals hashCode toString and named constructor retain value semantics`() {
        val report = ValidationReport(diagnostics = listOf(error, warning))

        assertEquals(report, report.copy())
        assertEquals(listOf(error, warning).hashCode(), report.hashCode())
        assertEquals("ValidationReport(diagnostics=[$error, $warning])", report.toString())
        assertNotEquals(report, report.copy(diagnostics = listOf(error)))
    }

    @Test
    fun `parallel readers retain validity after later source mutations`() {
        val source = mutableListOf(error, warning)
        val report = ValidationReport(source)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(4)
        try {
            val readers = (1..4).map {
                executor.submit {
                    check(start.await(5, TimeUnit.SECONDS))
                    repeat(1000) {
                        assertFalse(report.isValid)
                        assertTrue(report.hasWarnings)
                        assertEquals(listOf(error, warning), report.diagnostics)
                    }
                }
            }
            source.clear()
            start.countDown()
            readers.forEach { it.get(5, TimeUnit.SECONDS) }
        } finally {
            start.countDown()
            executor.shutdownNow()
        }
    }
}

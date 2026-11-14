package com.thermora.foundation.common.diagnostic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class DiagnosticOwnershipTest {

    @Test
    fun `constructor snapshots source details and retains stable value semantics`() {
        val details = linkedMapOf("first" to "1", "second" to "2")
        val diagnostic = Diagnostic("E", "error", DiagnosticSeverity.ERROR, "model", details)
        val equal = Diagnostic("E", "error", DiagnosticSeverity.ERROR, "model", details.toMap())
        val hash = diagnostic.hashCode()
        val rendered = diagnostic.toString()

        details["first"] = "changed"
        details.clear()

        assertEquals(mapOf("first" to "1", "second" to "2"), diagnostic.details)
        assertEquals(equal, diagnostic)
        assertEquals(hash, diagnostic.hashCode())
        assertEquals(rendered, diagnostic.toString())
    }

    @Test
    fun `getter map and entry views cannot modify stored details`() {
        val diagnostic = Diagnostic("E", "error", DiagnosticSeverity.ERROR, details = mutableMapOf("key" to "value"))

        assertFailsWith<UnsupportedOperationException> { (diagnostic.details as MutableMap)["key"] = "changed" }
        assertFailsWith<UnsupportedOperationException> { (diagnostic.details as MutableMap).clear() }
        assertFailsWith<UnsupportedOperationException> {
            (diagnostic.details.entries.first() as MutableMap.MutableEntry).setValue("changed")
        }
        assertFailsWith<UnsupportedOperationException> { (diagnostic.details.keys as MutableSet).clear() }
        assertEquals(mapOf("key" to "value"), diagnostic.details)
    }

    @Test
    fun `copy snapshots explicit details and destructuring exposes only immutable map`() {
        val original = Diagnostic(code = "E", message = "error", severity = DiagnosticSeverity.ERROR)
        val details = linkedMapOf("key" to "value")
        val copy = original.copy(source = "model", details = details)
        val (code, message, severity, source, destructuredDetails) = copy
        details.clear()

        assertEquals("E", code)
        assertEquals("error", message)
        assertEquals(DiagnosticSeverity.ERROR, severity)
        assertEquals("model", source)
        assertEquals(mapOf("key" to "value"), destructuredDetails)
        assertFailsWith<UnsupportedOperationException> { (destructuredDetails as MutableMap).clear() }
        assertEquals(copy, copy.copy())
        assertEquals(emptyMap(), original.details)
        assertNull(original.source)
    }

    @Test
    fun `equals hashCode and toString retain data class value semantics`() {
        val diagnostic = Diagnostic("E", "error", DiagnosticSeverity.ERROR, "model", linkedMapOf("key" to "value"))
        val expectedHash = (((("E".hashCode() * 31 + "error".hashCode()) * 31 +
            DiagnosticSeverity.ERROR.hashCode()) * 31 + "model".hashCode()) * 31 + mapOf("key" to "value").hashCode())

        assertEquals(expectedHash, diagnostic.hashCode())
        assertEquals("Diagnostic(code=E, message=error, severity=ERROR, source=model, details={key=value})", diagnostic.toString())
        assertEquals(diagnostic, diagnostic.copy())
        assertNotEquals(diagnostic, diagnostic.copy(code = "other"))
        assertNotEquals(diagnostic, diagnostic.copy(message = "other"))
        assertNotEquals(diagnostic, diagnostic.copy(severity = DiagnosticSeverity.INFO))
        assertNotEquals(diagnostic, diagnostic.copy(source = null))
        assertNotEquals(diagnostic, diagnostic.copy(details = emptyMap()))
    }

    @Test
    fun `default details are immutable through getter and copy`() {
        val diagnostic = Diagnostic("I", "info", DiagnosticSeverity.INFO)

        assertEquals("Diagnostic(code=I, message=info, severity=INFO, source=null, details={})", diagnostic.toString())
        assertFailsWith<UnsupportedOperationException> { (diagnostic.details as MutableMap)["key"] = "value" }
        assertFailsWith<UnsupportedOperationException> { (diagnostic.copy().component5() as MutableMap)["key"] = "value" }
    }
}

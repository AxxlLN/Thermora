package com.thermora.application.ports.snapshot

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SnapshotDiagnosticTest {

    @Test
    fun `diagnostic round trip preserves all fields`() {
        val original = Diagnostic(
            code = "THERMAL_WARNING",
            message = "Temperature exceeds recommended range",
            severity = DiagnosticSeverity.WARNING,
            source = "sealed-housing-legacy",
            details = mapOf("temperature" to "85.0")
        )

        val restored = SnapshotDiagnostic.from(original).toDiagnostic()

        assertEquals(original, restored)
    }

    @Test
    fun `details are copied defensively`() {
        val details = mutableMapOf("value" to "10")

        val snapshot = SnapshotDiagnostic(
            code = "TEST",
            message = "Test message",
            severity = DiagnosticSeverity.INFO,
            details = details
        )

        details["value"] = "20"

        assertEquals("10", snapshot.details["value"])

        assertFailsWith<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST")
            (snapshot.details as MutableMap<String, String>)["value"] = "30"
        }
    }
}

package com.thermora.application.ports.snapshot

import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.util.Collections
import java.util.LinkedHashMap

class SnapshotDiagnostic(
    val code: String,
    val message: String,
    val severity: DiagnosticSeverity,
    val source: String? = null,
    details: Map<String, String> = emptyMap()
) {
    val details: Map<String, String> =
        Collections.unmodifiableMap(LinkedHashMap(details))

    companion object {
        fun from(diagnostic: Diagnostic): SnapshotDiagnostic =
            SnapshotDiagnostic(
                code = diagnostic.code,
                message = diagnostic.message,
                severity = diagnostic.severity,
                source = diagnostic.source,
                details = diagnostic.details
            )
    }

    fun toDiagnostic(): Diagnostic =
        Diagnostic(
            code = code,
            message = message,
            severity = severity,
            source = source,
            details = details
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SnapshotDiagnostic) return false

        return code == other.code &&
                message == other.message &&
                severity == other.severity &&
                source == other.source &&
                details == other.details
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + severity.hashCode()
        result = 31 * result + (source?.hashCode() ?: 0)
        result = 31 * result + details.hashCode()
        return result
    }

    override fun toString(): String =
        "SnapshotDiagnostic(code=$code, message=$message, severity=$severity, source=$source, details=$details)"
}

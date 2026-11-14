package com.thermora.foundation.common.diagnostic

import java.util.Collections
import java.util.LinkedHashMap

class Diagnostic(
    val code: String,
    val message: String,
    val severity: DiagnosticSeverity,
    val source: String? = null,
    details: Map<String, String> = emptyMap()
) {
    val details: Map<String, String> = Collections.unmodifiableMap(LinkedHashMap(details))

    operator fun component1(): String = code
    operator fun component2(): String = message
    operator fun component3(): DiagnosticSeverity = severity
    operator fun component4(): String? = source
    operator fun component5(): Map<String, String> = details

    fun copy(
        code: String = this.code,
        message: String = this.message,
        severity: DiagnosticSeverity = this.severity,
        source: String? = this.source,
        details: Map<String, String> = this.details
    ): Diagnostic = Diagnostic(code, message, severity, source, details)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Diagnostic) return false
        return code == other.code && message == other.message && severity == other.severity &&
            source == other.source && details == other.details
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
        "Diagnostic(code=$code, message=$message, severity=$severity, source=$source, details=$details)"
}

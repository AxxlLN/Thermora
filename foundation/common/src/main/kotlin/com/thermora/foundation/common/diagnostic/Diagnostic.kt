package com.thermora.foundation.common.diagnostic

data class Diagnostic(
    val code: String,
    val message: String,
    val severity: DiagnosticSeverity,
    val source: String? = null,
    val details: Map<String, String> = emptyMap()
)
import com.thermora.calculation.api.validation.ValidationReport
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import com.thermora.foundation.common.result.CalculationResult

fun main() {
    val original = Diagnostic(code = "E", message = "error", severity = DiagnosticSeverity.ERROR)
    val diagnostic = original.copy(source = "model", details = linkedMapOf("key" to "value"))
    val (code, message, severity, source, details) = diagnostic
    check(code == "E" && message == "error" && severity == DiagnosticSeverity.ERROR)
    check(source == "model" && details == mapOf("key" to "value"))
    check(diagnostic == diagnostic.copy())
    check(diagnostic.hashCode() == diagnostic.copy().hashCode())

    val success = CalculationResult.Success(value = "ok", diagnostics = listOf(diagnostic))
    val (value, diagnostics) = success.copy(value = "copy")
    check(value == "copy" && diagnostics == listOf(diagnostic))
    check(success.copy(diagnostics = emptyList()).diagnostics.isEmpty())
    check(CalculationResult.Success(value = "default").diagnostics.isEmpty())

    val failure = CalculationResult.Failure(diagnostics = diagnostics)
    val (failureDiagnostics) = failure.copy(diagnostics = listOf(original))
    check(failureDiagnostics == listOf(original))
    check(failure == failure.copy())

    val report = ValidationReport(diagnostics = diagnostics)
    val (reportDiagnostics) = report.copy(diagnostics = diagnostics)
    check(reportDiagnostics == diagnostics && !report.isValid)
    check(ValidationReport().isValid && ValidationReport.valid().isValid)
    check(ValidationReport.of(diagnostic).merge(ValidationReport()).diagnostics == diagnostics)
    check(report.toString() == "ValidationReport(diagnostics=$diagnostics)")
}

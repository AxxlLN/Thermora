import com.thermora.application.ports.record.*
import com.thermora.application.ports.snapshot.*
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant

fun externalRecord(): CalculationRecord {
    val snapshot = CalculationSnapshot(CalculationModelId("external-client"), "model-version",
        SnapshotPayload(SnapshotSchema("input", 1), "opaque input"),
        SnapshotPayload(SnapshotSchema("output", 2), "opaque output"), CalculationStatus.SUCCESS,
        Instant.EPOCH, Instant.EPOCH, listOf(SnapshotDiagnostic.from(Diagnostic("INFO", "Message", DiagnosticSeverity.INFO))))
    return CalculationRecord(CalculationRecordId("external-record"), 3, snapshot, Instant.EPOCH)
}

fun externalInsert(repository: CalculationRecordRepository): RepositoryResult<Unit> = repository.insert(externalRecord())
fun externalHistory(repository: CalculationRecordRepository): RepositoryResult<CalculationHistoryPage> = repository.findPage(CalculationHistoryQuery())

import com.thermora.infrastructure.persistence.memory.InMemoryCalculationRecordRepository
import com.thermora.application.ports.record.*
import com.thermora.application.ports.snapshot.*
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.diagnostic.Diagnostic
import com.thermora.foundation.common.diagnostic.DiagnosticSeverity
import java.time.Instant

fun memoryOnlyDependency(): RepositoryResult<CalculationRecord?> {
    val repository: CalculationRecordRepository = InMemoryCalculationRecordRepository()
    val id = CalculationRecordId("memory-only-consumer")
    val snapshot = CalculationSnapshot(CalculationModelId("historic"), "v1",
        SnapshotPayload(SnapshotSchema("input", 1), "opaque"), null, CalculationStatus.FAILURE,
        Instant.EPOCH, Instant.EPOCH, listOf(SnapshotDiagnostic.from(Diagnostic("ERROR", "Message", DiagnosticSeverity.ERROR))))
    repository.insert(CalculationRecord(id, 1, snapshot, Instant.EPOCH))
    val page: RepositoryResult<CalculationHistoryPage> = repository.findPage(CalculationHistoryQuery(100, Long.MAX_VALUE))
    check(page is RepositoryResult.Success)
    return repository.findById(id)
}

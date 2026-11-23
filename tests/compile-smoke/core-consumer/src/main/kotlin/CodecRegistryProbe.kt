import com.thermora.application.core.codec.SnapshotCodecRegistry
import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.codec.SnapshotCodecResult
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema

fun externalRegistry(codecs: Collection<SnapshotCodec<*>>): SnapshotCodecRegistry = SnapshotCodecRegistry(codecs)

fun externalTypedCodec(registry: SnapshotCodecRegistry, schema: SnapshotSchema): SnapshotCodec<String>? =
    registry.find(schema, String::class)

fun externalDecode(registry: SnapshotCodecRegistry, payload: SnapshotPayload): SnapshotCodecResult<String>? =
    registry.find(payload.schema, String::class)?.decode(payload)

fun externalSchemas(registry: SnapshotCodecRegistry): List<SnapshotSchema> = registry.availableSchemas()

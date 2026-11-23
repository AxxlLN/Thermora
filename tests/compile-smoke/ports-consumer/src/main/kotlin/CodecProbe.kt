import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.codec.SnapshotCodecError
import com.thermora.application.ports.codec.SnapshotCodecResult
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema

class ExternalTextCodec : SnapshotCodec<String> {
    override val schema = SnapshotSchema("external.text", 1)
    override val valueType = String::class

    override fun encode(value: String): SnapshotCodecResult<SnapshotPayload> =
        if (value.isBlank()) SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE, "Empty text")
        else SnapshotCodecResult.Success(SnapshotPayload(schema, value))

    override fun decode(payload: SnapshotPayload): SnapshotCodecResult<String> = when {
        payload.schema.id != schema.id -> SnapshotCodecResult.Failure(SnapshotCodecError.SCHEMA_MISMATCH, "Different schema")
        payload.schema.version != schema.version -> SnapshotCodecResult.Failure(SnapshotCodecError.UNSUPPORTED_SCHEMA, "Different version")
        else -> SnapshotCodecResult.Success(payload.content)
    }
}

fun encodeExternalText(value: String): SnapshotCodecResult<SnapshotPayload> = ExternalTextCodec().encode(value)

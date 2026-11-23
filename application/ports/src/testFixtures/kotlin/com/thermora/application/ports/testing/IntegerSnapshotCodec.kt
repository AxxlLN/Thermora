package com.thermora.application.ports.testing

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.codec.SnapshotCodecError
import com.thermora.application.ports.codec.SnapshotCodecResult
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema

class IntegerSnapshotCodec(
    override val schema: SnapshotSchema = SnapshotSchema("test.positive-integer", 1),
    private val prefix: String = ""
) : SnapshotCodec<Int> {
    override val valueType = Int::class

    override fun encode(value: Int): SnapshotCodecResult<SnapshotPayload> =
        if (value > 0) SnapshotCodecResult.Success(SnapshotPayload(schema, "$prefix$value"))
        else SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE, "Value must be positive")

    override fun decode(payload: SnapshotPayload): SnapshotCodecResult<Int> {
        if (payload.schema.id != schema.id) {
            return SnapshotCodecResult.Failure(SnapshotCodecError.SCHEMA_MISMATCH, "Different schema ID")
        }
        if (payload.schema.version != schema.version) {
            return SnapshotCodecResult.Failure(SnapshotCodecError.UNSUPPORTED_SCHEMA, "Unsupported schema version")
        }
        if (!payload.content.startsWith(prefix)) {
            return SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_PAYLOAD, "Missing prefix")
        }
        val value = payload.content.removePrefix(prefix).toIntOrNull()
            ?: return SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_PAYLOAD, "Expected a signed 32-bit integer")
        return if (value > 0) SnapshotCodecResult.Success(value)
        else SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE, "Value must be positive")
    }
}

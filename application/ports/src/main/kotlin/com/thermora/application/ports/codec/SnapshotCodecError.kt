package com.thermora.application.ports.codec

enum class SnapshotCodecError {
    SCHEMA_MISMATCH,
    UNSUPPORTED_SCHEMA,
    INVALID_PAYLOAD,
    INVALID_VALUE,
    INTERNAL_ERROR
}

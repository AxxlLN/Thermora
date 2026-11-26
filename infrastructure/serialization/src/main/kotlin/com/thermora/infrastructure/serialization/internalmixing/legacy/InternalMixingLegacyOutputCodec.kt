package com.thermora.infrastructure.serialization.internalmixing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.internalmixing.legacy.output.InternalMixingLegacyOutput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.internalmixing.legacy.v1.InternalMixingLegacyOutputJsonMapper

class InternalMixingLegacyOutputCodec : SnapshotCodec<InternalMixingLegacyOutput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.internal-mixing.legacy.output", 1),
    InternalMixingLegacyOutput::class,
    InternalMixingLegacyOutputJsonMapper::read,
    InternalMixingLegacyOutputJsonMapper::write
)

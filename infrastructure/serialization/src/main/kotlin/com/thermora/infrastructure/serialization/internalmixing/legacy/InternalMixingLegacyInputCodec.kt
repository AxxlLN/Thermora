package com.thermora.infrastructure.serialization.internalmixing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.internalmixing.legacy.input.InternalMixingLegacyInput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.internalmixing.legacy.v1.InternalMixingLegacyInputJsonMapper

class InternalMixingLegacyInputCodec : SnapshotCodec<InternalMixingLegacyInput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.internal-mixing.legacy.input", 1),
    InternalMixingLegacyInput::class,
    InternalMixingLegacyInputJsonMapper::read,
    InternalMixingLegacyInputJsonMapper::write
)

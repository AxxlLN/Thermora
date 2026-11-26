package com.thermora.infrastructure.serialization.externalcooling.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.externalcooling.legacy.input.ExternalCoolingLegacyInput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.externalcooling.legacy.v1.ExternalCoolingLegacyInputJsonMapper

class ExternalCoolingLegacyInputCodec : SnapshotCodec<ExternalCoolingLegacyInput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.external-cooling.legacy.input", 1),
    ExternalCoolingLegacyInput::class,
    ExternalCoolingLegacyInputJsonMapper::read,
    ExternalCoolingLegacyInputJsonMapper::write
)

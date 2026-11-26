package com.thermora.infrastructure.serialization.externalcooling.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.externalcooling.legacy.output.ExternalCoolingLegacyOutput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.externalcooling.legacy.v1.ExternalCoolingLegacyOutputJsonMapper

class ExternalCoolingLegacyOutputCodec : SnapshotCodec<ExternalCoolingLegacyOutput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.external-cooling.legacy.output", 1),
    ExternalCoolingLegacyOutput::class,
    ExternalCoolingLegacyOutputJsonMapper::read,
    ExternalCoolingLegacyOutputJsonMapper::write
)

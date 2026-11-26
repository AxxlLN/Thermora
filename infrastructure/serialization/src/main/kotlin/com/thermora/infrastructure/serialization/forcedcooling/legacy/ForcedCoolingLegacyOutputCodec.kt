package com.thermora.infrastructure.serialization.forcedcooling.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.forcedcooling.legacy.output.ForcedCoolingLegacyOutput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.forcedcooling.legacy.v1.ForcedCoolingLegacyOutputJsonMapper

class ForcedCoolingLegacyOutputCodec : SnapshotCodec<ForcedCoolingLegacyOutput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.forced-cooling.legacy.output", 1), ForcedCoolingLegacyOutput::class,
    ForcedCoolingLegacyOutputJsonMapper::read, ForcedCoolingLegacyOutputJsonMapper::write
)

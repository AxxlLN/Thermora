package com.thermora.infrastructure.serialization.forcedcooling.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.forcedcooling.legacy.input.ForcedCoolingLegacyInput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.forcedcooling.legacy.v1.ForcedCoolingLegacyInputJsonMapper

class ForcedCoolingLegacyInputCodec : SnapshotCodec<ForcedCoolingLegacyInput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.forced-cooling.legacy.input", 1), ForcedCoolingLegacyInput::class,
    ForcedCoolingLegacyInputJsonMapper::read, ForcedCoolingLegacyInputJsonMapper::write
)

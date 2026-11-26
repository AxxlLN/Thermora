package com.thermora.infrastructure.serialization.perforatedhousing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.perforatedhousing.legacy.input.PerforatedHousingLegacyInput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.perforatedhousing.legacy.v1.PerforatedHousingLegacyInputJsonMapper

class PerforatedHousingLegacyInputCodec : SnapshotCodec<PerforatedHousingLegacyInput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.perforated-housing.legacy.input", 1), PerforatedHousingLegacyInput::class,
    PerforatedHousingLegacyInputJsonMapper::read, PerforatedHousingLegacyInputJsonMapper::write
)

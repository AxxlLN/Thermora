package com.thermora.infrastructure.serialization.finnedhousing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.finnedhousing.legacy.v1.FinnedHousingLegacyInputJsonMapper

class FinnedHousingLegacyInputCodec : SnapshotCodec<FinnedHousingLegacyInput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.finned-housing.legacy.input", 1),
    FinnedHousingLegacyInput::class,
    FinnedHousingLegacyInputJsonMapper::read,
    FinnedHousingLegacyInputJsonMapper::write
)

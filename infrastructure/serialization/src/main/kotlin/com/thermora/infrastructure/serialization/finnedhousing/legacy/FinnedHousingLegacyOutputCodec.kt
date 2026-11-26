package com.thermora.infrastructure.serialization.finnedhousing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingLegacyOutput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.finnedhousing.legacy.v1.FinnedHousingLegacyOutputJsonMapper

class FinnedHousingLegacyOutputCodec : SnapshotCodec<FinnedHousingLegacyOutput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.finned-housing.legacy.output", 1),
    FinnedHousingLegacyOutput::class,
    FinnedHousingLegacyOutputJsonMapper::read,
    FinnedHousingLegacyOutputJsonMapper::write
)

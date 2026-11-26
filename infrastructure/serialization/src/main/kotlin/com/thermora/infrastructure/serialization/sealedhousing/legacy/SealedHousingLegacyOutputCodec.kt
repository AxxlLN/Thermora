package com.thermora.infrastructure.serialization.sealedhousing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.sealedhousing.legacy.output.SealedHousingLegacyOutput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.sealedhousing.legacy.v1.SealedHousingLegacyOutputJsonMapper

class SealedHousingLegacyOutputCodec : SnapshotCodec<SealedHousingLegacyOutput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.sealed-housing.legacy.output", 1),
    SealedHousingLegacyOutput::class,
    SealedHousingLegacyOutputJsonMapper::read,
    SealedHousingLegacyOutputJsonMapper::write
)

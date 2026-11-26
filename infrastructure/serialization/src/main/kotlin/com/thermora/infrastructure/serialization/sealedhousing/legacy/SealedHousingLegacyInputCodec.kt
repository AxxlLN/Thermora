package com.thermora.infrastructure.serialization.sealedhousing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.sealedhousing.legacy.input.SealedHousingLegacyInput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.sealedhousing.legacy.v1.SealedHousingLegacyInputJsonMapper

class SealedHousingLegacyInputCodec : SnapshotCodec<SealedHousingLegacyInput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.sealed-housing.legacy.input", 1),
    SealedHousingLegacyInput::class,
    SealedHousingLegacyInputJsonMapper::read,
    SealedHousingLegacyInputJsonMapper::write
)

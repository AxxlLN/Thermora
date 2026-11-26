package com.thermora.infrastructure.serialization.perforatedhousing.legacy

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import com.thermora.models.perforatedhousing.legacy.output.PerforatedHousingLegacyOutput
import com.thermora.infrastructure.serialization.model.JsonSnapshotCodec
import com.thermora.infrastructure.serialization.perforatedhousing.legacy.v1.PerforatedHousingLegacyOutputJsonMapper

class PerforatedHousingLegacyOutputCodec : SnapshotCodec<PerforatedHousingLegacyOutput> by JsonSnapshotCodec(
    SnapshotSchema("thermora.perforated-housing.legacy.output", 1), PerforatedHousingLegacyOutput::class,
    PerforatedHousingLegacyOutputJsonMapper::read, PerforatedHousingLegacyOutputJsonMapper::write
)

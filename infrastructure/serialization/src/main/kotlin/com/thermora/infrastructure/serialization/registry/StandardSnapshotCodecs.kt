package com.thermora.infrastructure.serialization.registry

import com.thermora.application.ports.codec.SnapshotCodec
import java.util.Collections
import com.thermora.infrastructure.serialization.sealedhousing.legacy.*
import com.thermora.infrastructure.serialization.internalmixing.legacy.*
import com.thermora.infrastructure.serialization.externalcooling.legacy.*
import com.thermora.infrastructure.serialization.finnedhousing.legacy.*

import com.thermora.infrastructure.serialization.perforatedhousing.legacy.*

import com.thermora.infrastructure.serialization.forcedcooling.legacy.*

object StandardSnapshotCodecs {
    fun all(): List<SnapshotCodec<*>> = Collections.unmodifiableList(listOf(
        SealedHousingLegacyInputCodec(),
        SealedHousingLegacyOutputCodec(),
        InternalMixingLegacyInputCodec(),
        InternalMixingLegacyOutputCodec(),
        ExternalCoolingLegacyInputCodec(),
        ExternalCoolingLegacyOutputCodec(),
        FinnedHousingLegacyInputCodec(),
        FinnedHousingLegacyOutputCodec(),
        PerforatedHousingLegacyInputCodec(),
        PerforatedHousingLegacyOutputCodec(),
        ForcedCoolingLegacyInputCodec(),
        ForcedCoolingLegacyOutputCodec()
    ))
}

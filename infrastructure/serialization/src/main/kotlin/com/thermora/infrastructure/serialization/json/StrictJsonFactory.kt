package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.StreamReadConstraints
import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.core.StreamWriteConstraints
import com.fasterxml.jackson.core.StreamWriteFeature

internal object StrictJsonFactory {

    const val MAX_NESTING_DEPTH = 32
    private const val MAX_NUMBER_LENGTH = 64

    const val MAX_STRING_LENGTH = 256
    const val MAX_NAME_LENGTH = 256

    val instance: JsonFactory = JsonFactory.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .enable(StreamWriteFeature.STRICT_DUPLICATE_DETECTION)
        .disable(StreamWriteFeature.AUTO_CLOSE_CONTENT)
        .streamReadConstraints(
            StreamReadConstraints.builder()
                .maxNestingDepth(MAX_NESTING_DEPTH)
                .maxNumberLength(MAX_NUMBER_LENGTH)
                .maxStringLength(MAX_STRING_LENGTH)
                .maxNameLength(MAX_NAME_LENGTH)
                .build()
        )
        .streamWriteConstraints(
            StreamWriteConstraints.builder()
                .maxNestingDepth(MAX_NESTING_DEPTH)
                .build()
        )
        .build()
}

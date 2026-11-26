package com.thermora.infrastructure.serialization.json

/** A known write resource limit, independent of generator buffering or flush timing. */
internal class JsonWriteLimitFailure(message: String) : IllegalArgumentException(message)

internal fun requireJsonWriteLimit(condition: Boolean, message: String) {
    if (!condition) throw JsonWriteLimitFailure(message)
}

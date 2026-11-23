package com.thermora.infrastructure.serialization.json

internal class JsonTokenReadFailure(
    val kind: JsonFailureKind,
    detail: String,
    cause: Throwable? = null
) : IllegalArgumentException(detail, cause)

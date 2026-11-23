package com.thermora.application.ports.record

sealed interface RepositoryResult<out T> {

    data class Success<T>(
        val value: T
    ) : RepositoryResult<T>

    data class Failure(
        val error: RepositoryError
    ) : RepositoryResult<Nothing>
}

enum class RepositoryError {
    CONFLICT,
    UNAVAILABLE,
    INVALID_REQUEST,
    INTERNAL_ERROR
}

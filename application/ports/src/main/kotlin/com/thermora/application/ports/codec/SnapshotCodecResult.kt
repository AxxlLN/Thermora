package com.thermora.application.ports.codec

sealed interface SnapshotCodecResult<out T> {
    data class Success<T>(val value: T) : SnapshotCodecResult<T>
    data class Failure(val error: SnapshotCodecError, val message: String) : SnapshotCodecResult<Nothing>
}

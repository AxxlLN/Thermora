package com.thermora.cli.transport

/**
 * Result of writing a complete CLI response.
 */
internal sealed interface OutputWriteResult {

    data class Success(
        val bytesWritten: Int
    ) : OutputWriteResult

    data class Failure(
        val error: OutputWriteError,
        val message: String,
        val outputMayBePartial: Boolean = false
    ) : OutputWriteResult
}
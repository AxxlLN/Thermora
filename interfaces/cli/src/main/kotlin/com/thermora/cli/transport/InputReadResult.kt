package com.thermora.cli.transport

/**
 * Result of reading and decoding a CLI request.
 */
internal sealed interface InputReadResult {

    data class Success(
        val text: String
    ) : InputReadResult

    data class Failure(
        val error: InputReadError,
        val message: String
    ) : InputReadResult
}
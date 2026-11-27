package com.thermora.cli.command

/**
 * Result of parsing command-line arguments.
 */
internal sealed interface CommandParseResult {

    data class Success(
        val arguments: CommandArguments
    ) : CommandParseResult

    data class Failure(
        val error: CommandParseError,
        val message: String
    ) : CommandParseResult
}

/**
 * Stable command-line parsing error codes.
 */
internal enum class CommandParseError {

    UNKNOWN_COMMAND,

    INVALID_ARGUMENTS,

    INVALID_MODEL_ID
}
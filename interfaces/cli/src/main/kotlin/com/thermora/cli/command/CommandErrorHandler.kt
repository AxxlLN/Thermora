package com.thermora.cli.command

/**
 * Reports command parsing and dispatch errors.
 */
internal interface CommandErrorHandler {

    fun onParseFailure(
        failure: CommandParseResult.Failure
    )

    fun onMissingHandler(
        command: CliCommand
    )
}
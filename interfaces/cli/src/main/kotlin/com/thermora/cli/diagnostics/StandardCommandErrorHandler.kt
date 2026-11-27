package com.thermora.cli.diagnostics

import com.thermora.cli.command.CliCommand
import com.thermora.cli.command.CommandErrorHandler
import com.thermora.cli.command.CommandParseResult
import java.io.PrintStream

/**
 * Reports command errors to the standard error stream.
 *
 * Temporary implementation used before protocol
 * response serialization is introduced.
 */
internal class StandardCommandErrorHandler(
    private val errorOutput: PrintStream
) : CommandErrorHandler {

    override fun onParseFailure(
        failure: CommandParseResult.Failure
    ) {
        errorOutput.println(
            "${failure.error.name}: ${failure.message}"
        )
    }

    override fun onMissingHandler(
        command: CliCommand
    ) {
        errorOutput.println(
            "INVALID_CONFIGURATION: " +
                    "No handler registered for '${command.commandName}'"
        )
    }
}
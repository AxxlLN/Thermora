package com.thermora.cli.command.handler

import com.thermora.cli.command.CommandArguments
import com.thermora.cli.command.CommandHandler
import com.thermora.cli.execution.CliExitCode
import java.io.PrintStream

/**
 * Prints CLI version information.
 */
internal class VersionCommandHandler(
    private val output: PrintStream,
    private val version: String
) : CommandHandler {

    init {
        require(version.isNotBlank()) {
            "CLI version must not be blank"
        }
    }

    override fun execute(
        arguments: CommandArguments
    ): CliExitCode {

        check(arguments == CommandArguments.Version) {
            "Unexpected arguments for version command"
        }

        output.println("Thermora CLI $version")

        return if (output.checkError()) {
            CliExitCode.INTERNAL_ERROR
        } else {
            CliExitCode.SUCCESS
        }
    }
}
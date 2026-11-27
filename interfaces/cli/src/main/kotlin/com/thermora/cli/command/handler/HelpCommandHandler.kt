package com.thermora.cli.command.handler

import com.thermora.cli.command.CommandArguments
import com.thermora.cli.command.CommandHandler
import com.thermora.cli.execution.CliExitCode
import java.io.PrintStream

/**
 * Prints human-readable CLI usage information.
 */
internal class HelpCommandHandler(
    private val output: PrintStream
) : CommandHandler {

    override fun execute(
        arguments: CommandArguments
    ): CliExitCode {

        check(arguments == CommandArguments.Help) {
            "Unexpected arguments for help command"
        }

        output.println(
            """
            Thermora CLI
            Thermal Analysis Engine

            Usage:
              thermora <command> [arguments]

            Commands:
              help                  Show help
              version               Show version information

            Options:
              -h, --help            Show help
              -v, --version         Show version information
            """.trimIndent()
        )

        return if (output.checkError()) {
            CliExitCode.INTERNAL_ERROR
        } else {
            CliExitCode.SUCCESS
        }
    }
}
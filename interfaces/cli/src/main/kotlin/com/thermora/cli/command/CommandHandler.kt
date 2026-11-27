package com.thermora.cli.command

import com.thermora.cli.execution.CliExitCode

/**
 * Executes a parsed CLI command.
 */
internal fun interface CommandHandler {

    fun execute(arguments: CommandArguments): CliExitCode
}
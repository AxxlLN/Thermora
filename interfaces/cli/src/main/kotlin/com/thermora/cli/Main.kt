package com.thermora.cli

import com.thermora.cli.command.CliCommand
import com.thermora.cli.command.CommandDispatcher
import com.thermora.cli.command.CommandParser
import com.thermora.cli.command.handler.HelpCommandHandler
import com.thermora.cli.command.handler.VersionCommandHandler
import com.thermora.cli.diagnostics.StandardCommandErrorHandler
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    val dispatcher = CommandDispatcher(
        parser = CommandParser(),
        handlers = mapOf(
            CliCommand.HELP to HelpCommandHandler(
                output = System.out
            ),
            CliCommand.VERSION to VersionCommandHandler(
                output = System.out,
                version = "0.1.0"
            )
        ),
        errorHandler = StandardCommandErrorHandler(
            errorOutput = System.err
        )
    )

    val exitCode = dispatcher.dispatch(args)

    exitProcess(exitCode.code)
}
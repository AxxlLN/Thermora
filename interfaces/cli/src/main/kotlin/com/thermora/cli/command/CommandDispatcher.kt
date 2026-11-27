package com.thermora.cli.command

import com.thermora.cli.execution.CliExitCode
import java.util.Collections

/**
 * Routes parsed commands to their registered handlers.
 */
internal class CommandDispatcher(
    private val parser: CommandParser,
    handlers: Map<CliCommand, CommandHandler>,
    private val errorHandler: CommandErrorHandler
) {

    private val handlers: Map<CliCommand, CommandHandler> =
        Collections.unmodifiableMap(LinkedHashMap(handlers))

    fun dispatch(args: Array<String>): CliExitCode {

        return when (val result = parser.parse(args)) {

            is CommandParseResult.Success ->
                execute(result.arguments)

            is CommandParseResult.Failure -> {
                errorHandler.onParseFailure(result)
                CliExitCode.INVALID_REQUEST
            }
        }
    }

    private fun execute(
        arguments: CommandArguments
    ): CliExitCode {

        val handler = handlers[arguments.command]

        if (handler == null) {
            errorHandler.onMissingHandler(arguments.command)
            return CliExitCode.INTERNAL_ERROR
        }

        return handler.execute(arguments)
    }
}
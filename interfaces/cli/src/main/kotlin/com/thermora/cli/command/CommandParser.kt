package com.thermora.cli.command

/**
 * Parses command-line arguments without performing I/O
 * or executing application operations.
 */
internal class CommandParser {

    fun parse(args: Array<String>): CommandParseResult {

        if (args.isEmpty()) {
            return success(CommandArguments.Help)
        }

        val commandName = when (args[0]) {
            "-h", "--help" -> "help"
            "-v", "--version" -> "version"
            else -> args[0]
        }

        val command = CliCommand.fromName(commandName)
            ?: return failure(
                CommandParseError.UNKNOWN_COMMAND,
                "Unknown command: ${args[0]}"
            )

        val arguments = args.drop(1)

        return when (command) {

            CliCommand.HELP ->
                parseWithoutArguments(
                    arguments,
                    CommandArguments.Help
                )

            CliCommand.VERSION ->
                parseWithoutArguments(
                    arguments,
                    CommandArguments.Version
                )

            CliCommand.LIST_MODELS ->
                parseWithoutArguments(
                    arguments,
                    CommandArguments.ListModels
                )

            CliCommand.DESCRIBE_MODEL ->
                parseDescribeModel(arguments)

            CliCommand.VALIDATE ->
                parseWithoutArguments(
                    arguments,
                    CommandArguments.Validate
                )

            CliCommand.CALCULATE ->
                parseWithoutArguments(
                    arguments,
                    CommandArguments.Calculate
                )
        }
    }

    private fun parseWithoutArguments(
        arguments: List<String>,
        command: CommandArguments
    ): CommandParseResult {

        if (arguments.isNotEmpty()) {
            return failure(
                CommandParseError.INVALID_ARGUMENTS,
                "Command '${command.command.commandName}' " +
                        "does not accept additional arguments"
            )
        }

        return success(command)
    }

    private fun parseDescribeModel(
        arguments: List<String>
    ): CommandParseResult {

        if (arguments.size != 1 && arguments.size != 3) {
            return invalidDescribeModelArguments()
        }

        val modelId = arguments[0]

        if (
            modelId.length !in 1..MAX_MODEL_ID_LENGTH ||
            !MODEL_ID_PATTERN.matches(modelId)
        ) {
            return failure(
                CommandParseError.INVALID_MODEL_ID,
                "Invalid model identifier"
            )
        }

        if (arguments.size == 1) {
            return success(
                CommandArguments.DescribeModel(modelId)
            )
        }

        if (arguments[1] != "--schema") {
            return invalidDescribeModelArguments()
        }

        val direction = SchemaDirection.fromName(arguments[2])
            ?: return failure(
                CommandParseError.INVALID_ARGUMENTS,
                "Schema direction must be 'input' or 'output'"
            )

        return success(
            CommandArguments.DescribeModel(
                modelId = modelId,
                schemaDirection = direction
            )
        )
    }

    private fun invalidDescribeModelArguments():
            CommandParseResult.Failure =
        failure(
            CommandParseError.INVALID_ARGUMENTS,
            "Usage: thermora describe-model <model-id> " +
                    "[--schema input|output]"
        )

    private fun success(
        arguments: CommandArguments
    ): CommandParseResult.Success =
        CommandParseResult.Success(arguments)

    private fun failure(
        error: CommandParseError,
        message: String
    ): CommandParseResult.Failure =
        CommandParseResult.Failure(error, message)

    private companion object {

        const val MAX_MODEL_ID_LENGTH = 128

        val MODEL_ID_PATTERN =
            Regex("[a-z0-9][a-z0-9._-]*")
    }
}
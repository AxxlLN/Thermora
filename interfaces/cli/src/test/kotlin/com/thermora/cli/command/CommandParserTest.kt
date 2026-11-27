package com.thermora.cli.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class CommandParserTest {

    private val parser = CommandParser()

    @Test
    fun `empty arguments resolve to help`() {
        assertSuccess(CommandArguments.Help)
    }

    @Test
    fun `help command is supported`() {
        assertSuccess(CommandArguments.Help, "help")
    }

    @Test
    fun `short help alias is supported`() {
        assertSuccess(CommandArguments.Help, "-h")
    }

    @Test
    fun `long help alias is supported`() {
        assertSuccess(CommandArguments.Help, "--help")
    }

    @Test
    fun `version command and aliases are supported`() {
        for (alias in listOf("version", "-v", "--version")) {
            assertSuccess(CommandArguments.Version, alias)
        }
    }

    @Test
    fun `list models command is supported`() {
        assertSuccess(
            CommandArguments.ListModels,
            "list-models"
        )
    }

    @Test
    fun `validate command is supported`() {
        assertSuccess(CommandArguments.Validate, "validate")
    }

    @Test
    fun `calculate command is supported`() {
        assertSuccess(CommandArguments.Calculate, "calculate")
    }

    @Test
    fun `describe model accepts model identifier`() {

        val result = parser.parse(
            arrayOf("describe-model", "sealed-housing-legacy")
        )

        val success =
            assertIs<CommandParseResult.Success>(result)

        val arguments =
            assertIs<CommandArguments.DescribeModel>(
                success.arguments
            )

        assertEquals(
            "sealed-housing-legacy",
            arguments.modelId
        )

        assertNull(arguments.schemaDirection)
    }

    @Test
    fun `describe model accepts input schema direction`() {
        assertSuccess(
            CommandArguments.DescribeModel(
                modelId = "sealed-housing-legacy",
                schemaDirection = SchemaDirection.INPUT
            ),
            "describe-model",
            "sealed-housing-legacy",
            "--schema",
            "input"
        )
    }

    @Test
    fun `describe model accepts output schema direction`() {
        assertSuccess(
            CommandArguments.DescribeModel(
                modelId = "sealed-housing-legacy",
                schemaDirection = SchemaDirection.OUTPUT
            ),
            "describe-model",
            "sealed-housing-legacy",
            "--schema",
            "output"
        )
    }

    @Test
    fun `unknown command is rejected`() {
        assertFailure(
            CommandParseError.UNKNOWN_COMMAND,
            "unknown"
        )
    }

    @Test
    fun `commands are case sensitive`() {
        assertFailure(
            CommandParseError.UNKNOWN_COMMAND,
            "CALCULATE"
        )
    }

    @Test
    fun `unsupported persistent command is rejected`() {
        assertFailure(
            CommandParseError.UNKNOWN_COMMAND,
            "serve-stdio"
        )
    }

    @Test
    fun `unexpected arguments are rejected`() {

        val commands = listOf(
            "help",
            "-h",
            "--help",
            "version",
            "-v",
            "--version",
            "list-models",
            "validate",
            "calculate"
        )

        for (command in commands) {
            assertFailure(
                CommandParseError.INVALID_ARGUMENTS,
                command,
                "unexpected"
            )
        }
    }

    @Test
    fun `missing model identifier is rejected`() {
        assertFailure(
            CommandParseError.INVALID_ARGUMENTS,
            "describe-model"
        )
    }

    @Test
    fun `invalid model identifiers are rejected`() {

        val invalidIds = listOf(
            "",
            " ",
            "UPPERCASE",
            "-invalid",
            "invalid/model",
            "invalid model",
            "модель",
            "a".repeat(129)
        )

        for (id in invalidIds) {
            assertFailure(
                CommandParseError.INVALID_MODEL_ID,
                "describe-model",
                id
            )
        }
    }

    @Test
    fun `model identifier length boundary is accepted`() {

        val modelId = "a".repeat(128)

        assertSuccess(
            CommandArguments.DescribeModel(modelId),
            "describe-model",
            modelId
        )
    }

    @Test
    fun `valid model identifier characters are accepted`() {

        val modelId = "model-1.test_v2"

        assertSuccess(
            CommandArguments.DescribeModel(modelId),
            "describe-model",
            modelId
        )
    }

    @Test
    fun `missing schema direction is rejected`() {
        assertFailure(
            CommandParseError.INVALID_ARGUMENTS,
            "describe-model",
            "sealed-housing-legacy",
            "--schema"
        )
    }

    @Test
    fun `unknown schema direction is rejected`() {
        assertFailure(
            CommandParseError.INVALID_ARGUMENTS,
            "describe-model",
            "sealed-housing-legacy",
            "--schema",
            "unknown"
        )
    }

    @Test
    fun `unknown describe model option is rejected`() {
        assertFailure(
            CommandParseError.INVALID_ARGUMENTS,
            "describe-model",
            "sealed-housing-legacy",
            "--unknown",
            "input"
        )
    }

    @Test
    fun `additional describe model arguments are rejected`() {
        assertFailure(
            CommandParseError.INVALID_ARGUMENTS,
            "describe-model",
            "sealed-housing-legacy",
            "--schema",
            "input",
            "unexpected"
        )
    }

    private fun assertSuccess(
        expected: CommandArguments,
        vararg args: String
    ) {
        val result = parser.parse(arrayOf(*args))

        val success =
            assertIs<CommandParseResult.Success>(result)

        assertEquals(expected, success.arguments)
    }

    private fun assertFailure(
        expected: CommandParseError,
        vararg args: String
    ) {
        val result = parser.parse(arrayOf(*args))

        val failure =
            assertIs<CommandParseResult.Failure>(result)

        assertEquals(expected, failure.error)
    }
}
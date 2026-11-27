package com.thermora.cli.command

import com.thermora.cli.execution.CliExitCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class CommandDispatcherTest {

    @Test
    fun `dispatches command to registered handler`() {

        var received: CommandArguments? = null

        val dispatcher = createDispatcher(
            mapOf(
                CliCommand.HELP to CommandHandler {
                    received = it
                    CliExitCode.SUCCESS
                }
            )
        )

        val result = dispatcher.dispatch(arrayOf("help"))

        assertEquals(CliExitCode.SUCCESS, result)
        assertSame(CommandArguments.Help, received)
    }

    @Test
    fun `dispatches empty arguments to help handler`() {

        var invoked = false

        val dispatcher = createDispatcher(
            mapOf(
                CliCommand.HELP to CommandHandler {
                    invoked = true
                    CliExitCode.SUCCESS
                }
            )
        )

        val result = dispatcher.dispatch(emptyArray())

        assertEquals(CliExitCode.SUCCESS, result)
        assertEquals(true, invoked)
    }

    @Test
    fun `passes typed arguments to handler`() {

        var received: CommandArguments? = null

        val dispatcher = createDispatcher(
            mapOf(
                CliCommand.DESCRIBE_MODEL to CommandHandler {
                    received = it
                    CliExitCode.SUCCESS
                }
            )
        )

        val result = dispatcher.dispatch(
            arrayOf(
                "describe-model",
                "sealed-housing-legacy",
                "--schema",
                "input"
            )
        )

        assertEquals(CliExitCode.SUCCESS, result)

        val arguments =
            assertIs<CommandArguments.DescribeModel>(received)

        assertEquals(
            "sealed-housing-legacy",
            arguments.modelId
        )

        assertEquals(
            SchemaDirection.INPUT,
            arguments.schemaDirection
        )
    }

    @Test
    fun `returns handler exit code unchanged`() {

        for (exitCode in CliExitCode.entries) {

            val dispatcher = createDispatcher(
                mapOf(
                    CliCommand.CALCULATE to CommandHandler {
                        exitCode
                    }
                )
            )

            assertEquals(
                exitCode,
                dispatcher.dispatch(arrayOf("calculate"))
            )
        }
    }

    @Test
    fun `unknown command returns invalid request`() {

        val errors = RecordingErrorHandler()

        val dispatcher = createDispatcher(
            handlers = emptyMap(),
            errors = errors
        )

        val result = dispatcher.dispatch(
            arrayOf("unknown")
        )

        assertEquals(CliExitCode.INVALID_REQUEST, result)

        assertEquals(
            CommandParseError.UNKNOWN_COMMAND,
            errors.parseFailure?.error
        )

        assertNull(errors.missingHandler)
    }

    @Test
    fun `invalid arguments do not invoke handler`() {

        var invoked = false

        val errors = RecordingErrorHandler()

        val dispatcher = createDispatcher(
            handlers = mapOf(
                CliCommand.HELP to CommandHandler {
                    invoked = true
                    CliExitCode.SUCCESS
                }
            ),
            errors = errors
        )

        val result = dispatcher.dispatch(
            arrayOf("help", "unexpected")
        )

        assertEquals(CliExitCode.INVALID_REQUEST, result)
        assertEquals(false, invoked)

        assertEquals(
            CommandParseError.INVALID_ARGUMENTS,
            errors.parseFailure?.error
        )
    }

    @Test
    fun `missing handler returns internal error`() {

        val errors = RecordingErrorHandler()

        val dispatcher = createDispatcher(
            handlers = emptyMap(),
            errors = errors
        )

        val result = dispatcher.dispatch(
            arrayOf("calculate")
        )

        assertEquals(CliExitCode.INTERNAL_ERROR, result)

        assertEquals(
            CliCommand.CALCULATE,
            errors.missingHandler
        )

        assertNull(errors.parseFailure)
    }

    @Test
    fun `dispatcher protects handler registry from mutation`() {

        val handlers = mutableMapOf(
            CliCommand.HELP to CommandHandler {
                CliExitCode.SUCCESS
            }
        )

        val dispatcher = createDispatcher(handlers)

        handlers.clear()

        assertEquals(
            CliExitCode.SUCCESS,
            dispatcher.dispatch(arrayOf("help"))
        )
    }

    private fun createDispatcher(
        handlers: Map<CliCommand, CommandHandler>,
        errors: RecordingErrorHandler = RecordingErrorHandler()
    ): CommandDispatcher =
        CommandDispatcher(
            parser = CommandParser(),
            handlers = handlers,
            errorHandler = errors
        )

    private class RecordingErrorHandler :
        CommandErrorHandler {

        var parseFailure: CommandParseResult.Failure? = null

        var missingHandler: CliCommand? = null

        override fun onParseFailure(
            failure: CommandParseResult.Failure
        ) {
            parseFailure = failure
        }

        override fun onMissingHandler(
            command: CliCommand
        ) {
            missingHandler = command
        }
    }
}
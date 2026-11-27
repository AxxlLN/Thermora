package com.thermora.cli.diagnostics

import com.thermora.cli.command.CliCommand
import com.thermora.cli.command.CommandParseError
import com.thermora.cli.command.CommandParseResult
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class StandardCommandErrorHandlerTest {

    @Test
    fun `reports command parsing failure`() {

        val buffer = ByteArrayOutputStream()

        val handler = StandardCommandErrorHandler(
            PrintStream(buffer, true, Charsets.UTF_8)
        )

        handler.onParseFailure(
            CommandParseResult.Failure(
                error = CommandParseError.UNKNOWN_COMMAND,
                message = "Unknown command"
            )
        )

        assertEquals(
            "UNKNOWN_COMMAND: Unknown command" +
                    System.lineSeparator(),
            buffer.toString(Charsets.UTF_8)
        )
    }

    @Test
    fun `reports missing command handler`() {

        val buffer = ByteArrayOutputStream()

        val handler = StandardCommandErrorHandler(
            PrintStream(buffer, true, Charsets.UTF_8)
        )

        handler.onMissingHandler(CliCommand.CALCULATE)

        assertEquals(
            "INVALID_CONFIGURATION: " +
                    "No handler registered for 'calculate'" +
                    System.lineSeparator(),
            buffer.toString(Charsets.UTF_8)
        )
    }
}
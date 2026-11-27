package com.thermora.cli.command.handler

import com.thermora.cli.command.CommandArguments
import com.thermora.cli.execution.CliExitCode
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HelpCommandHandlerTest {

    @Test
    fun `prints usage information`() {

        val buffer = ByteArrayOutputStream()

        val handler = HelpCommandHandler(
            PrintStream(buffer, true, Charsets.UTF_8)
        )

        val result = handler.execute(CommandArguments.Help)

        val output = buffer.toString(Charsets.UTF_8)

        assertEquals(CliExitCode.SUCCESS, result)

        assertTrue(output.contains("Thermora CLI"))
        assertTrue(output.contains("Usage:"))
        assertTrue(output.contains("--help"))
        assertTrue(output.contains("--version"))
    }

    @Test
    fun `rejects incompatible command arguments`() {

        val handler = HelpCommandHandler(
            PrintStream(ByteArrayOutputStream())
        )

        assertFailsWith<IllegalStateException> {
            handler.execute(CommandArguments.Version)
        }
    }
}
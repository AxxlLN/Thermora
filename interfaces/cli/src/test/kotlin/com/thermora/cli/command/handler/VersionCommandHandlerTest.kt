package com.thermora.cli.command.handler

import com.thermora.cli.command.CommandArguments
import com.thermora.cli.execution.CliExitCode
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VersionCommandHandlerTest {

    @Test
    fun `prints configured CLI version`() {

        val buffer = ByteArrayOutputStream()

        val handler = VersionCommandHandler(
            output = PrintStream(
                buffer,
                true,
                Charsets.UTF_8
            ),
            version = "0.1.0"
        )

        val result = handler.execute(
            CommandArguments.Version
        )

        assertEquals(CliExitCode.SUCCESS, result)

        assertEquals(
            "Thermora CLI 0.1.0" + System.lineSeparator(),
            buffer.toString(Charsets.UTF_8)
        )
    }

    @Test
    fun `rejects blank version`() {

        assertFailsWith<IllegalArgumentException> {

            VersionCommandHandler(
                output = PrintStream(
                    ByteArrayOutputStream()
                ),
                version = " "
            )
        }
    }

    @Test
    fun `rejects incompatible command arguments`() {

        val handler = VersionCommandHandler(
            output = PrintStream(
                ByteArrayOutputStream()
            ),
            version = "0.1.0"
        )

        assertFailsWith<IllegalStateException> {
            handler.execute(CommandArguments.Help)
        }
    }
}
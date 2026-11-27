package com.thermora.cli.execution

import kotlin.test.Test
import kotlin.test.assertEquals

class CliExitCodeTest {

    @Test
    fun `exit codes match protocol v1`() {

        assertEquals(0, CliExitCode.SUCCESS.code)

        assertEquals(2, CliExitCode.INVALID_REQUEST.code)

        assertEquals(3, CliExitCode.CALCULATION_FAILED.code)

        assertEquals(4, CliExitCode.INTERNAL_ERROR.code)
    }

    @Test
    fun `exit codes are unique`() {

        val codes = CliExitCode.entries.map { it.code }

        assertEquals(
            codes.size,
            codes.toSet().size
        )
    }
}
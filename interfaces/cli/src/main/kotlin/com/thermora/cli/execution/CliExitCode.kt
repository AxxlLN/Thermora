package com.thermora.cli.execution

/**
 * Stable process exit codes defined by CLI Protocol V1.
 */
internal enum class CliExitCode(val code: Int) {

    SUCCESS(0),

    INVALID_REQUEST(2),

    CALCULATION_FAILED(3),

    INTERNAL_ERROR(4)
}
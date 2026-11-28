package com.thermora.cli.transport

/**
 * Errors that may occur while preparing or writing
 * a CLI response.
 */
internal enum class OutputWriteError {

    INVALID_UTF8,

    UNEXPECTED_BOM,

    EMPTY_RESPONSE,

    RESPONSE_TOO_LARGE,

    OUTPUT_WRITE_FAILED
}
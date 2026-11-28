package com.thermora.cli.transport

/**
 * Errors that may occur while reading or decoding
 * a CLI request.
 */
internal enum class InputReadError {

    EMPTY_REQUEST,

    INVALID_UTF8,

    UNEXPECTED_BOM,

    REQUEST_TOO_LARGE,

    INPUT_READ_FAILED
}
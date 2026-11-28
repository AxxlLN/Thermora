package com.thermora.cli.transport

/**
 * Byte-level limits of the CLI transport protocol.
 */
internal object TransportLimits {

    const val MAX_REQUEST_BYTES: Int = 2 * 1024 * 1024

    const val MAX_RESPONSE_BYTES: Int = 2 * 1024 * 1024

    const val READ_BUFFER_BYTES: Int = 8 * 1024
}
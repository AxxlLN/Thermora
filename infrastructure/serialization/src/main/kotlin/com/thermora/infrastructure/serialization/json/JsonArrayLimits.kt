package com.thermora.infrastructure.serialization.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import java.util.ArrayDeque

/**
 * Tracks the number of direct elements in each JSON array.
 */
internal class JsonArrayLimits(
    private val onSizeLimit: () -> Nothing = { throw IllegalArgumentException("JSON array exceeds maximum element count") }
) {

    companion object {
        const val MAX_ARRAY_ELEMENTS = 256
    }

    private data class Container(
        val isArray: Boolean,
        var elementCount: Int = 0
    )

    private val containers = ArrayDeque<Container>()

    fun onToken(token: JsonToken) {
        when (token) {
            JsonToken.START_ARRAY,
            JsonToken.START_OBJECT -> {
                countValue()

                containers.addLast(
                    Container(
                        isArray = token == JsonToken.START_ARRAY
                    )
                )
            }

            JsonToken.END_ARRAY,
            JsonToken.END_OBJECT -> {
                require(containers.isNotEmpty()) {
                    "Unexpected JSON container end"
                }

                val container = containers.removeLast()

                require(
                    container.isArray ==
                            (token == JsonToken.END_ARRAY)
                ) {
                    "Mismatched JSON container"
                }
            }

            JsonToken.FIELD_NAME -> Unit

            else -> countValue()
        }
    }

    private fun countValue() {
        val parent = containers.peekLast() ?: return

        if (!parent.isArray) return

        parent.elementCount++

        if (parent.elementCount > MAX_ARRAY_ELEMENTS) onSizeLimit()
    }
}

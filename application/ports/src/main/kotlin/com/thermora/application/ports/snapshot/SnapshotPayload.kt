package com.thermora.application.ports.snapshot

data class SnapshotPayload(
    val schema: SnapshotSchema,
    val content: String
) {
    init {
        require(content.isNotBlank()) {
            "Snapshot payload content must not be blank"
        }
    }
}

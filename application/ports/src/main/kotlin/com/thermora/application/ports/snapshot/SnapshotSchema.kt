package com.thermora.application.ports.snapshot

data class SnapshotSchema(
    val id: String,
    val version: Int
) {
    init {
        require(id.isNotBlank()) {
            "Snapshot schema ID must not be blank"
        }

        require(version > 0) {
            "Snapshot schema version must be positive"
        }
    }
}
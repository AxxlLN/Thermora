package com.thermora.application.ports.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SnapshotSchemaTest {

    @Test
    fun `valid schema is accepted`() {
        val schema = SnapshotSchema(
            id = "thermora.sealed-housing.input",
            version = 1
        )

        assertEquals(1, schema.version)
    }

    @Test
    fun `blank schema ID is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            SnapshotSchema("", 1)
        }
    }

    @Test
    fun `non-positive version is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            SnapshotSchema("thermora.input", 0)
        }
    }
}
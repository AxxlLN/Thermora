package com.thermora.application.core.codec

import com.thermora.application.ports.codec.SnapshotCodec
import com.thermora.application.ports.snapshot.SnapshotSchema
import java.util.Collections
import kotlin.reflect.KClass

class SnapshotCodecRegistry(codecs: Collection<SnapshotCodec<*>> = emptyList()) {
    private class Entry(val codec: SnapshotCodec<*>, val valueType: KClass<*>)

    private val entries: Map<SnapshotSchema, Entry>
    private val schemas: List<SnapshotSchema>

    init {
        val registrations = LinkedHashMap<SnapshotSchema, Entry>()
        for (codec in codecs.toList()) {
            val schema = codec.schema
            require(!registrations.containsKey(schema)) { "Duplicate snapshot schema: $schema" }
            registrations[schema] = Entry(codec, codec.valueType)
        }
        entries = Collections.unmodifiableMap(registrations)
        schemas = Collections.unmodifiableList(ArrayList(registrations.keys.sortedWith(
            compareBy<SnapshotSchema> { it.id }.thenBy { it.version }
        )))
    }

    fun find(schema: SnapshotSchema): SnapshotCodec<*>? = entries[schema]?.codec

    fun <T : Any> find(schema: SnapshotSchema, expectedType: KClass<T>): SnapshotCodec<T>? {
        val entry = entries[schema] ?: return null
        if (entry.valueType != expectedType) return null
        @Suppress("UNCHECKED_CAST")
        return entry.codec as SnapshotCodec<T>
    }

    fun availableSchemas(): List<SnapshotSchema> = schemas
}

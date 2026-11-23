package com.thermora.application.ports.codec

import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.application.ports.snapshot.SnapshotSchema
import kotlin.reflect.KClass

interface SnapshotCodec<T : Any> {
    val schema: SnapshotSchema
    val valueType: KClass<T>

    fun encode(value: T): SnapshotCodecResult<SnapshotPayload>
    fun decode(payload: SnapshotPayload): SnapshotCodecResult<T>
}

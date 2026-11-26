package com.thermora.infrastructure.serialization.model

import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.infrastructure.serialization.json.*
import com.fasterxml.jackson.core.exc.StreamConstraintsException
import kotlin.reflect.KClass

internal class JsonSnapshotCodec<T:Any>(
    override val schema:SnapshotSchema,
    override val valueType:KClass<T>,
    private val read:(StrictJsonReader,String)->T,
    private val write:(StrictJsonWriter,T)->Unit
):SnapshotCodec<T> {
    override fun decode(payload:SnapshotPayload):SnapshotCodecResult<T> {
        if(payload.schema.id!=schema.id) return SnapshotCodecResult.Failure(SnapshotCodecError.SCHEMA_MISMATCH,"Unexpected schema ID")
        if(payload.schema.version!=schema.version) return SnapshotCodecResult.Failure(SnapshotCodecError.UNSUPPORTED_SCHEMA,"Unsupported schema version")
        return try {
            val value=StrictJsonReader.read(payload.content) { r -> r.withReadFailures("$") {
                r.nextToken()
                val result=read(r,"$")
                if(r.nextToken()!=null) throw JsonReadFailure.payload("$","Trailing content")
                result
            } }
            SnapshotCodecResult.Success(value)
        } catch(e:JsonReadFailure) {
            SnapshotCodecResult.Failure(if(e.kind==JsonFailureKind.PAYLOAD) SnapshotCodecError.INVALID_PAYLOAD else SnapshotCodecError.INVALID_VALUE,e.message ?: "Invalid payload")
        }
    }
    override fun encode(value:T):SnapshotCodecResult<SnapshotPayload> {
        return try {
            val content = StrictJsonWriter.write { w -> write(w,value) }
            SnapshotCodecResult.Success(SnapshotPayload(schema,content))
        } catch(e:ModelWriteFailure) {
            SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE,e.message ?: "Invalid wire value")
        } catch(e:StreamConstraintsException) {
            SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE,"JSON resource limit exceeded")
        } catch(e:JsonWriteLimitFailure) {
            SnapshotCodecResult.Failure(SnapshotCodecError.INVALID_VALUE,e.message ?: "JSON output exceeds wire limits")
        }
    }
}

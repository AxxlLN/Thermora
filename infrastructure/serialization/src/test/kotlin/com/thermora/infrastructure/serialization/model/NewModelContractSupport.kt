package com.thermora.infrastructure.serialization.model

import com.fasterxml.jackson.core.JsonToken
import com.thermora.infrastructure.serialization.json.*
import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.application.ports.record.*
import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.model.*
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.infrastructure.persistence.memory.InMemoryCalculationRecordRepository
import java.time.Instant
import java.util.Base64
import java.util.zip.GZIPInputStream
import kotlin.test.*

internal object NewModelContractSupport {
    fun resource(name: String) = checkNotNull(javaClass.getResource("/model-v1/$name")).readText()
    // Independent structural encoder oracle; does not use a model decoder.
    private fun tree(text: String): Any? = StrictJsonReader.read(text) { r ->
        fun value(): Any? = when(r.currentToken()) {
            JsonToken.START_OBJECT -> linkedMapOf<String,Any?>().also { map ->
                while(r.nextToken()!=JsonToken.END_OBJECT) { val key=r.readFieldName();r.nextToken();map[key]=value() }
            }
            JsonToken.START_ARRAY -> mutableListOf<Any?>().also { list -> while(r.nextToken()!=JsonToken.END_ARRAY) list+=value() }
            JsonToken.VALUE_STRING -> r.readString()
            JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_FLOAT -> r.readDouble().toRawBits()
            JsonToken.VALUE_TRUE,JsonToken.VALUE_FALSE -> r.readBoolean()
            JsonToken.VALUE_NULL -> { r.readNull();null }
            else -> error("Unexpected token")
        }
        r.nextToken();value()
    }
    fun <T:Any> fixture(codec:SnapshotCodec<T>, root:String, suffix:String, expected:T) {
        val text=resource(root+suffix+".json")
        assertEquals(expected,assertIs<SnapshotCodecResult.Success<T>>(codec.decode(SnapshotPayload(codec.schema,text))).value)
        val payload=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value
        assertEquals(tree(text),tree(payload.content))
        assertEquals(payload,assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value)
        assertEquals(codec.valueType,expected::class)
        assertEquals(1,codec.schema.version)
        if(suffix=="Full") assertEquals(expected,assertIs<SnapshotCodecResult.Success<T>>(codec.decode(SnapshotPayload(codec.schema,resource(root+"Reversed.json")))).value)
    }
    fun <T:Any> invalid(codec:SnapshotCodec<T>, root:String) {
        val full=resource(root+"Full.json")
        assertIs<SnapshotCodecResult.Success<T>>(codec.decode(SnapshotPayload(codec.schema,full+" \n\t")))
        for(tail in listOf(" {}"," []"," true"," @invalid")) {
            val f=assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,full+tail)))
            assertEquals(SnapshotCodecError.INVALID_PAYLOAD,f.error);assertTrue(f.message.startsWith("$:"))
        }
        assertEquals(SnapshotCodecError.SCHEMA_MISMATCH,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(SnapshotSchema("other",1),"invalid"))).error)
        assertEquals(SnapshotCodecError.UNSUPPORTED_SCHEMA,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema.copy(version=2),"invalid"))).error)
        for(text in listOf("{}","[]","null","{",full+" ".repeat(1048576))) {
            assertEquals(SnapshotCodecError.INVALID_PAYLOAD,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,text))).error)
        }
    }
    fun corpus(slug:String,codecs:List<SnapshotCodec<*>>,expectedNegative:Int,expectedQuoted:Int) {
        for((kind,count) in listOf("negative" to expectedNegative,"quoted" to expectedQuoted)) {
            var seen=0
            GZIPInputStream(checkNotNull(javaClass.getResourceAsStream("/model-v1/$slug-$kind.tsv.gz"))).bufferedReader().useLines { lines -> lines.forEach { row ->
                val parts=row.split('\t');val codec=codecs[parts[0].toInt()]
                val text=String(Base64.getDecoder().decode(parts.last()),Charsets.UTF_8)
                val f=assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,text)))
                assertEquals(SnapshotCodecError.INVALID_PAYLOAD,f.error)
                assertTrue(f.message.startsWith(parts[if(kind=="negative")2 else 1]+":"),f.message)
                seen++
            } }
            assertEquals(count,seen)
        }
    }
    fun <I:CalculationInput,O:CalculationOutput> history(id:String, input:I, invalid:I, ic:SnapshotCodec<I>, oc:SnapshotCodec<O>,
        verifyRestored: (O, List<SnapshotDiagnostic>) -> Unit = { _, _ -> }
    ) {
        val core=ThermoraCoreFactory.createDefault(); val repo=InMemoryCalculationRecordRepository()
        for((index,value) in listOf(input,invalid).withIndex()) {
            val result=core.calculate(CalculationModelId(id),value,oc.valueType)
            if(index==0) assertIs<CalculationResult.Success<O>>(result) else assertIs<CalculationResult.Failure>(result)
            val output=if(result is CalculationResult.Success) assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(oc.encode(result.value)).value else null
            val ds=when(result) { is CalculationResult.Success -> result.diagnostics; is CalculationResult.Failure -> result.diagnostics }
            assertTrue(ds.isNotEmpty())
            val now=Instant.parse("2026-11-26T00:00:00Z")
            val snapshot=CalculationSnapshot(CalculationModelId(id),"1.0.0",assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(ic.encode(value)).value,output,if(index==0)CalculationStatus.SUCCESS else CalculationStatus.FAILURE,now,now,ds.map(SnapshotDiagnostic::from))
            val record=CalculationRecord(CalculationRecordId("$id-$index"),1,snapshot,now)
            assertIs<RepositoryResult.Success<Unit>>(repo.insert(record))
            val stored=checkNotNull(assertIs<RepositoryResult.Success<CalculationRecord?>>(repo.findById(record.id)).value)
            assertEquals(snapshot.diagnostics,stored.snapshot.diagnostics)
            assertEquals(value,assertIs<SnapshotCodecResult.Success<I>>(ic.decode(stored.snapshot.input)).value)
            if (result is CalculationResult.Success) {
                val restored = assertIs<SnapshotCodecResult.Success<O>>(oc.decode(checkNotNull(stored.snapshot.output))).value
                assertEquals(result.value, restored)
                verifyRestored(restored, stored.snapshot.diagnostics)
            } else assertNull(stored.snapshot.output)
        }
    }
}

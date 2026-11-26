package com.thermora.infrastructure.serialization.model

import com.fasterxml.jackson.core.JsonToken
import com.thermora.infrastructure.serialization.json.*
import java.util.Collections

internal class ModelWriteFailure(detail: String, cause: Throwable? = null) : IllegalArgumentException(detail,cause)

internal object ModelJson {
    fun <T:Any> nullable(r: StrictJsonReader, read: ()->T): T? =
        if(r.currentToken()==JsonToken.VALUE_NULL) { r.readNull(); null } else read()
    fun <T> list(r: StrictJsonReader,path:String,read:(StrictJsonReader,String)->T): List<T> {
        r.expect(JsonToken.START_ARRAY)
        val values=ArrayList<T>()
        while(true) {
            val itemPath="$path[${values.size}]"
            val ended=r.withReadFailures(itemPath) {
                val token=r.nextToken() ?: throw JsonReadFailure.payload(itemPath,"Missing array end")
                if(token==JsonToken.END_ARRAY) true else {
                    values+=read(r,itemPath)
                    false
                }
            }
            if(ended) break
        }
        return Collections.unmodifiableList(values)
    }
    fun <T> writeNullable(w:StrictJsonWriter,value:T?,write:(T)->Unit) {
        if(value==null) w.nullValue() else write(value)
    }
    fun <T> writeList(w:StrictJsonWriter,values:List<T>,write:(T)->Unit) {
        if(values.size>JsonArrayLimits.MAX_ARRAY_ELEMENTS) throw ModelWriteFailure("Array exceeds wire limit")
        w.startArray()
        for(value in values.toList()) write(value)
        w.endArray()
    }
    fun double(w:StrictJsonWriter,value:Double) {
        if(!value.isFinite()) throw ModelWriteFailure("Non-finite number")
        w.double(value)
    }
    fun foundation(write:()->Unit) {
        try { write() }
        catch(e: IllegalArgumentException) { throw ModelWriteFailure("Value exceeds Foundation wire constraints",e) }
    }
}

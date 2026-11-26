package com.thermora.infrastructure.serialization.model

import com.thermora.infrastructure.serialization.sealedhousing.legacy.SealedHousingLegacyOutputCodec
import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import kotlin.test.*

class SealedHousingLegacyOutputCodecTest {
 private val codec=SealedHousingLegacyOutputCodec()
 @Test fun `SealedHousingLegacyOutputMinimal independent fixture`() {
  val text=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyOutputMinimal.json")).readText()
  val expected=IndependentModelFixtures.SealedHousingLegacyOutputMinimal()
  assertEquals(expected,assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text))).value)
  val encoded=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value
  assertEquals(text,encoded.content)
  assertEquals(encoded,assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value)
 }
 @Test fun `SealedHousingLegacyOutputFull independent fixture`() {
  val text=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyOutputFull.json")).readText()
  val expected=IndependentModelFixtures.SealedHousingLegacyOutputFull()
  assertEquals(expected,assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text))).value)
  val encoded=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value
  assertEquals(text,encoded.content)
  assertEquals(encoded,assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value)
 }
 @Test fun `schema type and invalid contracts`() {
  assertEquals("thermora.sealed-housing.legacy.output",codec.schema.id)
  assertEquals(1,codec.schema.version)
  assertEquals(IndependentModelFixtures.SealedHousingLegacyOutputFull()::class,codec.valueType)
  assertEquals(SnapshotCodecError.SCHEMA_MISMATCH,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(SnapshotSchema("other",1),"invalid"))).error)
  assertEquals(SnapshotCodecError.UNSUPPORTED_SCHEMA,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema.copy(version=2),"invalid"))).error)
  for(text in listOf("{}","null","[]","{")) assertEquals(SnapshotCodecError.INVALID_PAYLOAD,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,text))).error)
 }
 @Test fun `valid full fixture isolates trailing content validation`() {
  val text=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyOutputFull.json")).readText()
  assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text)))
  assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text+" \n\t\r")))
  for(tail in listOf(" {}", " []", " 42", " @invalid")) {
   val failure=assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,text+tail)))
   assertEquals(SnapshotCodecError.INVALID_PAYLOAD,failure.error)
   assertTrue(failure.message.startsWith("$:"),failure.message)
  }
 }

}

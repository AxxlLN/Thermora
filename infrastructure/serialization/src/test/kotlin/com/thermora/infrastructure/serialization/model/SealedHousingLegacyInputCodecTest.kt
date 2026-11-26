package com.thermora.infrastructure.serialization.model

import com.thermora.infrastructure.serialization.sealedhousing.legacy.SealedHousingLegacyInputCodec
import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import kotlin.test.*

class SealedHousingLegacyInputCodecTest {
 private val codec=SealedHousingLegacyInputCodec()
 @Test fun `SealedHousingLegacyInputMinimal independent fixture`() {
  val text=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyInputMinimal.json")).readText()
  val expected=IndependentModelFixtures.SealedHousingLegacyInputMinimal()
  assertEquals(expected,assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text))).value)
  val encoded=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value
  assertEquals(text,encoded.content)
  assertEquals(encoded,assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value)
 }
 @Test fun `SealedHousingLegacyInputFull independent fixture`() {
  val text=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyInputFull.json")).readText()
  val expected=IndependentModelFixtures.SealedHousingLegacyInputFull()
  assertEquals(expected,assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text))).value)
  val encoded=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value
  assertEquals(text,encoded.content)
  assertEquals(encoded,assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(expected)).value)
 }
 @Test fun `schema type and invalid contracts`() {
  assertEquals("thermora.sealed-housing.legacy.input",codec.schema.id)
  assertEquals(1,codec.schema.version)
  assertEquals(IndependentModelFixtures.SealedHousingLegacyInputFull()::class,codec.valueType)
  assertEquals(SnapshotCodecError.SCHEMA_MISMATCH,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(SnapshotSchema("other",1),"invalid"))).error)
  assertEquals(SnapshotCodecError.UNSUPPORTED_SCHEMA,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema.copy(version=2),"invalid"))).error)
  for(text in listOf("{}","null","[]","{")) assertEquals(SnapshotCodecError.INVALID_PAYLOAD,assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,text))).error)
 }
 @Test fun `valid full fixture isolates trailing content validation`() {
  val text=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyInputFull.json")).readText()
  assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text)))
  assertIs<SnapshotCodecResult.Success<*>>(codec.decode(SnapshotPayload(codec.schema,text+" \n\t\r")))
  for(tail in listOf(" {}", " []", " 42", " @invalid")) {
   val failure=assertIs<SnapshotCodecResult.Failure>(codec.decode(SnapshotPayload(codec.schema,text+tail)))
   assertEquals(SnapshotCodecError.INVALID_PAYLOAD,failure.error)
   assertTrue(failure.message.startsWith("$:"),failure.message)
  }
 }

}

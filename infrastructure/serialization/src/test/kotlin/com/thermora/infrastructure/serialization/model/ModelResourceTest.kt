package com.thermora.infrastructure.serialization.model

import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.*
import com.thermora.infrastructure.serialization.sealedhousing.legacy.*
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.dimension.PhysicalDimension
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.models.sealedhousing.legacy.output.SealedHousingConvectionRegime
import com.thermora.foundation.domain.geometry.WallOrientation
import kotlin.test.*

class ModelResourceTest {
    @Test fun `arrays256 preserve repeats arrays257 are rejected on both boundaries`() {
        val c=SealedHousingLegacyInputCodec()
        val initial=IndependentModelFixtures.SealedHousingLegacyInputFull()
        val config=checkNotNull(initial.heatTransfer)
        val input=initial.copy(heatTransfer=config.copy(wallGaps=List(256){config.wallGaps.first()}))
        val payload=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(input)).value
        assertIs<SnapshotCodecResult.Success<*>>(c.decode(payload))
        val marker="\"wallGaps\":["
        val at=payload.content.indexOf(marker)+marker.length
        val wall="{\"wallIndex\":1,\"gap\":{\"value\":1,\"unit\":{\"kind\":\"standard\",\"id\":\"m\"}},\"orientation\":\"vertical\"},"
        val text=payload.content.substring(0,at)+wall+payload.content.substring(at)
        assertEquals(SnapshotCodecError.INVALID_PAYLOAD,assertIs<SnapshotCodecResult.Failure>(c.decode(SnapshotPayload(c.schema,text))).error)
    }
    @Test fun `sum of permitted lists can exceed total UTF8 output budget`() {
        val c=SealedHousingLegacyOutputCodec()
        val initial=IndependentModelFixtures.SealedHousingLegacyOutputFull()
        val ext=checkNotNull(initial.externalHeatTransfer)
        val inside=checkNotNull(initial.internalHeatTransfer)
        val q=Quantity(1.0,MeasurementUnit("😀".repeat(128),PhysicalDimension.AREA,1.0,-0.0))
        val largeR=com.thermora.foundation.domain.thermal.ThermalResistance(Quantity(1.0,MeasurementUnit("😀".repeat(128),PhysicalDimension.THERMAL_RESISTANCE,1.0,-0.0)))
        val trial=inside.trials.first().copy(dissipatedPower=q,resistance=largeR)
        val input=initial.copy(externalHeatTransfer=ext.copy(laminarCoefficients=List(256){q},turbulentCoefficients=List(256){q},selectedCoefficients=List(256){q}),internalHeatTransfer=inside.copy(wallConvectionCoefficients=List(256){q},trials=List(256){trial}))
        assertEquals(SnapshotCodecError.INVALID_VALUE,assertIs<SnapshotCodecResult.Failure>(c.encode(input)).error)
    }
    @Test fun `Unicode strings and root types retain strict limits`() {
        val c=SealedHousingLegacyInputCodec()
        val valid=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyInputFull.json")).readText()
        for(text in listOf("\uFEFF"+valid,"[".repeat(33)+"0"+"]".repeat(33),valid.replace("\"W\"","\""+"x".repeat(257)+"\""),valid.replace("\"W\"","\"\\uD800\""))) {
            assertEquals(SnapshotCodecError.INVALID_PAYLOAD,assertIs<SnapshotCodecResult.Failure>(c.decode(SnapshotPayload(c.schema,text))).error)
        }
        val cOutput=SealedHousingLegacyOutputCodec()
        val data=IndependentModelFixtures.SealedHousingLegacyOutputFull()
        for(value in listOf(Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY)) {
            val bad=data.copy(externalHeatTransfer=checkNotNull(data.externalHeatTransfer).copy(laminarAirFactor=value))
            assertEquals(SnapshotCodecError.INVALID_VALUE,assertIs<SnapshotCodecResult.Failure>(cOutput.encode(bad)).error)
        }
    }
    @Test fun `all enum IDs are explicit and reversible`() {
        val c=SealedHousingLegacyInputCodec();val value=IndependentModelFixtures.SealedHousingLegacyInputFull()
        val config=checkNotNull(value.heatTransfer)
        val walls=config.wallGaps.first()
        val input=value.copy(heatTransfer=config.copy(wallGaps=listOf(walls.copy(orientation=WallOrientation.VERTICAL),walls.copy(orientation=WallOrientation.HORIZONTAL_TOP),walls.copy(orientation=WallOrientation.HORIZONTAL_BOTTOM))))
        val payload=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(c.encode(input)).value
        for(id in listOf("vertical","horizontal-top","horizontal-bottom")) assertTrue(payload.content.contains("\"$id\""))
        assertEquals(input,assertIs<SnapshotCodecResult.Success<*>>(c.decode(payload)).value)
        val oc=SealedHousingLegacyOutputCodec();val out=IndependentModelFixtures.SealedHousingLegacyOutputFull()
        val both=out.copy(externalHeatTransfer=checkNotNull(out.externalHeatTransfer).copy(regimes=listOf(SealedHousingConvectionRegime.LAMINAR,SealedHousingConvectionRegime.TURBULENT)))
        val op=assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(oc.encode(both)).value
        assertTrue(op.content.contains("\"laminar\",\"turbulent\""))
        assertEquals(both,assertIs<SnapshotCodecResult.Success<*>>(oc.decode(op)).value)
    }
    @Test fun `model numeric token64 is accepted and token65 is rejected with its path`() {
        val c=SealedHousingLegacyOutputCodec()
        val original=checkNotNull(javaClass.getResource("/model-v1/SealedHousingLegacyOutputFull.json")).readText()
        val pattern=Regex("\"sideSwitchThresholdValue\":[0-9.]+")
        assertEquals(1,pattern.findAll(original).count())
        val at="-0."+"1".repeat(61)
        assertEquals(64,at.length)
        val accepted=original.replace(pattern,"\"sideSwitchThresholdValue\":"+at)
        assertIs<SnapshotCodecResult.Success<*>>(c.decode(SnapshotPayload(c.schema,accepted)))
        val rejected=original.replace(pattern,"\"sideSwitchThresholdValue\":"+at+"1")
        val error=assertIs<SnapshotCodecResult.Failure>(c.decode(SnapshotPayload(c.schema,rejected)))
        assertEquals(SnapshotCodecError.INVALID_PAYLOAD,error.error)
        assertTrue(error.message.startsWith("$.externalHeatTransfer.sideSwitchThresholdValue:"),error.message)
    }

}

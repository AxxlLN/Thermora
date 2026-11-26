package com.thermora.infrastructure.serialization.model

import com.thermora.application.ports.codec.*
import com.thermora.application.ports.snapshot.SnapshotPayload
import com.thermora.foundation.domain.thermal.ThermalResistance
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.MeasurementUnit
import com.thermora.infrastructure.serialization.sealedhousing.legacy.SealedHousingLegacyOutputCodec
import com.thermora.models.sealedhousing.legacy.output.*
import kotlin.test.*
import com.thermora.infrastructure.serialization.json.*
import com.thermora.infrastructure.serialization.sealedhousing.legacy.v1.SealedHousingLegacyOutputJsonMapper

class ModelWriteBudgetRegressionTest {
    // Explicit DTO copying mirrors the audit scenario; no reflection or production encoder builds expected data.
    private fun auditValue(): SealedHousingLegacyOutput {
        val n = 227
        val symbol = "😀".repeat(82)
        assertEquals(164, symbol.length)
        assertEquals(328, symbol.toByteArray(Charsets.UTF_8).size)
        fun q(value: Quantity) = Quantity(value.value, value.unit.let {
            MeasurementUnit(symbol, it.dimension, it.scaleToBase, it.offsetToBase)
        })
        fun r(value: ThermalResistance) = ThermalResistance(q(value.value))
        val base = IndependentModelFixtures.SealedHousingLegacyOutputFull()
        val g = checkNotNull(base.geometryResults)
        val e = checkNotNull(base.externalHeatTransfer)
        val i = checkNotNull(base.internalHeatTransfer)
        val ep = e.trials.first()
        val ip = i.trials.first()
        return base.copy(
            enclosureToAmbientResistance = r(base.enclosureToAmbientResistance),
            heatedZoneToAmbientResistance = r(base.heatedZoneToAmbientResistance),
            geometryResults = g.copy(enclosureArea=q(g.enclosureArea), heatedZoneArea=q(g.heatedZoneArea),
                zoneFaceAreaXY=q(g.zoneFaceAreaXY), zoneFaceAreaXZ=q(g.zoneFaceAreaXZ), zoneFaceAreaYZ=q(g.zoneFaceAreaYZ)),
            externalHeatTransfer = e.copy(radiationCoefficient=q(e.radiationCoefficient),
                laminarCoefficients=List(n){q(e.laminarCoefficients.first())},
                turbulentCoefficients=List(n){q(e.turbulentCoefficients.first())},
                selectedCoefficients=List(n){q(e.selectedCoefficients.first())},
                regimes=List(n){e.regimes.first()}, enclosureToAmbientResistance=r(e.enclosureToAmbientResistance),
                trials=List(n){ep.copy(resistance=r(ep.resistance), dissipatedPower=q(ep.dissipatedPower))}),
            internalHeatTransfer = i.copy(wallConvectionCoefficients=List(n){q(i.wallConvectionCoefficients.first())},
                radiationCoefficient=q(i.radiationCoefficient), zoneToEnclosureResistance=r(i.zoneToEnclosureResistance),
                elementToSinkResistance=r(i.elementToSinkResistance),
                trials=List(n){ip.copy(resistance=r(ip.resistance), dissipatedPower=q(ip.dissipatedPower))})
        )
    }

    @Test fun `audit limit occurs inside mapper while writing a field name`() {
        var mapperReturned = false
        val error = assertFailsWith<JsonWriteLimitFailure> {
            StrictJsonWriter.write { w ->
                SealedHousingLegacyOutputJsonMapper.write(w, auditValue())
                mapperReturned = true
            }
        }
        assertFalse(mapperReturned)
        assertTrue(error.stackTrace.any { it.className.endsWith("StrictJsonWriter") && it.methodName == "fieldName" })
    }

    @Test fun `audit supplementary payload overflow returns failure and codec remains reusable`() {
        val codec = SealedHousingLegacyOutputCodec()
        val failure = assertIs<SnapshotCodecResult.Failure>(codec.encode(auditValue()))
        assertEquals(SnapshotCodecError.INVALID_VALUE, failure.error)
        val small = IndependentModelFixtures.SealedHousingLegacyOutputFull()
        val encoded = assertIs<SnapshotCodecResult.Success<SnapshotPayload>>(codec.encode(small)).value
        assertEquals(small, assertIs<SnapshotCodecResult.Success<*>>(codec.decode(encoded)).value)
    }
}

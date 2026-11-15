package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.models.finnedhousing.legacy.FinnedHousingLegacyTestFixtures
import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LegacyCalculationsTest {
    private fun input() = FinnedHousingLegacyTestFixtures.excelInput()

    @Test
    fun `asymmetric wall gaps exercise every wall index and the area pairing in O22`() {
        val original = input()
        val gaps = listOf(0.002, 0.005, 0.007, 0.009, 0.02, 0.006)
        val request = original.copy(internalProbes = original.internalProbes.copy(
            wallGaps = original.internalProbes.wallGaps.mapIndexed { index, wall ->
                wall.copy(gap = Quantity(gaps[index], StandardUnits.METER)) }))
        val result = LegacyInternalHeatTransfer.calculate(request, LegacyGeometry.calculate(request),
            LegacyFinalCalculations.enclosureTemperature(request))
        val expected = listOf(8.30768152679699, 9.060339864272896, 8.70549392403857,
            9.942029518255415, 6.325540326556013, 9.881521404691302)
        expected.zip(result.wallConvectionCoefficients).forEach { (value, coefficient) ->
            assertEquals(value, coefficient.baseValue, 1e-11)
        }
        assertEquals(4.034151287365713, result.zoneToEnclosureResistance.value.baseValue, 1e-11)
    }

    @Test
    fun `all nine geometry areas reproduce the recorded source cells`() {
        val geometry = LegacyGeometry.calculate(input())
        val actual = listOf(geometry.finnedSurfaceArea, geometry.smoothSideArea, geometry.smoothFaceArea,
            geometry.smoothSurfaceArea, geometry.originalEnclosureArea, geometry.heatedZoneArea,
            geometry.heatedZoneFaceAreaXY, geometry.heatedZoneFaceAreaXZ, geometry.heatedZoneFaceAreaYZ)
        val expected = listOf(0.04418166, 0.006669, 0.009438, 0.032214, 0.034398,
            0.0164515, 0.007728, 0.000308, 0.00018975)
        expected.zip(actual).forEach { (value, quantity) -> assertEquals(value, quantity.baseValue, 1e-14) }
    }

    @Test
    fun `all eleven values of each external probe reproduce rows five through seven`() {
        val request = input()
        val probes = LegacyExternalHeatTransfer.calculate(request, LegacyGeometry.calculate(request)).probes
        val expected = listOf(
            listOf(315.5, 6.41078066349, 3.7495393020134467, 0.0064313453685622569,
                0.53814972979124098, 315.30925135104377, 316.65462567552186,
                0.62433466327547993, 3.8135277990243774, 1.9106697764872866, 2.6168833890241157),
            listOf(318.0, 6.56559777384, 4.4449680784799748, 0.0075761056686542167,
                0.61905895200822547, 316.80941047991774, 319.90470523995884,
                0.64380543430232873, 4.6777207818074205, 1.6954663373281265, 5.898082303278831),
            listOf(320.5, 6.72368590719, 4.9039862183896208, 0.0083059199564965921,
                0.66488890276449153, 318.02666645853265, 323.01333322926632,
                0.66285256298595452, 5.2501246590473976, 1.572774693625024, 9.5372846859756581)
        )
        assertEquals(3, probes.size)
        probes.zip(expected).forEach { (probe, reference) ->
            val actual = listOf(probe.smoothMeanTemperature.kelvin, probe.smoothRadiationCoefficient.baseValue,
                probe.smoothConvectionCoefficient.baseValue, probe.channelParameter, probe.channelCorrection,
                probe.channelAirTemperature.kelvin, probe.finnedMeanTemperature.kelvin,
                probe.finnedRadiationCoefficient.baseValue, probe.finnedConvectionCoefficient.baseValue,
                probe.enclosureToAmbientResistance.value.baseValue, probe.dissipatedPower.baseValue)
            reference.zip(actual).forEach { (value, result) -> assertEquals(value, result, 1e-10) }
        }
    }

    @Test
    fun `all six wall coefficients reproduce E16 through E21`() {
        val request = input()
        val geometry = LegacyGeometry.calculate(request)
        val result = LegacyInternalHeatTransfer.calculate(request, geometry, LegacyFinalCalculations.enclosureTemperature(request))
        val expected = listOf(8.2060136118562141, 8.7045481038321526, 8.2060136118562141,
            8.7045481038321526, 6.7061797654132542, 9.7846391872624601)
        assertEquals(6, result.wallConvectionCoefficients.size)
        expected.zip(result.wallConvectionCoefficients).forEach { (value, coefficient) ->
            assertEquals(value, coefficient.baseValue, 1e-11)
        }
    }

    @Test
    fun `internal radiation mean and resistance reproduce their source cells`() {
        val request = input()
        val result = LegacyInternalHeatTransfer.calculate(request, LegacyGeometry.calculate(request),
            LegacyFinalCalculations.enclosureTemperature(request))
        assertEquals(320.63098423378119, result.enclosureTemperature.kelvin, 1e-11)
        assertEquals(360.63098423378119, result.referenceZoneTemperature.kelvin, 1e-11)
        assertEquals(340.63098423378119, result.meanGapTemperature.kelvin, 1e-11)
        assertEquals(0.76737666355170309, result.reducedEmissivity, 1e-14)
        assertEquals(6.9023773514084903, result.radiationCoefficient.baseValue, 1e-11)
        assertEquals(4.0084708021437319, result.zoneToEnclosureResistance.value.baseValue, 1e-11)
    }

    @Test
    fun `internal trial powers use the manual resistances instead of O22`() {
        val request = input()
        val result = LegacyInternalHeatTransfer.calculate(request, LegacyGeometry.calculate(request),
            LegacyFinalCalculations.enclosureTemperature(request))
        val temperatures = listOf(330.63098423378119, 340.63098423378119, 360.63098423378119)
        val powers = listOf(1.9780033548316769, 4.4014290166949284, 9.9514808932628096)
        assertEquals(3, result.trialResults.size)
        result.trialResults.forEachIndexed { index, trial ->
            assertEquals(temperatures[index], trial.zoneTemperature.kelvin, 1e-11)
            assertEquals(powers[index], trial.dissipatedPower.baseValue, 1e-12)
            assertEquals(request.internalProbes.trialZoneRises[index], trial.zoneRise)
            assertEquals(request.internalProbes.trialResistances[index], trial.resistance)
        }
    }

    @Test
    fun `nine final outputs reproduce AC12 AC13 AC25 AC26 and AA28 through AA32`() {
        val request = input()
        val result = LegacyFinalCalculations.calculate(request, LegacyGeometry.calculate(request))
        val expected = listOf(7.6309842337811995, 1.774647496228186, 27.144910812935397,
            6.312769956496604, 320.63098423378119, 340.14491081293539,
            330.38794752335832, 341.03115170746338, 347.90615170746338)
        val actual = listOf(result.enclosureRise.kelvin, result.enclosureToAmbientResistance.value.baseValue,
            result.heatedZoneRise.kelvin, result.heatedZoneToAmbientResistance.value.baseValue,
            result.enclosureTemperature.kelvin, result.heatedZoneTemperature.kelvin,
            result.internalAirTemperature.kelvin, result.elementTemperature.kelvin, result.junctionTemperature.kelvin)
        expected.zip(actual).forEach { (value, actualValue) -> assertEquals(value, actualValue, 1e-9) }
    }

    @Test
    fun `radiation uses the continuous limit at equal temperatures`() {
        assertEquals(5.51124, LegacyRadiation.coefficient(300.0, 300.0, 0.9, "test"), 1e-12)
    }

    @Test
    fun `radiation avoids overflowing fourth powers when its result is finite`() {
        assertEquals(2.0412e293, LegacyRadiation.coefficient(1e100, 1e100, 0.9, "test"), 2.0412e280)
        assertFailsWith<LegacyCalculationException> {
            LegacyRadiation.coefficient(Double.MAX_VALUE, Double.MAX_VALUE, 0.9, "test")
        }
    }

    @Test
    fun `parallel resistance avoids overflowing total conductance`() {
        assertEquals(1e-309, LegacyThermalResistance.parallelPaths(listOf(1e308 to 10.0), "test"), 1e-322)
        assertFailsWith<LegacyCalculationException> {
            LegacyThermalResistance.parallelPaths(listOf(0.0 to 1.0), "test")
        }
    }

    @Test
    fun `ratio powers retain representable values beyond the range of a raw ratio`() {
        assertEquals(1e150, LegacyArithmetic.ratioPower(1e300, 1e-300, 0.25, "test"), 1e137)
        assertEquals(1e-150, LegacyArithmetic.ratioPower(1e-300, 1e300, 0.25, "test"), 1e-163)
    }

    @Test
    fun `mean supports extremes cancellation subnormal rounding and signed zero`() {
        assertEquals(Double.MAX_VALUE, LegacyArithmetic.mean(Double.MAX_VALUE, Double.MAX_VALUE))
        assertEquals(0.0, LegacyArithmetic.mean(Double.MAX_VALUE, -Double.MAX_VALUE))
        assertEquals(2.0 * Double.MIN_VALUE, LegacyArithmetic.mean(Double.MIN_VALUE, 2.0 * Double.MIN_VALUE))
        assertEquals((-0.0).toRawBits(), LegacyArithmetic.mean(-0.0, -0.0).toRawBits())
    }

    @Test
    fun `negative channel corrections are rejected before a fractional power`() {
        val error = assertFailsWith<LegacyCalculationException> {
            LegacyChannelCorrelation.correction(0.0001206631401231193, "L5")
        }
        assertEquals("L5", error.cell)
    }
}

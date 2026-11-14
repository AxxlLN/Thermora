package com.thermora.models.internalmixing.legacy

import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.models.internalmixing.legacy.internal.LegacyZoneRiseCurve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LegacyZoneRiseCurveTest {

    @Test
    fun `reproduces Excel reference zone rise`() {
        val result = LegacyZoneRiseCurve.calculate(
            totalPowerWatts = 4.3,
            enclosureRise = TemperatureDelta.fromKelvin(
                10.8726142430732
            )
        )

        assertEquals(
            24.282434969840896,
            result.kelvin,
            1e-10
        )
    }

    @Test
    fun `zero enclosure rise preserves empirical contribution`() {
        val result = LegacyZoneRiseCurve.calculate(
            totalPowerWatts = 1.0,
            enclosureRise = TemperatureDelta.fromKelvin(0.0)
        )

        assertEquals(5.2013, result.kelvin, 1e-12)
    }

    @Test
    fun `zone rise increases with total power`() {
        val enclosureRise = TemperatureDelta.fromKelvin(10.0)

        val low = LegacyZoneRiseCurve.calculate(1.0, enclosureRise)
        val high = LegacyZoneRiseCurve.calculate(10.0, enclosureRise)

        assertTrue(high.kelvin > low.kelvin)
    }

    @Test
    fun `rejects zero power`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyZoneRiseCurve.calculate(
                0.0,
                TemperatureDelta.fromKelvin(10.0)
            )
        }
    }

    @Test
    fun `rejects negative power`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyZoneRiseCurve.calculate(
                -1.0,
                TemperatureDelta.fromKelvin(10.0)
            )
        }
    }

    @Test
    fun `rejects non finite power`() {
        for (power in listOf(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )) {
            assertFailsWith<IllegalArgumentException> {
                LegacyZoneRiseCurve.calculate(
                    power,
                    TemperatureDelta.fromKelvin(10.0)
                )
            }
        }
    }
}

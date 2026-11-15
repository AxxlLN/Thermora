package com.thermora.models.externalcooling.legacy.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import com.thermora.foundation.units.temperature.TemperatureDelta

class LegacyHeatedZoneCalculationsTest {

    @Test
    fun `heated zone rise matches Excel AA23`() {
        val enclosureRise =
            LegacyEnclosureRiseCurve.calculate(4.3)

        val result = LegacyHeatedZoneRiseCurve.calculate(
            enclosureRise = enclosureRise,
            totalPowerWatts = 4.3
        )

        assertEquals(
            expected = 26.35835392600632,
            actual = result.kelvin,
            absoluteTolerance = 1e-9
        )
    }

    @Test
    fun `heated zone resistance matches Excel AA24`() {
        val enclosureRise =
            LegacyEnclosureRiseCurve.calculate(4.3)

        val heatedZoneRise =
            LegacyHeatedZoneRiseCurve.calculate(
                enclosureRise = enclosureRise,
                totalPowerWatts = 4.3
            )

        val result = LegacyHeatedZoneResistance.calculate(
            heatedZoneRise = heatedZoneRise,
            totalPowerWatts = 4.3
        )

        assertEquals(
            expected = 6.1298497502340279,
            actual = result.value.baseValue,
            absoluteTolerance = 1e-9
        )
    }

    @Test
    fun `zero total power is rejected`() {
        val enclosureRise =
            TemperatureDelta.fromKelvin(5.0)

        assertFailsWith<IllegalArgumentException> {
            LegacyHeatedZoneRiseCurve.calculate(
                enclosureRise = enclosureRise,
                totalPowerWatts = 0.0
            )
        }
    }

    @Test
    fun `non finite total power is rejected`() {
        val heatedZoneRise =
            TemperatureDelta.fromKelvin(10.0)

        assertFailsWith<IllegalArgumentException> {
            LegacyHeatedZoneResistance.calculate(
                heatedZoneRise = heatedZoneRise,
                totalPowerWatts = Double.NaN
            )
        }
    }
}

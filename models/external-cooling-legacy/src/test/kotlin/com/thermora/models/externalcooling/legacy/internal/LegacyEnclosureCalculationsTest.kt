
package com.thermora.models.externalcooling.legacy.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LegacyEnclosureCalculationsTest {

    @Test
    fun `enclosure rise matches Excel AA11`() {
        val result = LegacyEnclosureRiseCurve.calculate(4.3)

        assertEquals(
            expected = 6.9496599897105886,
            actual = result.kelvin,
            absoluteTolerance = 1e-9
        )
    }

    @Test
    fun `enclosure resistance matches Excel AA12`() {
        val rise = LegacyEnclosureRiseCurve.calculate(4.3)

        val result = LegacyEnclosureResistance.calculate(
            enclosureRise = rise,
            totalPowerWatts = 4.3
        )

        assertEquals(
            expected = 1.6161999976071137,
            actual = result.value.baseValue,
            absoluteTolerance = 1e-9
        )
    }

    @Test
    fun `zero power is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyEnclosureRiseCurve.calculate(0.0)
        }
    }

    @Test
    fun `negative power is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyEnclosureRiseCurve.calculate(-1.0)
        }
    }

    @Test
    fun `non finite power is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyEnclosureRiseCurve.calculate(Double.NaN)
        }
    }
}

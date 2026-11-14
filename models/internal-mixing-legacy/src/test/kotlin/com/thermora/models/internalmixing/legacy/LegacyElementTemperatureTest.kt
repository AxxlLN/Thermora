package com.thermora.models.internalmixing.legacy

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.models.internalmixing.legacy.internal.LegacyElementTemperature
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class LegacyElementTemperatureTest {

    private val ambient = Temperature.fromKelvin(313.0)
    private val zone = Temperature.fromKelvin(337.2824349698409)

    private fun calculateElement(
        elementArea: Double = 0.00423,
        elementPower: Double = 1.25
    ): Temperature =
        LegacyElementTemperature.calculateElement(
            ambientTemperature = ambient,
            heatedZoneRise = zone - ambient,
            heatedZoneArea = BigDecimal(0.0164515),
            elementPowerWatts = elementPower,
            totalPowerWatts = 4.3,
            elementArea = elementArea
        )

    private fun calculateJunction(
        elementPower: Double = 1.25
    ): Temperature =
        LegacyElementTemperature.calculateJunction(
            heatedZoneTemperature = zone,
            elementPowerWatts = elementPower,
            transitionToCaseResistance = 5.0,
            caseToRadiatorResistance = 0.5
        )

    @Test
    fun `reproduces Excel element temperature AB22`() {
        assertEquals(
            338.07522029292744,
            calculateElement().kelvin,
            1e-9
        )
    }

    @Test
    fun `reproduces Excel junction temperature AB23`() {
        assertEquals(
            344.1574349698409,
            calculateJunction().kelvin,
            1e-9
        )
    }

    @Test
    fun `zero element power preserves legacy behavior`() {
        val element = calculateElement(elementPower = 0.0)
        val junction = calculateJunction(elementPower = 0.0)

        assertEquals(
            313.0 + 0.75 * (zone.kelvin - 313.0),
            element.kelvin,
            1e-10
        )

        assertEquals(zone.kelvin, junction.kelvin, 1e-10)
    }

    @Test
    fun `element area affects element temperature`() {
        val smallArea = calculateElement(elementArea = 0.002)
        val largeArea = calculateElement(elementArea = 0.008)

        assertNotEquals(smallArea.kelvin, largeArea.kelvin)
    }

    @Test
    fun `junction temperature is independent of element area`() {
        val junctionBefore = calculateJunction()

        calculateElement(elementArea = 0.002)
        calculateElement(elementArea = 0.008)

        val junctionAfter = calculateJunction()

        assertEquals(junctionBefore.kelvin, junctionAfter.kelvin)
    }

    @Test
    fun `junction uses zone temperature instead of element temperature`() {
        val element = calculateElement()
        val junction = calculateJunction()

        val legacyExpected = zone.kelvin + 5.5 * 1.25
        val alternative = element.kelvin + 5.5 * 1.25

        assertEquals(legacyExpected, junction.kelvin, 1e-10)
        assertNotEquals(alternative, junction.kelvin)
    }

    @Test
    fun `rejects zero element area`() {
        assertFailsWith<IllegalArgumentException> {
            calculateElement(elementArea = 0.0)
        }
    }

    @Test
    fun `rejects negative element power`() {
        assertFailsWith<IllegalArgumentException> {
            calculateJunction(elementPower = -1.0)
        }
    }
}

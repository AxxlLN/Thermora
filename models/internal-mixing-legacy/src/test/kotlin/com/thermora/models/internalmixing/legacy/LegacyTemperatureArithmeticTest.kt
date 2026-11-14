package com.thermora.models.internalmixing.legacy

import com.thermora.foundation.units.temperature.Temperature
import com.thermora.foundation.units.temperature.TemperatureDelta
import com.thermora.models.internalmixing.legacy.internal.LegacyTemperatureArithmetic
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LegacyTemperatureArithmeticTest {

    private fun assertExactMean(first: Double, second: Double) {
        val expected = BigDecimal(first).add(BigDecimal(second)).divide(BigDecimal(2)).toDouble()
        assertEquals(expected, LegacyTemperatureArithmetic.mean(first, second), "$first, $second")
        assertEquals(expected, LegacyTemperatureArithmetic.mean(second, first), "$second, $first")
    }

    @Test
    fun `mean preserves cancellation between extreme opposite values`() {
        assertExactMean(Double.MAX_VALUE, -Double.MAX_VALUE)
        assertExactMean(Double.MAX_VALUE, -1e308)
        assertExactMean(-Double.MAX_VALUE, 1e308)
    }

    @Test
    fun `mean handles extreme values of the same sign`() {
        assertExactMean(Double.MAX_VALUE, Double.MAX_VALUE)
        assertExactMean(-Double.MAX_VALUE, -Double.MAX_VALUE)
        assertExactMean(1e308, 1.7e308)
        assertExactMean(-1e308, -1.7e308)
    }

    @Test
    fun `mean rounds subnormal values without losing the half step`() {
        listOf(1.0, -1.0).forEach { sign ->
            assertExactMean(sign * Double.MIN_VALUE, sign * 2.0 * Double.MIN_VALUE)
            assertExactMean(sign * Double.MIN_VALUE, sign * Double.MIN_VALUE)
            assertExactMean(0.0, sign * Double.MIN_VALUE)
            assertExactMean(sign * java.lang.Double.MIN_NORMAL, sign * Double.MIN_VALUE)
        }
        assertExactMean(Double.MIN_VALUE, -Double.MIN_VALUE)
    }

    @Test
    fun `mean preserves signed zero and rejects every nonfinite argument`() {
        assertEquals((-0.0).toRawBits(), LegacyTemperatureArithmetic.mean(-0.0, -0.0).toRawBits())
        assertEquals(0.0.toRawBits(), LegacyTemperatureArithmetic.mean(-0.0, 0.0).toRawBits())
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { LegacyTemperatureArithmetic.mean(value, 313.0) }
            assertFailsWith<IllegalArgumentException> { LegacyTemperatureArithmetic.mean(313.0, value) }
        }
    }

    @Test
    fun `mean matches an exact oracle across deterministic binary64 samples`() {
        val random = Random(71283)
        repeat(2000) {
            val first = Double.fromBits(random.nextLong())
            val second = Double.fromBits(random.nextLong())
            if (first.isFinite() && second.isFinite()) assertExactMean(first, second)
        }
    }

    @Test
    fun `adds temperature rise to ambient temperature`() {
        val ambient = Temperature.fromKelvin(313.0)
        val rise = TemperatureDelta.fromKelvin(24.282434969840896)

        val result = LegacyTemperatureArithmetic.addRise(
            ambient,
            rise
        )

        assertEquals(
            337.2824349698409,
            result.kelvin,
            1e-12
        )
    }

    @Test
    fun `calculates mean internal air temperature`() {
        val enclosure = Temperature.fromKelvin(323.8726142430732)
        val zone = Temperature.fromKelvin(337.2824349698409)

        val result = LegacyTemperatureArithmetic.meanTemperature(
            enclosure,
            zone
        )

        assertEquals(
            330.577524606457,
            result.kelvin,
            1e-12
        )
    }

    @Test
    fun `calculates mean independently of argument order`() {
        val first = 323.0
        val second = 337.0

        assertEquals(
            LegacyTemperatureArithmetic.mean(first, second),
            LegacyTemperatureArithmetic.mean(second, first)
        )
    }

    @Test
    fun `avoids overflow when averaging large temperatures`() {
        val result = LegacyTemperatureArithmetic.mean(
            1e308,
            1e308
        )

        assertEquals(1e308, result)
    }

    @Test
    fun `preserves equal temperatures`() {
        val result = LegacyTemperatureArithmetic.mean(
            313.0,
            313.0
        )

        assertEquals(313.0, result)
    }

    @Test
    fun `rejects non finite temperatures`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyTemperatureArithmetic.mean(
                Double.POSITIVE_INFINITY,
                313.0
            )
        }
    }

    @Test
    fun `rejects absolute temperature overflow`() {
        assertFailsWith<IllegalArgumentException> {
            LegacyTemperatureArithmetic.addRise(
                Temperature.fromKelvin(Double.MAX_VALUE),
                TemperatureDelta.fromKelvin(Double.MAX_VALUE)
            )
        }
    }
}

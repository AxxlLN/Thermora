package com.thermora.infrastructure.serialization.json

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictJsonNumberBoundaryTest {
    @Test
    fun `zero with an exponent outside BigDecimal range remains zero`() {
        for (token in listOf("0e2147483648", "0e-2147483649", "0.00e999999999999999999999999")) {
            assertEquals(0.0.toRawBits(), StrictJsonNumber.parseDouble(token).toRawBits(), token)
        }
    }

    @Test
    fun `negative zero with an extreme exponent retains its sign`() {
        for (token in listOf("-0e2147483648", "-0.00e-999999999999999999999999")) {
            assertEquals((-0.0).toRawBits(), StrictJsonNumber.parseDouble(token).toRawBits(), token)
        }
    }
}

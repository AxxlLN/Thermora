package com.thermora.foundation.units.dimension

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PhysicalDimensionTest {

    @Test
    fun `should calculate area dimension`() {
        assertEquals(
            PhysicalDimension(length = 2),
            PhysicalDimension.AREA
        )
    }

    @Test
    fun `should calculate volume dimension`() {
        assertEquals(
            PhysicalDimension(length = 3),
            PhysicalDimension.VOLUME
        )
    }

    @Test
    fun `should calculate velocity dimension`() {
        assertEquals(
            PhysicalDimension(length = 1, time = -1),
            PhysicalDimension.VELOCITY
        )
    }

    @Test
    fun `should calculate power dimension`() {
        assertEquals(
            PhysicalDimension(length = 2, mass = 1, time = -3),
            PhysicalDimension.POWER
        )
    }

    @Test
    fun `should calculate energy dimension`() {
        assertEquals(
            PhysicalDimension(length = 2, mass = 1, time = -2),
            PhysicalDimension.ENERGY
        )
    }

    @Test
    fun `should calculate pressure dimension`() {
        assertEquals(
            PhysicalDimension(length = -1, mass = 1, time = -2),
            PhysicalDimension.PRESSURE
        )
    }

    @Test
    fun `should return dimensionless after division`() {
        assertEquals(
            PhysicalDimension.DIMENSIONLESS,
            PhysicalDimension.LENGTH / PhysicalDimension.LENGTH
        )
    }

    @Test
    fun `should reject multiplication overflow`() {
        val first = PhysicalDimension(length = Int.MAX_VALUE)
        val second = PhysicalDimension(length = 1)

        assertFailsWith<ArithmeticException> {
            first * second
        }
    }

    @Test
    fun `should reject division overflow`() {
        val first = PhysicalDimension(length = Int.MIN_VALUE)
        val second = PhysicalDimension(length = 1)

        assertFailsWith<ArithmeticException> {
            first / second
        }
    }
}
package com.thermora.foundation.domain.ventilation

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertFailsWith

class FanSpecificationTest {

    @Test
    fun `should accept valid fan specification`() {
        FanSpecification(
            nominalVolumeFlowRate = Quantity(
                2.05 * 0.00047194745,
                StandardUnits.CUBIC_METER_PER_SECOND
            ),
            characteristicDimension = Quantity(
                0.03,
                StandardUnits.METER
            )
        )
    }

    @Test
    fun `should reject non-positive volume flow rate`() {
        assertFailsWith<IllegalArgumentException> {
            FanSpecification(
                nominalVolumeFlowRate = Quantity(
                    0.0,
                    StandardUnits.CUBIC_METER_PER_SECOND
                ),
                characteristicDimension = Quantity(
                    0.03,
                    StandardUnits.METER
                )
            )
        }
    }

    @Test
    fun `should reject incompatible flow dimension`() {
        assertFailsWith<IllegalArgumentException> {
            FanSpecification(
                nominalVolumeFlowRate = Quantity(
                    2.05,
                    StandardUnits.WATT
                ),
                characteristicDimension = Quantity(
                    0.03,
                    StandardUnits.METER
                )
            )
        }
    }

    @Test
    fun `should reject incompatible characteristic dimension`() {
        assertFailsWith<IllegalArgumentException> {
            FanSpecification(
                nominalVolumeFlowRate = Quantity(
                    0.001,
                    StandardUnits.CUBIC_METER_PER_SECOND
                ),
                characteristicDimension = Quantity(
                    0.03,
                    StandardUnits.WATT
                )
            )
        }
    }
}
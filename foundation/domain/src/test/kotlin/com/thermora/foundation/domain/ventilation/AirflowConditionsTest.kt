package com.thermora.foundation.domain.ventilation

import com.thermora.foundation.units.quantity.Quantity
import com.thermora.foundation.units.unit.StandardUnits
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AirflowConditionsTest {

    @Test
    fun `should accept valid airflow velocity`() {
        AirflowConditions(
            velocity = Quantity(
                1.0,
                StandardUnits.METER_PER_SECOND
            )
        )
    }

    @Test
    fun `should accept zero velocity`() {
        AirflowConditions(
            velocity = Quantity(
                0.0,
                StandardUnits.METER_PER_SECOND
            )
        )
    }

    @Test
    fun `should reject negative velocity`() {
        assertFailsWith<IllegalArgumentException> {
            AirflowConditions(
                velocity = Quantity(
                    -1.0,
                    StandardUnits.METER_PER_SECOND
                )
            )
        }
    }

    @Test
    fun `should reject incompatible dimension`() {
        assertFailsWith<IllegalArgumentException> {
            AirflowConditions(
                velocity = Quantity(
                    1.0,
                    StandardUnits.WATT
                )
            )
        }
    }
}
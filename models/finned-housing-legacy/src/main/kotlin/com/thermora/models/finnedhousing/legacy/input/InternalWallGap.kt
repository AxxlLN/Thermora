package com.thermora.models.finnedhousing.legacy.input

import com.thermora.foundation.units.quantity.Quantity

data class InternalWallGap(
    val wallIndex: Int,
    val gap: Quantity,
    val convectionMultiplier: Double
)

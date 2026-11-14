
package com.thermora.models.internalmixing.legacy.internal

import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import java.math.BigDecimal

/**
 * Heated-zone surface area from the original Excel model.
 *
 * Source: "Исходные данные"!V12
 *
 * S = 2 * (L * W + H * fill * (L + W))
 */
internal object LegacyHeatedZoneArea {

    fun calculate(geometry: HeatSourceGeometry): BigDecimal {
        val length = BigDecimal(geometry.length.baseValue)
        val width = BigDecimal(geometry.width.baseValue)
        val height = BigDecimal(geometry.height.baseValue)
        val fill = BigDecimal(geometry.volumeFillCoefficient)

        return length.multiply(width)
            .add(height.multiply(fill).multiply(length.add(width)))
            .multiply(BigDecimal(2))
    }
}

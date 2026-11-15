package com.thermora.models.finnedhousing.legacy.internal

import com.thermora.models.finnedhousing.legacy.input.FinnedHousingLegacyInput
import com.thermora.models.finnedhousing.legacy.output.FinnedHousingGeometryResults
import java.math.BigDecimal

internal object LegacyGeometry {
    fun calculate(input: FinnedHousingLegacyInput): FinnedHousingGeometryResults {
        val length = BigDecimal(input.enclosure.length.baseValue)
        val width = BigDecimal(input.enclosure.width.baseValue)
        val height = BigDecimal(input.enclosure.height.baseValue)
        val finWidth = BigDecimal(input.fins.width.baseValue)
        val finHeight = BigDecimal(input.fins.height.baseValue)
        val finLength = BigDecimal(input.fins.length.baseValue)
        val count = BigDecimal(input.fins.countPerSide)
        val spacing = BigDecimal(input.fins.spacing.baseValue)
        val two = BigDecimal(2)
        val finnedArea = finLength.multiply(count.subtract(BigDecimal.ONE))
            .multiply(two.multiply(finHeight).add(spacing))
            .add(finWidth.multiply(count).multiply(finLength.add(two.multiply(finHeight))))
            .multiply(two)
        val side = length.add(two.multiply(finHeight)).multiply(height)
        val face = length.multiply(width)
        val original = face.add(height.multiply(width.add(length))).multiply(two)
        val zoneLength = BigDecimal(input.heatedZone.length.baseValue)
        val zoneWidth = BigDecimal(input.heatedZone.width.baseValue)
        val zoneHeight = BigDecimal(input.heatedZone.height.baseValue)
        val fill = BigDecimal(input.heatedZone.volumeFillCoefficient)
        val xy = zoneLength.multiply(zoneWidth)
        val xz = zoneLength.multiply(zoneHeight).multiply(fill)
        val yz = zoneWidth.multiply(zoneHeight).multiply(fill)
        return FinnedHousingGeometryResults(
            LegacyArithmetic.area(finnedArea, "Исходные данные!V7"),
            LegacyArithmetic.area(side, "Исходные данные!V8"),
            LegacyArithmetic.area(face, "Исходные данные!V9"),
            LegacyArithmetic.area(side.add(face).multiply(two), "Исходные данные!V10"),
            LegacyArithmetic.area(original, "Исходные данные!V11"),
            LegacyArithmetic.area(xy.add(xz).add(yz).multiply(two), "Исходные данные!V12"),
            LegacyArithmetic.area(xy, "Исходные данные!V13"),
            LegacyArithmetic.area(xz, "Исходные данные!V14"),
            LegacyArithmetic.area(yz, "Исходные данные!V15")
        )
    }
}

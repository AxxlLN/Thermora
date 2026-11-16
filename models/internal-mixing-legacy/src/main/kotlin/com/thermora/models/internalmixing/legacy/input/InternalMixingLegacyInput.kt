
package com.thermora.models.internalmixing.legacy.input

import com.thermora.calculation.api.model.CalculationInput
import com.thermora.foundation.domain.geometry.HeatSourceGeometry
import com.thermora.foundation.domain.thermal.CriticalElementThermalProperties
import com.thermora.foundation.units.quantity.Quantity

/**
 * Input parameters for the internal mixing legacy model.
 *
 * The sealed housing calculation must be performed
 * before this model is executed.
 *
 * The model reproduces the final Excel calculation branch.
 */
data class InternalMixingLegacyInput @JvmOverloads constructor(
    val sealedHousing: SealedHousingBoundarySnapshot,
    val heatedZone: HeatSourceGeometry,
    val criticalElement: CriticalElementThermalProperties,
    val elementWithRadiatorArea: Quantity,
    val heatTransfer: InternalMixingHeatTransferInput? = null
) : CalculationInput

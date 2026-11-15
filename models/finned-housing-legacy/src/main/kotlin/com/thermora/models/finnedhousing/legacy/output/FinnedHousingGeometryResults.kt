package com.thermora.models.finnedhousing.legacy.output

import com.thermora.foundation.units.quantity.Quantity

data class FinnedHousingGeometryResults(
    val finnedSurfaceArea: Quantity,
    val smoothSideArea: Quantity,
    val smoothFaceArea: Quantity,
    val smoothSurfaceArea: Quantity,
    val originalEnclosureArea: Quantity,
    val heatedZoneArea: Quantity,
    val heatedZoneFaceAreaXY: Quantity,
    val heatedZoneFaceAreaXZ: Quantity,
    val heatedZoneFaceAreaYZ: Quantity
)

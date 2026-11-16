import com.thermora.application.core.ThermoraCoreFactory
import com.thermora.calculation.api.metadata.CalculationModelId
import com.thermora.foundation.common.result.CalculationResult
import com.thermora.models.sealedhousing.legacy.input.*
import com.thermora.models.sealedhousing.legacy.output.*
import com.thermora.models.internalmixing.legacy.input.*
import com.thermora.models.internalmixing.legacy.output.*
import com.thermora.models.externalcooling.legacy.input.*
import com.thermora.models.externalcooling.legacy.output.*

fun calculateSealedHousingTrace(input: SealedHousingLegacyInput, trace: SealedHousingHeatTransferInput): CalculationResult<SealedHousingLegacyOutput> =
    ThermoraCoreFactory.createDefault().calculate(CalculationModelId("sealed-housing-legacy"), input.copy(heatTransfer=trace), SealedHousingLegacyOutput::class)
fun SealedHousingGeometry(output: SealedHousingLegacyOutput): SealedHousingGeometryResults? = output.geometryResults
fun SealedHousingOldDestructuring(output: SealedHousingLegacyOutput): Double {
    val (enclosure, zone, air, element, junction) = output
    return enclosure.kelvin + zone.kelvin + air.kelvin + element.kelvin + junction.kelvin
}

fun calculateInternalMixingTrace(input: InternalMixingLegacyInput, trace: InternalMixingHeatTransferInput): CalculationResult<InternalMixingLegacyOutput> =
    ThermoraCoreFactory.createDefault().calculate(CalculationModelId("internal-mixing-legacy"), input.copy(heatTransfer=trace), InternalMixingLegacyOutput::class)
fun InternalMixingGeometry(output: InternalMixingLegacyOutput): InternalMixingGeometryResults? = output.geometryResults
fun InternalMixingOldDestructuring(output: InternalMixingLegacyOutput): Double {
    val (enclosure, zone, air, element, junction) = output
    return enclosure.kelvin + zone.kelvin + air.kelvin + element.kelvin + junction.kelvin
}

fun calculateExternalCoolingTrace(input: ExternalCoolingLegacyInput, trace: ExternalCoolingHeatTransferInput): CalculationResult<ExternalCoolingLegacyOutput> =
    ThermoraCoreFactory.createDefault().calculate(CalculationModelId("external-cooling-legacy"), input.copy(heatTransfer=trace), ExternalCoolingLegacyOutput::class)
fun ExternalCoolingGeometry(output: ExternalCoolingLegacyOutput): ExternalCoolingGeometryResults? = output.geometryResults
fun ExternalCoolingOldDestructuring(output: ExternalCoolingLegacyOutput): Double {
    val (enclosure, zone, air, element, junction) = output
    return enclosure.kelvin + zone.kelvin + air.kelvin + element.kelvin + junction.kelvin
}

fun sealedTransfer(output: SealedHousingLegacyOutput): SealedHousingInternalHeatTransferResults? = output.internalHeatTransfer
fun mixingTransfer(output: InternalMixingLegacyOutput): InternalMixingHeatTransferResults? = output.heatTransferResults
fun externalTransfer(output: ExternalCoolingLegacyOutput): ExternalCoolingExternalHeatTransferResults? = output.externalHeatTransfer

package com.keizuka.vapeconfig.core.calculator

/** Theoretical estimate only; it does not account for every real-world variation. */
data class CoilEstimate(val resistanceOhms: Double, val isEstimate: Boolean = true)

enum class WireMaterial(val resistivityOhmMeters: Double) {
    KANTHAL_A1(1.45e-6),
    NICHROME_80(1.09e-6),
    STAINLESS_316L(0.75e-6),
}

data class CoilParameters(
    val material: WireMaterial,
    val wireDiameterMm: Double,
    val innerDiameterMm: Double,
    val wraps: Int,
    val legLengthMm: Double,
    val coilCount: Int,
)

fun estimateCoilResistance(parameters: CoilParameters): CoilEstimate {
    require(parameters.wireDiameterMm > 0.0) { "Wire diameter must be positive." }
    require(parameters.innerDiameterMm > 0.0) { "Coil diameter must be positive." }
    require(parameters.wraps in 1..100) { "Wrap count must be between 1 and 100." }
    require(parameters.legLengthMm >= 0.0) { "Leg length cannot be negative." }
    require(parameters.coilCount in 1..8) { "Coil count must be between 1 and 8." }

    val wireDiameterMeters = parameters.wireDiameterMm / 1000.0
    val sectionArea = Math.PI * (wireDiameterMeters / 2.0) * (wireDiameterMeters / 2.0)
    val meanTurnDiameterMeters = (parameters.innerDiameterMm + parameters.wireDiameterMm) / 1000.0
    val lengthPerCoilMeters =
        parameters.wraps * Math.PI * meanTurnDiameterMeters +
            (2.0 * parameters.legLengthMm / 1000.0)
    val resistance = parameters.material.resistivityOhmMeters * lengthPerCoilMeters / sectionArea
    return CoilEstimate(resistance / parameters.coilCount)
}

enum class BaseCarrier { PG, VG }

data class LiquidMixParameters(
    val targetVolumeMl: Double,
    val targetNicotineMgMl: Double,
    val nicotineBaseMgMl: Double,
    val flavorPercent: Double,
    val targetVgPercent: Double,
    val nicotineCarrier: BaseCarrier,
    val flavorCarrier: BaseCarrier,
)

data class LiquidMixResult(
    val nicotineBaseMl: Double,
    val flavorMl: Double,
    val pgMl: Double,
    val vgMl: Double,
) {
    val totalMl: Double get() = nicotineBaseMl + flavorMl + pgMl + vgMl
}

/** Calculates ingredient volumes only, based on declared concentrations and carriers. */
fun calculateLiquidMix(parameters: LiquidMixParameters): LiquidMixResult {
    require(parameters.targetVolumeMl > 0.0) { "Target volume must be positive." }
    require(parameters.nicotineBaseMgMl > 0.0) { "Nicotine base strength must be positive." }
    require(parameters.targetNicotineMgMl >= 0.0) { "Target nicotine cannot be negative." }
    require(parameters.targetNicotineMgMl <= parameters.nicotineBaseMgMl) {
        "Target nicotine cannot exceed the base strength."
    }
    require(parameters.flavorPercent in 0.0..100.0) { "Flavor percentage must be between 0 and 100." }
    require(parameters.targetVgPercent in 0.0..100.0) { "VG percentage must be between 0 and 100." }

    val nicotineMl = parameters.targetVolumeMl * parameters.targetNicotineMgMl / parameters.nicotineBaseMgMl
    val flavorMl = parameters.targetVolumeMl * parameters.flavorPercent / 100.0
    val targetPgMl = parameters.targetVolumeMl * (100.0 - parameters.targetVgPercent) / 100.0
    val targetVgMl = parameters.targetVolumeMl - targetPgMl
    val pgFromBases = (if (parameters.nicotineCarrier == BaseCarrier.PG) nicotineMl else 0.0) +
        (if (parameters.flavorCarrier == BaseCarrier.PG) flavorMl else 0.0)
    val vgFromBases = (if (parameters.nicotineCarrier == BaseCarrier.VG) nicotineMl else 0.0) +
        (if (parameters.flavorCarrier == BaseCarrier.VG) flavorMl else 0.0)
    val pgMl = targetPgMl - pgFromBases
    val vgMl = targetVgMl - vgFromBases
    require(pgMl >= -0.0001 && vgMl >= -0.0001) {
        "These ingredients do not fit the requested PG/VG ratio."
    }
    return LiquidMixResult(nicotineMl, flavorMl, pgMl.coerceAtLeast(0.0), vgMl.coerceAtLeast(0.0))
}

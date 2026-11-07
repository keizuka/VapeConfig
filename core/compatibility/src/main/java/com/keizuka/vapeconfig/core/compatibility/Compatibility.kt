package com.keizuka.vapeconfig.core.compatibility

enum class CompatibilityStatus { COMPATIBLE, INCOMPATIBLE, UNKNOWN }
data class CompatibilityResult(val status: CompatibilityStatus)

/** Checks connector text only; it does not establish full device safety or fit. */
fun checkConnectorCompatibility(deviceConnector: String, atomizerConnector: String): CompatibilityResult {
    val device = deviceConnector.normalizeConnector()
    val atomizer = atomizerConnector.normalizeConnector()
    if (device.isBlank() || atomizer.isBlank()) return CompatibilityResult(CompatibilityStatus.UNKNOWN)
    return CompatibilityResult(if (device == atomizer) CompatibilityStatus.COMPATIBLE else CompatibilityStatus.INCOMPATIBLE)
}
private fun String.normalizeConnector(): String = trim().lowercase().filterNot { it.isWhitespace() }

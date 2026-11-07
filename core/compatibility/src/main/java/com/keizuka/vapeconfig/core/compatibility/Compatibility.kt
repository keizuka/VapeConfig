package com.keizuka.vapeconfig.core.compatibility

enum class CompatibilityStatus { COMPATIBLE, INCOMPATIBLE, UNKNOWN }

data class CompatibilityResult(val status: CompatibilityStatus, val explanation: String)

package com.keizuka.vapeconfig.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VapeDarkColors = darkColorScheme(
    primary = Color(0xFF8BD5CA),
    onPrimary = Color(0xFF003731),
    secondary = Color(0xFFB4C9C4),
    background = Color(0xFF101413),
    surface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFF29312E),
    onSurface = Color(0xFFE0E3E1),
)

@Composable
fun VapeConfigTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VapeDarkColors, content = content)
}

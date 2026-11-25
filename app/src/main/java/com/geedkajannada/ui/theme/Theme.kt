package com.geedkajannada.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Scheme: ColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = White,
    primaryContainer = CreamPanel,
    onPrimaryContainer = Indigo,
    secondary = IndigoLight,
    onSecondary = White,
    tertiary = Sun,
    onTertiary = BrownInk,
    background = Sand,
    onBackground = BrownInk,
    surface = CreamPanel,
    onSurface = BrownInk,
    surfaceVariant = CreamPanel,
    onSurfaceVariant = BrownMid,
    outline = Line,
    outlineVariant = Line,
    error = ErrorRed,
    onError = White,
    surfaceTint = Indigo,
)

@Composable
fun GeedkaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}

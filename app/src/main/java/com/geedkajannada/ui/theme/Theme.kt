package com.geedkajannada.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Scheme: ColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = White,
    primaryContainer = Peach,
    onPrimaryContainer = Emerald,
    secondary = Leaf,
    onSecondary = White,
    tertiary = Gold,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = Peach,
    onSurfaceVariant = Bark,
    outline = Line,
    outlineVariant = Line,
    error = ErrorRed,
    onError = White,
    surfaceTint = Emerald,
)

@Composable
fun GeedkaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}

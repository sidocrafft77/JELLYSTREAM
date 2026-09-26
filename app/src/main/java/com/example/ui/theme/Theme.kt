package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JellyDarkColorScheme = darkColorScheme(
    primary = JellyPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = JellyPurpleSecondary.copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,
    secondary = JellyCyan,
    onSecondary = Color.Black,
    secondaryContainer = JellyCyan.copy(alpha = 0.25f),
    onSecondaryContainer = Color.White,
    tertiary = JellyPink,
    onTertiary = Color.White,
    background = JellyDarkBg,
    onBackground = TextPrimary,
    surface = JellySurface,
    onSurface = TextPrimary,
    surfaceVariant = JellySurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = JellyBorder,
    outlineVariant = JellyBorder.copy(alpha = 0.6f)
)

@Composable
fun JellyStreamTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JellyDarkColorScheme,
        typography = Typography,
        content = content
    )
}

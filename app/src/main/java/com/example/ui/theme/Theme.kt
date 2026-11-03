package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YupiDarkColorScheme = darkColorScheme(
    primary = YupiPurpleLight,
    onPrimary = Color.Black,
    primaryContainer = YupiPurpleDark,
    onPrimaryContainer = YupiTextPrimary,
    secondary = YupiElectricBlue,
    onSecondary = Color.Black,
    secondaryContainer = YupiBlueVibrant,
    onSecondaryContainer = YupiTextPrimary,
    tertiary = YupiNeonCyan,
    onTertiary = Color.Black,
    background = YupiBackground,
    onBackground = YupiTextPrimary,
    surface = YupiSurface,
    onSurface = YupiTextPrimary,
    surfaceVariant = YupiSurfaceVariant,
    onSurfaceVariant = YupiTextSecondary,
    outline = YupiBorder,
    outlineVariant = YupiBorderSubtle
)

// YupiForge is dark theme by default as specified in requirements
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YupiDarkColorScheme,
        typography = Typography,
        content = content
    )
}

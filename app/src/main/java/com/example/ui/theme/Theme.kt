package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AngoDarkColorScheme = darkColorScheme(
    primary = AngoRed,
    onPrimary = AngoWhite,
    primaryContainer = AngoRedDark,
    onPrimaryContainer = AngoWhite,
    secondary = AngoGold,
    onSecondary = AngoBlack,
    secondaryContainer = AngoGoldDark,
    onSecondaryContainer = AngoWhite,
    tertiary = AngoRedLight,
    background = AngoBlack,
    onBackground = AngoWhite,
    surface = AngoSurface,
    onSurface = AngoWhite,
    surfaceVariant = AngoSurfaceVariant,
    onSurfaceVariant = AngoTextSecondary,
    outline = AngoDivider,
    error = AngoRedLight
)

@Composable
fun ANGOFLYTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AngoDarkColorScheme,
        typography = Typography,
        content = content
    )
}

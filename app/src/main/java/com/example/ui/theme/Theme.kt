package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BoBLightColorScheme = lightColorScheme(
    primary = ChampagneGold,
    onPrimary = Color.White,
    primaryContainer = SlateElevated,
    onPrimaryContainer = TextPrimary,
    secondary = EnergyCyan,
    onSecondary = Color.White,
    secondaryContainer = SlateElevated,
    onSecondaryContainer = EnergyCyan,
    tertiary = EnergyCyan,
    onTertiary = Color.White,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateElevated,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun BoBStoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BoBLightColorScheme,
        typography = Typography,
        content = content
    )
}

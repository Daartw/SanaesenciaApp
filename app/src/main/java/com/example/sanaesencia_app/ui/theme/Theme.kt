package com.example.sanaesencia_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimarySana,
    onPrimary = Color.White,
    secondary = AccentGoldSana,
    background = BackgroundWarmSana,
    surface = SurfaceMintSana,
    onBackground = TextDarkSana,
    onSurface = TextDarkSana,
    error = SanaError
)

private val DarkColors = darkColorScheme(
    primary = AccentGoldSana,
    secondary = PrimarySana,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFECECEC),
    onSurface = Color(0xFFECECEC),
    error = SanaError
)

@Composable
fun SanaesenciaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SanaTypography,
        content = content
    )
}
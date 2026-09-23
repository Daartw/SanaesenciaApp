package com.example.sanaesencia_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SanaesenciaGreen,
    onPrimary = Color.White,
    secondary = SanaesenciaPurple,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = SanaError
)

private val DarkColors = darkColorScheme(
    primary = SanaesenciaGreen,
    secondary = SanaesenciaPurple,
    background = Color(0xFF172018),
    surface = Color(0xFF202820),
    onBackground = Color(0xFFE8F0E8),
    onSurface = Color(0xFFE8F0E8),
    error = Color(0xFFFFB4AB)
)

@Composable
fun SanaesenciaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SanaTypography,
        content = content
    )
}

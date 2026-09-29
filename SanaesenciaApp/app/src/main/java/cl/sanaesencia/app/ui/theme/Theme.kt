package cl.sanaesencia.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SanaesenciaColorScheme = lightColorScheme(
    primary = SanaesenciaGreen,
    secondary = SanaesenciaTerracotta,
    background = SanaesenciaBackground,
    surface = SanaesenciaCard,
    onBackground = SanaesenciaText,
    onSurface = SanaesenciaText
)

@Composable
fun SanaesenciaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SanaesenciaColorScheme,
        content = content
    )
}
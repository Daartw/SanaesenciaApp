package cl.sanaesencia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SanaesenciaColorScheme = lightColorScheme(
    primary = SanaesenciaGreen,
    onPrimary = Color.White,
    primaryContainer = SanaesenciaGreenContainer,
    onPrimaryContainer = SanaesenciaOnGreenContainer,
    secondary = SanaesenciaTerracotta,
    onSecondary = Color.White,
    secondaryContainer = SanaesenciaTerracottaContainer,
    onSecondaryContainer = SanaesenciaOnTerracottaContainer,
    tertiary = SanaesenciaTeal,
    onTertiary = Color.White,
    tertiaryContainer = SanaesenciaTealContainer,
    background = SanaesenciaBackground,
    onBackground = SanaesenciaTextPrimary,
    surface = SanaesenciaSurface,
    onSurface = SanaesenciaTextPrimary,
    surfaceVariant = SanaesenciaSurfaceVariant,
    onSurfaceVariant = SanaesenciaTextSecondary,
    outline = SanaesenciaOutline,
    outlineVariant = SanaesenciaOutlineVariant,
)

private val SanaesenciaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun SanaesenciaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SanaesenciaColorScheme,
        shapes = SanaesenciaShapes,
        content = { SanaesenciaBackground(content) },
    )
}

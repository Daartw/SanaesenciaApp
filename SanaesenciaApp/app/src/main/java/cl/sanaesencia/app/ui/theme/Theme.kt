package cl.sanaesencia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFAF7),
    surfaceContainer = Color(0xFFF4F1EB),
    surfaceContainerHigh = Color(0xFFEFEBE4),
)

private val SanaesenciaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

// Jerarquía tipográfica más marcada: títulos firmes, cuerpo legible y etiquetas compactas.
private val Base = Typography()
private val SanaesenciaTypography = Typography(
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.15).sp),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = Base.bodyMedium.copy(lineHeight = 21.sp),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    labelMedium = Base.labelMedium.copy(fontWeight = FontWeight.Medium),
)

@Composable
fun SanaesenciaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SanaesenciaColorScheme,
        shapes = SanaesenciaShapes,
        typography = SanaesenciaTypography,
        content = { SanaesenciaBackground(content) },
    )
}

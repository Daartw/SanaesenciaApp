package cl.sanaesencia.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun SanaesenciaBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            // Elegant, warm, natural background gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAF8F3), // Very soft warm cream
                        Color(0xFFF5F1E8), // Gentle sage tint cream
                        Color(0xFFFAF8F5), // Light warm white
                    ),
                ),
            )

            // Organic, ambient lighting accents for holistic wellness feel
            val width = size.width
            val height = size.height

            // Top-right soft warm amber glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFCDCD1).copy(alpha = 0.35f),
                        Color(0xFFFCDCD1).copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.75f,
                ),
                center = Offset(width * 0.85f, height * 0.12f),
                radius = width * 0.75f,
            )

            // Top-left soft sage green glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD6E8DC).copy(alpha = 0.40f),
                        Color(0xFFD6E8DC).copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.15f, height * 0.35f),
                    radius = width * 0.85f,
                ),
                center = Offset(width * 0.15f, height * 0.35f),
                radius = width * 0.85f,
            )

            // Bottom-right eucalyptus teal ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFCDE8E6).copy(alpha = 0.30f),
                        Color(0xFFCDE8E6).copy(alpha = 0.04f),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.90f, height * 0.80f),
                    radius = width * 0.90f,
                ),
                center = Offset(width * 0.90f, height * 0.80f),
                radius = width * 0.90f,
            )
        }
        content()
    }
}

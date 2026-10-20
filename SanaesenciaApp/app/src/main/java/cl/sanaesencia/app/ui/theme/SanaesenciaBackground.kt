package cl.sanaesencia.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SanaesenciaBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFBCEFFF),
                        Color(0xFFEAF9FF),
                        Color(0xFFF8FCFF),
                    ),
                ),
            )

            val radius = 42f
            val stepX = radius * 1.72f
            val stepY = radius * 1.5f
            var row = 0
            var y = -radius
            while (y < size.height + radius) {
                var x = if (row % 2 == 0) -radius else -radius + stepX / 2f
                while (x < size.width + radius) {
                    val path = hexagonPath(Offset(x, y), radius)
                    drawPath(
                        path = path,
                        color = Color(0xFF56BFEA).copy(alpha = 0.18f),
                        style = Stroke(width = 1.5f),
                    )
                    drawCircle(
                        color = Color(0xFF1597D1).copy(alpha = 0.28f),
                        radius = 3.5f,
                        center = Offset(x, y),
                    )
                    x += stepX
                }
                y += stepY
                row++
            }
        }
        content()
    }
}

private fun hexagonPath(center: Offset, radius: Float): Path {
    val path = Path()
    for (i in 0 until 6) {
        val angle = Math.toRadians((60 * i - 30).toDouble())
        val point = Offset(
            center.x + radius * cos(angle).toFloat(),
            center.y + radius * sin(angle).toFloat(),
        )
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

package cl.sanaesencia.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.domain.model.EstadoCita
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TarjetaBase(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
        }
    }
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    icono: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp),
            )
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/* ===================================================================== */
/* COMPONENTES COMPARTIDOS PARA UNA INTERFAZ CONSISTENTE                 */
/* ===================================================================== */

/**
 * Encabezado de pantalla: título grande, subtítulo opcional y acciones a la derecha.
 * Reemplaza a los TopAppBar planos para que todas las pantallas se vean iguales.
 */
@Composable
fun EncabezadoPantalla(
    titulo: String,
    subtitulo: String? = null,
    modifier: Modifier = Modifier,
    navegacion: (@Composable () -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (navegacion != null) 4.dp else 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navegacion != null) {
            navegacion()
            Spacer(Modifier.width(4.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        acciones()
    }
}

/** Botón "volver" estándar para usar en [EncabezadoPantalla]. */
@Composable
fun BotonVolver(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** Ícono dentro de un círculo de color suave (para tarjetas y listas). */
@Composable
fun IconoCirculo(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

/** Etiqueta tipo "píldora" para mostrar estados (Reservada, Completada, etc.). */
@Composable
fun Pildora(
    texto: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Píldora con el color y texto correctos según el estado de la cita. */
@Composable
fun EstadoCitaPildora(estado: EstadoCita, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (texto, fondo, tinta) = when (estado) {
        EstadoCita.RESERVADA -> Triple("Reservada", colors.tertiaryContainer, colors.tertiary)
        EstadoCita.CONFIRMADA -> Triple("Confirmada", colors.primaryContainer, colors.primary)
        EstadoCita.COMPLETADA -> Triple("Completada", colors.primaryContainer, colors.onPrimaryContainer)
        EstadoCita.CANCELADA -> Triple("Cancelada", colors.surfaceVariant, colors.onSurfaceVariant)
        EstadoCita.NO_ASISTIO -> Triple("No asistió", colors.errorContainer, colors.onErrorContainer)
    }
    Pildora(texto = texto, container = fondo, content = tinta, modifier = modifier)
}

/** Banner de aviso dentro de la pantalla (por ejemplo: "Sin conexión, mostrando datos guardados"). */
@Composable
fun AvisoBanner(
    texto: String,
    modifier: Modifier = Modifier,
    esError: Boolean = false,
    accion: String? = null,
    onAccion: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (esError) colors.errorContainer else colors.secondaryContainer,
        contentColor = if (esError) colors.onErrorContainer else colors.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (esError) Icons.Default.ErrorOutline else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (accion != null && onAccion != null) {
                TextButton(onClick = onAccion) { Text(accion) }
            }
        }
    }
}

/** Fila "ícono + texto" para datos dentro de tarjetas (fecha, hora, modalidad...). */
@Composable
fun DatoConIcono(
    icon: ImageVector,
    texto: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Formatos de fecha/hora en español de Chile para mostrar en pantalla. */
object FormatoFecha {
    private val locale = Locale("es", "CL")
    private val largo = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)
    private val corto = DateTimeFormatter.ofPattern("EEE d MMM", locale)

    /** "2026-11-03" → "Martes 3 de noviembre" */
    fun largo(iso: String): String = runCatching {
        LocalDate.parse(iso.take(10)).format(largo).replaceFirstChar { it.titlecase(locale) }
    }.getOrDefault(iso)

    fun largo(fecha: LocalDate): String = fecha.format(largo).replaceFirstChar { it.titlecase(locale) }

    /** "2026-11-03" → "mar 3 nov" */
    fun corto(fecha: LocalDate): String = fecha.format(corto)

    /** "09:00:00" → "09:00" */
    fun hora(raw: String): String = raw.take(5)
}

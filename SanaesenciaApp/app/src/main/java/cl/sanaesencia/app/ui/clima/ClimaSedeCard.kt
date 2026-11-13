package cl.sanaesencia.app.ui.clima

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.sanaesencia.app.domain.model.PronosticoDia
import cl.sanaesencia.app.presentation.ClimaViewModel
import cl.sanaesencia.app.presentation.UiState
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * IE 3.1.4 — Tarjeta que integra la API externa (Open-Meteo) al flujo visual del inicio
 * del paciente, junto a la información de la sede presencial.
 *
 * Es información complementaria: si el servicio externo falla, la tarjeta muestra un
 * aviso discreto con opción de reintentar y el resto del inicio sigue intacto.
 */
@Composable
fun ClimaSedeCard(
    viewModel: ClimaViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Clima en la sede",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Los Vilos · antes de tu cita presencial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = { viewModel.recargar() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar el clima",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            when (val actual = state) {
                is UiState.Cargando -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                is UiState.Exito -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        actual.datos.take(3).forEach { dia ->
                            DiaPronostico(
                                dia = dia,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    actual.datos.firstOrNull()
                        ?.takeIf { it.recomiendaPrecaucion }
                        ?.let {
                            Text(
                                "Hoy hay ${it.probabilidadLluvia}% de probabilidad de lluvia: " +
                                    "considera salir con tiempo hacia Caupolicán 220.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                }

                is UiState.Vacio -> {
                    Text(
                        "Sin pronóstico disponible por ahora.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is UiState.Error -> {
                    Text(
                        "No se pudo obtener el clima. Toca el botón para reintentar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                "Datos meteorológicos de Open-Meteo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiaPronostico(
    dia: PronosticoDia,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            etiquetaDia(dia.fecha),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            dia.descripcion,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${dia.temperaturaMaxima}° / ${dia.temperaturaMinima}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "${dia.probabilidadLluvia}% lluvia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** "Hoy", "Mañana" o el día de la semana abreviado en español. */
internal fun etiquetaDia(
    fecha: LocalDate,
    hoy: LocalDate = LocalDate.now(),
): String = when (fecha) {
    hoy -> "Hoy"
    hoy.plusDays(1) -> "Mañana"
    else -> fecha.dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale("es", "CL"))
        .replaceFirstChar { it.uppercase() }
}

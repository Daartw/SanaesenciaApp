package cl.sanaesencia.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.AvisoBanner
import cl.sanaesencia.app.ui.common.BotonPrimario
import cl.sanaesencia.app.ui.common.EstadoCitaPildora
import cl.sanaesencia.app.ui.common.FormatoFecha
import cl.sanaesencia.app.ui.common.IconoCirculo
import cl.sanaesencia.app.ui.common.TarjetaBase
import java.time.LocalDate

@Composable
fun ProfessionalHomeScreen(
    vm: AgendaViewModel,
    onAgenda: () -> Unit,
    onProfile: () -> Unit,
    nombre: String = "",
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.cargar() }

    val citas = (state as? UiState.Exito<List<CitaResponse>>)?.datos.orEmpty()
    val hoy = LocalDate.now().toString()
    val activas = citas.filter { it.estado == EstadoCita.RESERVADA || it.estado == EstadoCita.CONFIRMADA }
    val deHoy = activas.filter { it.fecha.take(10) == hoy }
    val completadas = citas.count { it.estado == EstadoCita.COMPLETADA }
    val proxima = activas
        .filter { it.fecha.take(10) >= hoy }
        .minWithOrNull(compareBy({ it.fecha }, { it.horaInicio }))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // SALUDO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = FormatoFecha.largo(LocalDate.now()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (nombre.isBlank()) "Hola" else "Hola, ${nombre.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Este es el resumen de tu jornada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onProfile) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // MÉTRICAS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Metrica(Icons.Default.Today, "Hoy", deHoy.size.toString(), Modifier.weight(1f))
            Metrica(Icons.Default.Schedule, "Pendientes", activas.size.toString(), Modifier.weight(1f))
            Metrica(Icons.Default.CheckCircle, "Completadas", completadas.toString(), Modifier.weight(1f))
        }

        // PRÓXIMA ATENCIÓN
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    )
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PRÓXIMA ATENCIÓN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                    when {
                        state is UiState.Cargando -> CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        proxima != null -> {
                            Text(
                                text = "${FormatoFecha.hora(proxima.horaInicio)} hrs",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                text = "${FormatoFecha.largo(proxima.fecha)} · Paciente #${proxima.pacienteId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            )
                        }
                        else -> Text(
                            text = "No tienes atenciones pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }

        (state as? UiState.Error)?.let {
            AvisoBanner(texto = it.mensaje, esError = true, accion = "Reintentar", onAccion = { vm.cargar() })
        }

        // CITAS DE HOY
        if (deHoy.isNotEmpty()) {
            Text("Agenda de hoy", style = MaterialTheme.typography.titleMedium)
            deHoy.sortedBy { it.horaInicio }.forEach { cita ->
                TarjetaBase {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FormatoFecha.hora(cita.horaInicio),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Paciente #${cita.pacienteId}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = cita.modalidad.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        EstadoCitaPildora(cita.estado)
                    }
                }
            }
        }

        // ACCESO A LA AGENDA
        Card(
            onClick = onAgenda,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconoCirculo(Icons.Default.CalendarMonth)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mis horas", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Gestiona tus citas y revisa el historial autorizado de tus pacientes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        BotonPrimario(texto = "Ver agenda completa", onClick = onAgenda, icono = Icons.Default.CalendarMonth)
    }
}

@Composable
private fun Metrica(
    icon: ImageVector,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    TarjetaBase(modifier = modifier) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

package cl.sanaesencia.app.ui.citas

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.presentation.CitasViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.DatoConIcono
import cl.sanaesencia.app.ui.common.EncabezadoPantalla
import cl.sanaesencia.app.ui.common.EstadoCitaPildora
import cl.sanaesencia.app.ui.common.FormatoFecha
import cl.sanaesencia.app.ui.common.PantallaEstado
import cl.sanaesencia.app.ui.common.TarjetaBase
import java.time.LocalDate

@Composable
fun CitasScreen(
    vm: CitasViewModel,
    onNew: () -> Unit,
    onReagendar: (CitaResponse) -> Unit = {},
) {
    val state by vm.citas.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var citaACancelar by remember { mutableStateOf<CitaResponse?>(null) }

    LaunchedEffect(Unit) { vm.cargar() }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            EncabezadoPantalla(
                titulo = "Mis citas",
                subtitulo = "Tus próximas atenciones y tu historial",
                acciones = {
                    FilledTonalButton(
                        onClick = onNew,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.heightIn(min = 44.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Agendar")
                    }
                },
            )

            Box(modifier = Modifier.weight(1f)) {
                PantallaEstado(
                    loading = state is UiState.Cargando,
                    error = (state as? UiState.Error)?.mensaje,
                    empty = state is UiState.Vacio,
                    onRetry = { vm.cargar() },
                    emptyTitle = "Aún no tienes citas",
                    emptyMessage = "Elige un profesional y reserva tu primera hora en minutos.",
                    emptyIcon = Icons.Default.EventAvailable,
                    emptyAction = "Agendar una cita",
                    onEmptyAction = onNew,
                ) {
                    val citas = (state as? UiState.Exito<List<CitaResponse>>)?.datos.orEmpty()
                    val hoy = LocalDate.now()
                    val (proximas, pasadas) = citas.partition { cita ->
                        val fecha = runCatching { LocalDate.parse(cita.fecha.take(10)) }.getOrNull()
                        (cita.estado == EstadoCita.RESERVADA || cita.estado == EstadoCita.CONFIRMADA) &&
                            (fecha == null || !fecha.isBefore(hoy))
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (proximas.isNotEmpty()) {
                            item(key = "h-proximas") { SeccionLista("Próximas", proximas.size) }
                            items(items = proximas.sortedWith(compareBy({ it.fecha }, { it.horaInicio })), key = { it.id }) { cita ->
                                CitaCard(
                                    cita = cita,
                                    onCancelar = { citaACancelar = cita },
                                    onReagendar = { onReagendar(cita) },
                                    modifier = Modifier.animateContentSize(),
                                )
                            }
                        }
                        if (pasadas.isNotEmpty()) {
                            item(key = "h-historial") { SeccionLista("Historial", pasadas.size) }
                            items(items = pasadas, key = { it.id }) { cita ->
                                CitaCard(cita = cita, onCancelar = null, onReagendar = null)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }

    citaACancelar?.let { cita ->
        AlertDialog(
            onDismissRequest = { citaACancelar = null },
            icon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
            title = { Text("¿Cancelar esta cita?") },
            text = {
                Text(
                    "${FormatoFecha.largo(cita.fecha)} a las ${FormatoFecha.hora(cita.horaInicio)}.\n" +
                        "Recuerda que solo puedes cancelar con al menos 15 horas de anticipación.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.cancelar(cita.id)
                        citaACancelar = null
                    },
                ) {
                    Text("Sí, cancelar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { citaACancelar = null }) { Text("Volver") }
            },
        )
    }
}

@Composable
private fun SeccionLista(titulo: String, cantidad: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = cantidad.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CitaCard(
    cita: CitaResponse,
    onCancelar: (() -> Unit)?,
    onReagendar: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    TarjetaBase(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = FormatoFecha.largo(cita.fecha),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Cita #${cita.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            EstadoCitaPildora(cita.estado)
        }

        DatoConIcono(
            icon = Icons.Default.Schedule,
            texto = "${FormatoFecha.hora(cita.horaInicio)} – ${FormatoFecha.hora(cita.horaFin)}",
        )
        DatoConIcono(
            icon = if (cita.modalidad == ModalidadCita.ONLINE) Icons.Default.Videocam else Icons.Default.MeetingRoom,
            texto = if (cita.modalidad == ModalidadCita.ONLINE) "Atención online" else "Presencial en la sede",
        )

        if (onCancelar != null || onReagendar != null) {
            Text(
                "Puedes reagendar o cancelar hasta 15 horas antes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (onReagendar != null) {
                    FilledTonalButton(
                        onClick = onReagendar,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp),
                    ) {
                        Text("Reagendar")
                    }
                }
                if (onCancelar != null) {
                    OutlinedButton(
                        onClick = onCancelar,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp),
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

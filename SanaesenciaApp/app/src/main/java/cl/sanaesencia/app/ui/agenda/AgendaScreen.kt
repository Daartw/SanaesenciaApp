package cl.sanaesencia.app.ui.agenda

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.FichaClinicaResponse
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.presentation.AgendaViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.DatoConIcono
import cl.sanaesencia.app.ui.common.EncabezadoPantalla
import cl.sanaesencia.app.ui.common.EstadoCitaPildora
import cl.sanaesencia.app.ui.common.FormatoFecha
import cl.sanaesencia.app.ui.common.PantallaEstado
import cl.sanaesencia.app.ui.common.TarjetaBase

@Composable
fun AgendaScreen(
    vm: AgendaViewModel,
    onLogout: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val ficha by vm.ficha.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var showFicha by remember { mutableStateOf(false) }
    var citaACompletar by remember { mutableStateOf<CitaResponse?>(null) }

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
                titulo = "Mis horas",
                subtitulo = "Solo ves las citas asignadas a ti",
                acciones = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )

            Box(modifier = Modifier.weight(1f)) {
                PantallaEstado(
                    loading = state is UiState.Cargando,
                    error = (state as? UiState.Error)?.mensaje,
                    empty = state is UiState.Vacio,
                    onRetry = { vm.cargar() },
                    emptyTitle = "Sin citas asignadas",
                    emptyMessage = "Cuando un paciente reserve contigo, la cita aparecerá aquí.",
                    emptyIcon = Icons.Default.EventNote,
                ) {
                    val citas = (state as? UiState.Exito<List<CitaResponse>>)?.datos.orEmpty()
                    val porDia = citas.groupBy { it.fecha.take(10) }.toSortedMap()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        porDia.forEach { (dia, delDia) ->
                            item(key = "dia-$dia") {
                                Text(
                                    text = FormatoFecha.largo(dia),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                            items(items = delDia.sortedBy { it.horaInicio }, key = { it.id }) { cita ->
                                CitaProfesionalCard(
                                    cita = cita,
                                    onHistorial = {
                                        vm.cargarFicha(cita.pacienteId)
                                        showFicha = true
                                    },
                                    onCompletar = { citaACompletar = cita },
                                    onNoAsistio = { vm.estado(cita.id, EstadoCita.NO_ASISTIO, null) },
                                    modifier = Modifier.animateContentSize(),
                                )
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

    // COMPLETAR CON NOTAS
    citaACompletar?.let { cita ->
        var notas by remember(cita.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { citaACompletar = null },
            title = { Text("Completar atención") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${FormatoFecha.largo(cita.fecha)} · ${FormatoFecha.hora(cita.horaInicio)} hrs · Paciente #${cita.pacienteId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas de la atención (opcional)") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.estado(cita.id, EstadoCita.COMPLETADA, notas.trim().ifBlank { null })
                        citaACompletar = null
                    },
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { citaACompletar = null }) { Text("Cancelar") }
            },
        )
    }

    // HISTORIAL DEL PACIENTE
    if (showFicha) {
        AlertDialog(
            onDismissRequest = { showFicha = false },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            title = { Text("Historial del paciente") },
            text = { FichaContenido(ficha) },
            confirmButton = {
                TextButton(onClick = { showFicha = false }) { Text("Cerrar") }
            },
        )
    }
}

@Composable
private fun FichaContenido(ficha: UiState<List<FichaClinicaResponse>>) {
    when (ficha) {
        is UiState.Cargando -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
        is UiState.Error -> Text(ficha.mensaje, color = MaterialTheme.colorScheme.error)
        is UiState.Vacio -> Text("Sin registros clínicos disponibles.")
        is UiState.Exito -> {
            if (ficha.datos.isEmpty()) {
                Text("Sin registros clínicos disponibles.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ficha.datos.forEachIndexed { index, f ->
                        if (index > 0) HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                FormatoFecha.largo(f.fecha),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            EstadoCitaPildora(f.estado)
                        }
                        Text(
                            f.notasAtencion ?: "Sin notas registradas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CitaProfesionalCard(
    cita: CitaResponse,
    onHistorial: () -> Unit,
    onCompletar: () -> Unit,
    onNoAsistio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendiente = cita.estado == EstadoCita.RESERVADA || cita.estado == EstadoCita.CONFIRMADA

    TarjetaBase(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${FormatoFecha.hora(cita.horaInicio)} – ${FormatoFecha.hora(cita.horaFin)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            EstadoCitaPildora(cita.estado)
        }

        DatoConIcono(Icons.Default.Person, "Paciente #${cita.pacienteId} · Cita #${cita.id}")
        DatoConIcono(
            icon = if (cita.modalidad == ModalidadCita.ONLINE) Icons.Default.Videocam else Icons.Default.MeetingRoom,
            texto = if (cita.modalidad == ModalidadCita.ONLINE) "Online" else "Presencial" + (cita.boxId?.let { " · Box $it" } ?: ""),
        )

        TextButton(
            onClick = onHistorial,
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("Ver historial del paciente")
        }

        if (pendiente) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onNoAsistio,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("No asistió")
                }
                Button(
                    onClick = onCompletar,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Completar")
                }
            }
        }
    }
}

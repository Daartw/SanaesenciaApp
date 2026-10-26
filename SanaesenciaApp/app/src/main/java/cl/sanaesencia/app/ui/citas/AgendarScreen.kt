package cl.sanaesencia.app.ui.citas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.sanaesencia.app.data.remote.CrearCitaRequest
import cl.sanaesencia.app.data.remote.DisponibilidadResponse
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.presentation.CatalogViewModel
import cl.sanaesencia.app.presentation.CitasViewModel
import cl.sanaesencia.app.presentation.UiState
import cl.sanaesencia.app.ui.common.AvisoBanner
import cl.sanaesencia.app.ui.common.BotonPrimario
import cl.sanaesencia.app.ui.common.BotonVolver
import cl.sanaesencia.app.ui.common.EncabezadoPantalla
import cl.sanaesencia.app.ui.common.FormatoFecha
import cl.sanaesencia.app.ui.common.IconoCirculo
import cl.sanaesencia.app.ui.common.TarjetaBase
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun AgendarScreen(
    profesionalId: Long,
    vm: CitasViewModel,
    catalogVm: CatalogViewModel,
    onBack: () -> Unit,
) {
    // Se agenda desde mañana: el backend exige 15 horas de anticipación.
    val primerDia = remember { LocalDate.now().plusDays(1) }
    val dias = remember { (0 until 21).map { primerDia.plusDays(it.toLong()) } }

    var date by remember { mutableStateOf(primerDia) }
    var selected by remember { mutableStateOf<DisponibilidadResponse?>(null) }
    var modalidad by remember { mutableStateOf(ModalidadCita.ONLINE) }

    val slotsState by vm.disponibilidad.collectAsState()
    val message by vm.message.collectAsState()
    val creada by vm.creada.collectAsState()
    val enviando by vm.enviando.collectAsState()
    val profesionales by catalogVm.profesionales.collectAsState(initial = emptyList())
    val profesional = profesionales.firstOrNull { it.id == profesionalId }

    LaunchedEffect(date, profesionalId) {
        selected = null
        vm.disponibilidad(profesionalId, date)
    }

    LaunchedEffect(creada) {
        if (creada) {
            vm.consumirCreada()
            onBack()
        }
    }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(4000)
            vm.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EncabezadoPantalla(
            titulo = "Agendar cita",
            subtitulo = "Elige día, hora y modalidad",
            navegacion = { BotonVolver(onBack) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // PROFESIONAL
            TarjetaBase(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoCirculo(Icons.Default.Person, size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = profesional?.titulo ?: "Profesional #$profesionalId",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = profesional?.categoriaEspecialidad ?: "Sanaesencia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // DÍA
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TituloBloque("Día", FormatoFecha.largo(date))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(dias, key = { it.toEpochDay() }) { dia ->
                        DiaChip(
                            dia = dia,
                            seleccionado = dia == date,
                            onClick = { date = dia },
                        )
                    }
                }
            }

            // HORA
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TituloBloque("Hora", "Bloques de 1 hora · 09:00 a 17:00")

                when (val s = slotsState) {
                    is UiState.Cargando -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }

                    is UiState.Error -> AvisoBanner(
                        texto = s.mensaje,
                        esError = true,
                        accion = "Reintentar",
                        onAccion = { vm.disponibilidad(profesionalId, date) },
                    )

                    is UiState.Vacio -> SinHorarios()

                    is UiState.Exito<List<DisponibilidadResponse>> -> {
                        val disponibles = s.datos.filter { it.disponible }
                        if (disponibles.isEmpty()) {
                            SinHorarios()
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                disponibles.forEach { slot ->
                                    FilterChip(
                                        selected = selected == slot,
                                        onClick = {
                                            selected = slot
                                            runCatching { LocalTime.parse(slot.horaInicio) }
                                                .getOrNull()
                                                ?.let { vm.validarCita(date, it) }
                                        },
                                        label = {
                                            Text(
                                                FormatoFecha.hora(slot.horaInicio),
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        modifier = Modifier.heightIn(min = 44.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // MODALIDAD
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TituloBloque("Modalidad", null)
                val opciones = listOf(ModalidadCita.ONLINE, ModalidadCita.PRESENCIAL)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    opciones.forEachIndexed { index, opcion ->
                        SegmentedButton(
                            selected = modalidad == opcion,
                            onClick = { modalidad = opcion },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = opciones.size),
                            icon = {
                                Icon(
                                    imageVector = if (opcion == ModalidadCita.ONLINE) Icons.Default.Videocam else Icons.Default.MeetingRoom,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 2.dp),
                                )
                            },
                        ) {
                            Text(if (opcion == ModalidadCita.ONLINE) "Online" else "Presencial")
                        }
                    }
                }
                Text(
                    text = if (modalidad == ModalidadCita.ONLINE) {
                        "Recibirás el enlace de la videollamada antes de tu hora."
                    } else {
                        "Te asignaremos un box disponible en la sede de Los Vilos."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            message?.let {
                AvisoBanner(
                    texto = it,
                    esError = true,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        // RESUMEN + CONFIRMAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val slot = selected
                Text(
                    text = if (slot == null) {
                        "Selecciona una hora para continuar"
                    } else {
                        "${FormatoFecha.largo(date)} · ${FormatoFecha.hora(slot.horaInicio)} hrs · " +
                            if (modalidad == ModalidadCita.ONLINE) "Online" else "Presencial"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                BotonPrimario(
                    texto = "Confirmar cita",
                    icono = Icons.Default.EventAvailable,
                    cargando = enviando,
                    enabled = slot != null && vm.validarCitaSilencioso(
                        date,
                        slot.horaInicio.let { runCatching { LocalTime.parse(it) }.getOrNull() },
                    ),
                    onClick = {
                        val s = selected ?: return@BotonPrimario
                        vm.crear(
                            CrearCitaRequest(
                                profesionalId = profesionalId,
                                boxId = null,
                                fecha = date.toString(),
                                horaInicio = s.horaInicio,
                                horaFin = s.horaFin,
                                modalidad = modalidad,
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun TituloBloque(titulo: String, detalle: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (detalle != null) {
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaChip(
    dia: LocalDate,
    seleccionado: Boolean,
    onClick: () -> Unit,
) {
    val locale = Locale("es", "CL")
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) colors.primary else colors.surface,
        contentColor = if (seleccionado) colors.onPrimary else colors.onSurface,
        border = if (seleccionado) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .width(58.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", "").uppercase(locale),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = dia.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = dia.month.getDisplayName(TextStyle.SHORT, locale).replace(".", ""),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun SinHorarios() {
    AvisoBanner(texto = "No quedan horarios disponibles este día. Prueba con otra fecha.")
}

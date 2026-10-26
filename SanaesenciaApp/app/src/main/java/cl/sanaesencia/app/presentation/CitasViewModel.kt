package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.CrearCitaRequest
import cl.sanaesencia.app.data.remote.DisponibilidadResponse
import cl.sanaesencia.app.data.remote.ReagendarCitaRequest
import cl.sanaesencia.app.data.remote.mensajeUsuario
import cl.sanaesencia.app.data.repository.CitaRepository
import cl.sanaesencia.app.domain.usecase.AgendarCitaUseCase
import cl.sanaesencia.app.domain.validacion.ResultadoValidacion
import cl.sanaesencia.app.domain.validacion.ValidadorCita
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CitasViewModel(private val repo: CitaRepository, private val agendar: AgendarCitaUseCase) : BaseViewModel() {
    private val clock = Clock.system(ZoneId.of("America/Santiago"))
    private val validator = ValidadorCita()
    val citas = MutableStateFlow<UiState<List<CitaResponse>>>(UiState.Cargando)
    val disponibilidad = MutableStateFlow<UiState<List<DisponibilidadResponse>>>(UiState.Cargando)
    val message = MutableStateFlow<String?>(null)
    private val _creada = MutableStateFlow(false)
    val creada: StateFlow<Boolean> = _creada.asStateFlow()
    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    fun cargar() = viewModelScope.launch { ejecutar(citas, { it.isEmpty() }) { repo.misCitas() } }

    fun disponibilidad(profesionalId: Long, fecha: LocalDate) = viewModelScope.launch {
        ejecutar(disponibilidad) { repo.disponibilidad(profesionalId, fecha) }
    }

    fun validarCitaSilencioso(fecha: LocalDate, hora: LocalTime?) = hora != null && validator(fecha, hora, clock) is ResultadoValidacion.Valido

    fun validarCita(fecha: LocalDate, hora: LocalTime): Boolean = when (val result = validator(fecha, hora, clock)) {
        ResultadoValidacion.Valido -> true
        is ResultadoValidacion.Invalido -> { message.value = result.mensaje; false }
    }

    fun crear(request: CrearCitaRequest) = viewModelScope.launch {
        if (_enviando.value) return@launch
        _enviando.value = true
        runCatching { agendar(request) }
            .onSuccess { message.value = "Cita reservada correctamente"; _creada.value = true; cargar() }
            .onFailure { message.value = it.mensajeUsuario("No fue posible reservar la cita") }
        _enviando.value = false
    }

    /** RF-08: cambia fecha/hora/modalidad de una cita vigente (respetando las 15 horas). */
    fun reagendar(citaId: Long, request: ReagendarCitaRequest) = viewModelScope.launch {
        if (_enviando.value) return@launch
        _enviando.value = true
        runCatching { repo.reagendar(citaId, request) }
            .onSuccess { message.value = "Cita reagendada correctamente"; _creada.value = true; cargar() }
            .onFailure { message.value = it.mensajeUsuario("No fue posible reagendar la cita") }
        _enviando.value = false
    }

    fun cancelar(id: Long) = viewModelScope.launch {
        runCatching { repo.cancelar(id) }
            .onSuccess { message.value = "Cita cancelada"; cargar() }
            .onFailure { message.value = it.mensajeUsuario("No fue posible cancelar la cita") }
    }

    fun consumirCreada() { _creada.value = false }
    fun clearMessage() { message.value = null }
}

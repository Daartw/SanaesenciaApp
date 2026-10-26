package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.EstadoCitaRequest
import cl.sanaesencia.app.data.remote.FichaClinicaResponse
import cl.sanaesencia.app.data.remote.mensajeUsuario
import cl.sanaesencia.app.data.repository.CitaRepository
import cl.sanaesencia.app.domain.model.EstadoCita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AgendaViewModel(private val repo: CitaRepository) : BaseViewModel() {
    val state = MutableStateFlow<UiState<List<CitaResponse>>>(UiState.Cargando)
    val ficha = MutableStateFlow<UiState<List<FichaClinicaResponse>>>(UiState.Vacio)
    val message = MutableStateFlow<String?>(null)

    fun cargar() = viewModelScope.launch { ejecutar(state, { it.isEmpty() }) { repo.agenda() } }

    fun estado(id: Long, estado: EstadoCita, notas: String?) = viewModelScope.launch {
        runCatching { repo.actualizarEstado(id, EstadoCitaRequest(estado, notas)) }
            .onSuccess {
                message.value = if (estado == EstadoCita.COMPLETADA) "Atención marcada como completada" else "Cita actualizada"
                cargar()
            }
            .onFailure { message.value = it.mensajeUsuario("No fue posible actualizar la cita") }
    }

    fun cargarFicha(pacienteId: Long) = viewModelScope.launch {
        ejecutar(ficha) { repo.historialPaciente(pacienteId) }
    }

    fun clearMessage() { message.value = null }
}

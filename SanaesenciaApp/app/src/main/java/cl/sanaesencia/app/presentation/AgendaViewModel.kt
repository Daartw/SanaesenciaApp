package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.EstadoCitaRequest
import cl.sanaesencia.app.data.remote.FichaClinicaResponse
import cl.sanaesencia.app.data.repository.CitaRepository
import cl.sanaesencia.app.domain.model.EstadoCita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AgendaViewModel(private val repo: CitaRepository) : BaseViewModel() {
    val state = MutableStateFlow<UiState<List<CitaResponse>>>(UiState.Cargando)
    val ficha = MutableStateFlow<List<FichaClinicaResponse>>(emptyList())
    fun cargar() = viewModelScope.launch { ejecutar(state, { it.isEmpty() }) { repo.agenda() } }
    fun estado(id: Long, estado: EstadoCita, notas: String?) = viewModelScope.launch { runCatching { repo.actualizarEstado(id, EstadoCitaRequest(estado, notas)) }.onSuccess { cargar() } }
    fun cargarFicha(pacienteId: Long) = viewModelScope.launch { runCatching { repo.historialPaciente(pacienteId) }.onSuccess { ficha.value = it } }
}

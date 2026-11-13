package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope
import cl.sanaesencia.app.data.repository.ClimaRepository
import cl.sanaesencia.app.domain.model.PronosticoDia
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * IE 3.1.4 — Estado de la tarjeta del clima de la sede.
 *
 * Usa [BaseViewModel.ejecutar], igual que el resto de la app, así que un fallo de la API
 * externa termina en [UiState.Error] con un mensaje legible y jamás en un cierre forzado.
 */
class ClimaViewModel(
    private val repo: ClimaRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<List<PronosticoDia>>>(UiState.Cargando)
    val state: StateFlow<UiState<List<PronosticoDia>>> = _state.asStateFlow()

    /** Pide el pronóstico una sola vez; para recargar a mano se usa [recargar]. */
    fun cargar() {
        if (_state.value is UiState.Exito) return
        recargar()
    }

    fun recargar() = viewModelScope.launch(dispatcher) {
        ejecutar(_state, { it.isEmpty() }) { repo.pronosticoSede() }
    }
}

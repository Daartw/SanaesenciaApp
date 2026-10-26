package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Las listas vienen de la base local (Room), así que se muestran aunque no haya conexión.
 * [state] indica si la última sincronización con el servidor funcionó.
 */
class CatalogViewModel(private val repo: CatalogRepository) : BaseViewModel() {
    val profesionales = repo.profesionales()
    val productos = repo.productos()
    val state = MutableStateFlow<UiState<Unit>>(UiState.Cargando)
    init { refresh() }
    fun refresh() { viewModelScope.launch { ejecutar(state) { repo.refresh() } } }
}

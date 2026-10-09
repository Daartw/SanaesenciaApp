package cl.sanaesencia.app.presentation

import androidx.lifecycle.viewModelScope

import cl.sanaesencia.app.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(private val repo: CatalogRepository) : BaseViewModel() {
    val profesionales = repo.profesionales()
    val productos = repo.productos()
    val state = MutableStateFlow<UiState<Unit>>(UiState.Cargando)
    init { refresh() }
    fun refresh() { viewModelScope.launch { ejecutar(state) { repo.refresh(); Unit } } }
}

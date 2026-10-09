package cl.sanaesencia.app.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel : ViewModel() {
    protected suspend fun <T> ejecutar(
        state: MutableStateFlow<UiState<T>>,
        empty: ((T) -> Boolean)? = null,
        block: suspend () -> T,
    ) {
        state.value = UiState.Cargando
        runCatching { block() }
            .onSuccess { data -> state.value = if (empty?.invoke(data) == true) UiState.Vacio else UiState.Exito(data) }
            .onFailure { state.value = UiState.Error(it.message ?: "Ocurrió un error inesperado") }
    }
}

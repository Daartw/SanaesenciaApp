package cl.sanaesencia.app.presentation

import androidx.lifecycle.ViewModel
import cl.sanaesencia.app.data.remote.mensajeUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel : ViewModel() {
    protected suspend fun <T> ejecutar(
        state: MutableStateFlow<UiState<T>>,
        empty: ((T) -> Boolean)? = null,
        block: suspend () -> T,
    ) {
        state.value = UiState.Cargando
        try {
            val data = block()
            state.value = if (empty?.invoke(data) == true) UiState.Vacio else UiState.Exito(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            state.value = UiState.Error(e.mensajeUsuario())
        }
    }
}

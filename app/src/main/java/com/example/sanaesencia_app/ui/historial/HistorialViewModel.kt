package com.example.sanaesencia_app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesencia_app.data.repository.CitaRepositoryImpl
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.model.EstadoCita
import com.example.sanaesencia_app.domain.repository.CitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistorialViewModel(
    private val repository: CitaRepository = CitaRepositoryImpl()
) : ViewModel() {

    private val _historial = MutableStateFlow<List<Cita>>(emptyList())
    val historial: StateFlow<List<Cita>> = _historial.asStateFlow()

    private val pacienteIdDemo = "P001"

    init {
        viewModelScope.launch {
            repository.getCitasByPaciente(pacienteIdDemo).collect { lista ->
                _historial.value = lista
            }
        }
    }

    // Señal simple de alerta predictiva: si hay una sesión marcada como INACTIVA,
    // se considera al paciente en riesgo de deserción. Este cálculo se hará más
    // robusto cuando se conecte al motor de alertas del backend.
    fun hayRiesgoDesercion(): Boolean =
        _historial.value.any { it.estado == EstadoCita.INACTIVA }
}

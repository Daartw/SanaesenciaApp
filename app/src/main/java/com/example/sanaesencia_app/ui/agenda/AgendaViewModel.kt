package com.example.sanaesencia_app.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesencia_app.data.repository.CitaRepositoryImpl
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.repository.CitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgendaViewModel(
    private val repository: CitaRepository = CitaRepositoryImpl()
) : ViewModel() {

    private val _citas = MutableStateFlow<List<Cita>>(emptyList())
    val citas: StateFlow<List<Cita>> = _citas.asStateFlow()

    // Paciente de prueba fijo mientras no haya login real contra backend
    private val pacienteIdDemo = "P001"

    init {
        cargarCitas()
    }

    private fun cargarCitas() {
        viewModelScope.launch {
            repository.getCitasByPaciente(pacienteIdDemo).collect { lista ->
                _citas.value = lista
            }
        }
    }
}

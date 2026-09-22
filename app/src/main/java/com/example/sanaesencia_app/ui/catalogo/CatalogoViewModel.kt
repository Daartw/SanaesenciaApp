package com.example.sanaesencia_app.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sanaesencia_app.data.repository.CitaRepositoryImpl
import com.example.sanaesencia_app.domain.model.Especialidad
import com.example.sanaesencia_app.domain.repository.CitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogoViewModel(
    // En esta etapa se inyecta el mock directo; más adelante se reemplaza
    // por inyección de dependencias (Hilt) apuntando a Room/Retrofit.
    private val repository: CitaRepository = CitaRepositoryImpl()
) : ViewModel() {

    private val _especialidades = MutableStateFlow<List<Especialidad>>(emptyList())
    val especialidades: StateFlow<List<Especialidad>> = _especialidades.asStateFlow()

    init {
        cargarEspecialidades()
    }

    private fun cargarEspecialidades() {
        viewModelScope.launch {
            repository.getEspecialidades().collect { lista ->
                _especialidades.value = lista
            }
        }
    }
}

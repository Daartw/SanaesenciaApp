package com.example.sanaesencia_app.ui.paciente

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PacienteViewModel(private val repository: com.example.sanaesencia_app.domain.repository.SanaesenciaRepository) : ViewModel() {
    private val _profesionales = MutableStateFlow(repository.profesionales())
    val profesionales: StateFlow<List<Profesional>> = _profesionales.asStateFlow()
    val productos = repository.productos()
    val promociones = repository.promociones()

    var especialidad = MutableStateFlow("Todas")
    var modalidad = MutableStateFlow<ModalidadAtencion?>(null)
    var texto = MutableStateFlow("")
    var experienciaMinima = MutableStateFlow(0)

    private val _filtrados = MutableStateFlow<List<Profesional>>(emptyList())
    val filtrados: StateFlow<List<Profesional>> = _filtrados.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(especialidad, modalidad, texto, experienciaMinima, profesionales) { esp, mod, txt, exp, lista ->
                lista.filter { p ->
                    (esp == "Todas" || p.especialidad == esp) &&
                        (mod == null || mod in p.modalidades) &&
                        (txt.isBlank() || p.nombre.contains(txt, true) || p.carrera.contains(txt, true) || p.certificaciones.any { it.contains(txt, true) } || p.capacitaciones.any { it.contains(txt, true) }) &&
                        p.aniosExperiencia >= exp
                }
            }.collect { _filtrados.value = it }
        }
    }

    fun cambiarEstado(id: String, estado: EstadoTerapeuta) {
        viewModelScope.launch {
            repository.cambiarEstadoProfesional(id, estado)
            _profesionales.value = repository.profesionales()
        }
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PacienteViewModel(AppContainer.repository(context)) as T
        }
    }
}

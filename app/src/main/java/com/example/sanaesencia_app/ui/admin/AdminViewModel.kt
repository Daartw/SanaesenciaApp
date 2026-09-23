package com.example.sanaesencia_app.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: com.example.sanaesencia_app.domain.repository.SanaesenciaRepository) : ViewModel() {
    private val _profesionales = MutableStateFlow(repository.profesionales())
    val profesionales = _profesionales.asStateFlow()
    private val _boxes = MutableStateFlow(repository.boxes())
    val boxes = _boxes.asStateFlow()
    val pacientes get() = repository.pacientesDemo()
    val promociones get() = repository.promociones()

    fun cambiarEstado(id: String, estado: EstadoTerapeuta) = viewModelScope.launch {
        repository.cambiarEstadoProfesional(id, estado)
        _profesionales.value = repository.profesionales()
    }

    fun cambiarBox(numero: Int, estado: EstadoBox) = viewModelScope.launch { repository.cambiarEstadoBox(numero, estado)
        _boxes.value = repository.boxes()
    }

    fun agregarDemo() = viewModelScope.launch {
        val nuevo = Profesional("T${System.currentTimeMillis()}", "Nuevo profesional", "Psicopedagogía", "Psicopedagogía", listOf("Certificación demostrativa"), listOf("Capacitación demostrativa"), 3, EstadoTerapeuta.DISPONIBLE, listOf(ModalidadAtencion.PRESENCIAL, ModalidadAtencion.ONLINE), "Perfil ficticio creado desde administración.")
        repository.agregarProfesional(nuevo)
        _profesionales.value = repository.profesionales()
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminViewModel(AppContainer.repository(context)) as T
        }
    }
}

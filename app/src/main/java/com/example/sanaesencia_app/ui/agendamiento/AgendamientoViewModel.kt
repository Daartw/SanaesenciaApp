package com.example.sanaesencia_app.ui.agendamiento

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.domain.model.*
import com.example.sanaesencia_app.session.DemoSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AgendamientoViewModel(private val repository: com.example.sanaesencia_app.domain.repository.SanaesenciaRepository) : ViewModel() {
    private val _confirmada = MutableStateFlow(false)
    val confirmada: StateFlow<Boolean> = _confirmada.asStateFlow()

    fun disponibilidad(profesionalId: String): List<Disponibilidad> = repository.disponibilidades(profesionalId)

    fun agendar(profesional: Profesional, disponibilidad: Disponibilidad) {
        viewModelScope.launch {
            repository.guardarCita(
                Cita(UUID.randomUUID().toString(), DemoSession.usuarioId, profesional.id, profesional.nombre,
                    profesional.especialidad, disponibilidad.fecha, disponibilidad.hora,
                    disponibilidad.modalidad, disponibilidad.box, EstadoCita.AGENDADA)
            )
            _confirmada.value = true
        }
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AgendamientoViewModel(AppContainer.repository(context)) as T
        }
    }
}

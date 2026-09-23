package com.example.sanaesencia_app.ui.historial

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.domain.model.EstadoCita
import com.example.sanaesencia_app.session.DemoSession

class HistorialViewModel(private val repository: com.example.sanaesencia_app.domain.repository.SanaesenciaRepository) : ViewModel() {
    val historial = repository.citasPaciente(DemoSession.usuarioId)
    fun hayRiesgoDesercion(lista: List<com.example.sanaesencia_app.domain.model.Cita>) = lista.any { it.estado == EstadoCita.INACTIVA }
    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = HistorialViewModel(AppContainer.repository(context)) as T
        }
    }
}

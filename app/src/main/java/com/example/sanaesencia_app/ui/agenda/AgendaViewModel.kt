package com.example.sanaesencia_app.ui.agenda

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.session.DemoSession

class AgendaViewModel(private val repository: com.example.sanaesencia_app.domain.repository.SanaesenciaRepository) : ViewModel() {
    val citas = repository.citasPaciente(DemoSession.usuarioId)
    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = AgendaViewModel(AppContainer.repository(context)) as T
        }
    }
}

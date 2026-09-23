package com.example.sanaesencia_app.ui.terapeuta

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.session.DemoSession
import kotlinx.coroutines.flow.map

class TerapeutaViewModel(private val repository: com.example.sanaesencia_app.domain.repository.SanaesenciaRepository) : ViewModel() {
    val citas = repository.citasTerapeuta(DemoSession.terapeutaDemoId)
    val pacientes = repository.pacientesDemo()
    val profesionales get() = repository.profesionales()

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TerapeutaViewModel(AppContainer.repository(context)) as T
        }
    }
}

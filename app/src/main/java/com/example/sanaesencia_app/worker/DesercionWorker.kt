package com.example.sanaesencia_app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sanaesencia_app.di.AppContainer
import com.example.sanaesencia_app.domain.model.EstadoCita
import com.example.sanaesencia_app.util.NotificationHelper
import com.example.sanaesencia_app.session.DemoSession
import kotlinx.coroutines.flow.first

class DesercionWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        val repository = AppContainer.repository(applicationContext)
        val citas = repository.citasPaciente(DemoSession.usuarioId).first()
        if (citas.any { it.estado == EstadoCita.INACTIVA }) NotificationHelper.mostrarAlertaDesercion(applicationContext)
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}

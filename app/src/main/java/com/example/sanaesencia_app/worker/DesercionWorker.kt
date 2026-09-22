package com.example.sanaesencia_app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sanaesencia_app.data.repository.CitaRepositoryImpl
import com.example.sanaesencia_app.domain.model.EstadoCita
import com.example.sanaesencia_app.util.NotificationHelper
import kotlinx.coroutines.flow.first

/**
 * Worker que representa el "motor de alertas predictivas" pedido en el caso:
 * revisa periódicamente si el paciente tiene sesiones marcadas como INACTIVA
 * (señal de posible deserción) y dispara una notificación si corresponde.
 *
 * Hoy corre contra el repositorio mock (CitaRepositoryImpl). Cuando el
 * repositorio se conecte a Room/Retrofit, este Worker no necesita cambios:
 * solo cambia de dónde vienen los datos.
 */
class DesercionWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val repository = CitaRepositoryImpl()

    // Paciente de prueba fijo mientras no haya login real contra backend.
    private val pacienteIdDemo = "P001"

    override suspend fun doWork(): Result {
        return try {
            val citas = repository.getCitasByPaciente(pacienteIdDemo).first()
            val hayRiesgo = citas.any { it.estado == EstadoCita.INACTIVA }

            if (hayRiesgo) {
                NotificationHelper.mostrarAlertaDesercion(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

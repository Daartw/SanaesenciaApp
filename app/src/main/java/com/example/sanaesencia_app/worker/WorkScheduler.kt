package com.example.sanaesencia_app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val WORK_NAME_PERIODICO = "desercion_check_periodico"

    /**
     * Programa el chequeo periódico de deserción. 15 minutos es el mínimo
     * permitido por WorkManager para trabajo periódico; en producción esto
     * normalmente correría una vez al día.
     */
    fun programarChequeoDesercion(context: Context) {
        val request = PeriodicWorkRequestBuilder<DesercionWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODICO,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Dispara el chequeo una sola vez, de inmediato. Útil para demostrar el
     * funcionamiento en clase sin esperar los 15 minutos del ciclo periódico.
     */
    fun ejecutarChequeoAhora(context: Context) {
        val request = OneTimeWorkRequestBuilder<DesercionWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}

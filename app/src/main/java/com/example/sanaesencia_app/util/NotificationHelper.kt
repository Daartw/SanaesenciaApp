package com.example.sanaesencia_app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Centraliza la creación del canal de notificaciones y el envío de alertas.
 * Cubre el requerimiento del caso: "notificaciones push: alertas predictivas
 * de recordatorio de citas, avisos de inactividad y promociones".
 */
object NotificationHelper {

    const val CHANNEL_ID = "sanaesencia_alertas"
    private const val CHANNEL_NAME = "Alertas Sanaesencia"
    private const val NOTIFICATION_ID_DESERCION = 1001
    private const val NOTIFICATION_ID_RECORDATORIO = 1002

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de citas y avisos de inactividad de pacientes"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun tienePermiso(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun mostrarAlertaDesercion(context: Context) {
        if (!tienePermiso(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Posible riesgo de deserción")
            .setContentText("Detectamos un paciente sin actividad reciente. Revisa el historial.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DESERCION, notification)
    }

    fun mostrarRecordatorioCita(context: Context, especialidad: String, hora: String) {
        if (!tienePermiso(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio de cita")
            .setContentText("Tienes $especialidad hoy a las $hora.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_RECORDATORIO, notification)
    }
}

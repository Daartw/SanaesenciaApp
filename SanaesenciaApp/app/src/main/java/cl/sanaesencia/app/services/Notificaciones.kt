package cl.sanaesencia.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.sanaesencia.app.MainActivity

/**
 * RF-13: canales y envío de notificaciones (recordatorios de citas, inactividad y promociones).
 * Las usa tanto el trabajo programado [RecordatoriosWorker] como los mensajes push de FCM.
 */
object Notificaciones {

    const val CANAL_RECORDATORIOS = "recordatorios"
    const val CANAL_SEGUIMIENTO = "seguimiento"
    const val CANAL_PROMOCIONES = "promociones"

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CANAL_RECORDATORIOS, "Recordatorios de citas", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Aviso antes de cada cita agendada" },
                NotificationChannel(CANAL_SEGUIMIENTO, "Seguimiento del tratamiento", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Te avisamos si llevas tiempo sin agendar" },
                NotificationChannel(CANAL_PROMOCIONES, "Promociones", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Ofertas de la tienda de esencias" },
            )
        )
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun mostrar(context: Context, id: Int, canal: String, titulo: String, texto: String) {
        if (!tienePermiso(context)) return

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .setContentIntent(abrirApp)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (_: SecurityException) {
            // El usuario retiró el permiso entre la comprobación y el envío.
        }
    }
}

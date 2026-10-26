package cl.sanaesencia.app.services

import android.content.Context
import android.util.Log
import cl.sanaesencia.app.data.remote.RetrofitClient
import cl.sanaesencia.app.data.remote.TokenPushRequest
import cl.sanaesencia.app.data.session.SessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * RF-13: recibe notificaciones push de Firebase Cloud Messaging
 * (recordatorios, alertas de inactividad y promociones enviadas desde el backend o la consola de Firebase).
 *
 * Para activarlo: crear el proyecto en Firebase, descargar google-services.json y dejarlo en
 * SanaesenciaApp/app/. Sin ese archivo la app funciona igual con los recordatorios locales.
 */
class SanaesenciaFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val titulo = message.notification?.title ?: message.data["titulo"] ?: "Sanaesencia"
        val texto = message.notification?.body ?: message.data["mensaje"] ?: return
        val canal = when (message.data["tipo"]) {
            "promocion" -> Notificaciones.CANAL_PROMOCIONES
            "inactividad" -> Notificaciones.CANAL_SEGUIMIENTO
            else -> Notificaciones.CANAL_RECORDATORIOS
        }
        Notificaciones.mostrar(applicationContext, (System.currentTimeMillis() % 100_000).toInt(), canal, titulo, texto)
    }

    override fun onNewToken(token: String) {
        enviarToken(applicationContext, token)
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** true si el proyecto tiene google-services.json (Firebase inicializado). */
        fun disponible(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()

        /** Pide el token FCM actual y lo registra en el backend para el usuario con sesión iniciada. */
        fun registrarToken(context: Context) {
            if (!disponible(context)) return
            runCatching {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    enviarToken(context.applicationContext, token)
                }
            }
        }

        private fun enviarToken(context: Context, token: String) {
            scope.launch {
                if (SessionManager(context).session.first() == null) return@launch
                RetrofitClient.initialize(context)
                runCatching { RetrofitClient.api.registrarTokenPush(TokenPushRequest(token)) }
                    .onFailure { Log.w("SanaesenciaFCM", "No se pudo registrar el token push", it) }
            }
        }
    }
}

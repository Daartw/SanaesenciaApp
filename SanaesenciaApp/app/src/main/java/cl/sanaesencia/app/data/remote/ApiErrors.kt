package cl.sanaesencia.app.data.remote

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import retrofit2.HttpException

/**
 * Avisa a toda la app cuando el backend responde 401 a una petición que SÍ llevaba token.
 * Significa que la sesión expiró o ya no es válida: la navegación escucha este evento,
 * borra la sesión y vuelve al login.
 */
object SessionEvents {
    private val _expirada = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val expirada: SharedFlow<Unit> = _expirada.asSharedFlow()

    fun notificarExpirada() {
        _expirada.tryEmit(Unit)
    }
}

/**
 * Convierte cualquier error de red o del backend en un mensaje claro para el usuario,
 * en vez de mostrar textos como "HTTP 400" o nombres de clases.
 */
fun Throwable.mensajeUsuario(
    porDefecto: String = "Ocurrió un error inesperado. Inténtalo nuevamente.",
): String = when (this) {
    is HttpException -> {
        val mensajeServidor = mensajeDelServidor()
        when (code()) {
            401 -> mensajeServidor ?: "Tu sesión expiró. Inicia sesión nuevamente."
            403 -> mensajeServidor ?: "No tienes permisos para realizar esta acción."
            404 -> mensajeServidor ?: "El recurso solicitado no existe."
            in 500..599 -> "El servidor tuvo un problema. Inténtalo en unos minutos."
            else -> mensajeServidor ?: porDefecto
        }
    }
    is UnknownHostException, is ConnectException ->
        "No se pudo conectar con el servidor. Verifica tu conexión y que el backend esté encendido."
    is SocketTimeoutException -> "El servidor tardó demasiado en responder. Inténtalo nuevamente."
    is IOException -> "Problema de conexión. Verifica tu internet e inténtalo nuevamente."
    else -> message?.takeIf { it.isNotBlank() && !it.startsWith("HTTP ") } ?: porDefecto
}

/** true si el error significa que el usuario debe volver a iniciar sesión. */
val Throwable.esSesionExpirada: Boolean
    get() = this is HttpException && code() == 401

/** true si el error es de red (servidor apagado, sin internet, etc.). */
val Throwable.esErrorDeConexion: Boolean
    get() = this is IOException

private fun HttpException.mensajeDelServidor(): String? = runCatching {
    val body = response()?.errorBody()?.string()
    if (body.isNullOrBlank()) null else JSONObject(body).optString("mensaje").takeIf { it.isNotBlank() }
}.getOrNull()

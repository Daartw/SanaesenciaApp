package cl.sanaesencia.integration

import cl.sanaesencia.api.CitaResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * RF-14: integración con MEDIlink.
 *
 * No se inventan endpoints ni credenciales: la URL, la ruta de citas y la API key se configuran en
 * application.yml (app.medilink.*) o con variables de entorno cuando el centro tenga el acceso oficial.
 * Mientras no estén configuradas, la sincronización informa "NO_CONFIGURADO" y no envía nada.
 */
interface MedilinkApi {
    val configurado: Boolean

    /** Envía las citas a MEDIlink y devuelve cuántas aceptó. */
    fun enviarCitas(citas: List<CitaResponse>): Int
}

@Component
class MedilinkHttpClient(
    @Value("\${app.medilink.base-url:}") private val baseUrl: String,
    @Value("\${app.medilink.citas-path:}") private val citasPath: String,
    @Value("\${app.medilink.api-key:}") private val apiKey: String,
    private val mapper: ObjectMapper,
) : MedilinkApi {

    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    override val configurado: Boolean
        get() = baseUrl.isNotBlank() && citasPath.isNotBlank() && apiKey.isNotBlank()

    override fun enviarCitas(citas: List<CitaResponse>): Int {
        check(configurado) { "MEDIlink no está configurado" }
        val request = HttpRequest.newBuilder(URI.create(baseUrl.trimEnd('/') + "/" + citasPath.trimStart('/')))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(citas)))
            .build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) { "MEDIlink respondió con código ${response.statusCode()}" }
        return citas.size
    }
}

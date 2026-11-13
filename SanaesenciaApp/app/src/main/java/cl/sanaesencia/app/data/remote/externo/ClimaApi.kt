package cl.sanaesencia.app.data.remote.externo

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * IE 3.1.4 — API EXTERNA.
 *
 * Cliente de Open-Meteo (https://open-meteo.com), un servicio meteorológico público,
 * gratuito y sin API key. Se usa para mostrarle al paciente el pronóstico de la sede
 * de Los Vilos antes de una cita presencial.
 *
 * Es un servicio de TERCEROS, totalmente separado de los microservicios propios:
 * tiene su propia interfaz, su propio cliente Retrofit y su propia URL base
 * (ver [ClimaRetrofit]). Si Open-Meteo falla o no hay conexión, la app sigue
 * funcionando con normalidad: solo deja de mostrarse la tarjeta del clima.
 */
interface ClimaApi {

    /**
     * Pronóstico diario para unas coordenadas.
     *
     * Documentación: https://open-meteo.com/en/docs
     *
     * @param variables lista separada por comas de variables diarias.
     * @param zonaHoraria zona horaria IANA con la que se agrupan los días.
     * @param dias cantidad de días de pronóstico (1 a 16).
     */
    @GET("v1/forecast")
    suspend fun pronosticoDiario(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("daily") variables: String,
        @Query("timezone") zonaHoraria: String,
        @Query("forecast_days") dias: Int,
    ): ClimaRespuestaDto
}

/**
 * Respuesta de Open-Meteo. Todos los campos son opcionales a propósito: si el servicio
 * cambia su formato o devuelve datos incompletos, el parseo no revienta y el repositorio
 * simplemente descarta los días que no se pueden armar.
 */
data class ClimaRespuestaDto(
    @Json(name = "timezone") val zonaHoraria: String? = null,
    @Json(name = "daily") val diario: ClimaDiarioDto? = null,
)

data class ClimaDiarioDto(
    @Json(name = "time") val fechas: List<String?>? = null,
    @Json(name = "weather_code") val codigos: List<Int?>? = null,
    @Json(name = "temperature_2m_max") val temperaturasMaximas: List<Double?>? = null,
    @Json(name = "temperature_2m_min") val temperaturasMinimas: List<Double?>? = null,
    @Json(name = "precipitation_probability_max") val probabilidadesLluvia: List<Int?>? = null,
)

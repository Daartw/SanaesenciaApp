package cl.sanaesencia.app.data.repository

import cl.sanaesencia.app.data.remote.externo.ClimaApi
import cl.sanaesencia.app.data.remote.externo.ClimaRespuestaDto
import cl.sanaesencia.app.domain.model.PronosticoDia
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * IE 3.1.4 — Traduce la respuesta de Open-Meteo al modelo de dominio de la app.
 *
 * Es `open` y recibe la [ClimaApi] por constructor para poder sustituirla por un doble
 * de prueba en los tests unitarios.
 */
open class ClimaRepository(
    private val api: ClimaApi,
) {

    companion object {
        /** Sede presencial: Av. Caupolicán 220, Los Vilos (RF-15). */
        const val LATITUD_SEDE = -31.9167
        const val LONGITUD_SEDE = -71.5167

        const val ZONA_HORARIA = "America/Santiago"
        const val VARIABLES_DIARIAS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
        const val DIAS_POR_DEFECTO = 3
    }

    /**
     * Pronóstico de los próximos días en la sede. Lanza la excepción de red tal cual:
     * el ViewModel la convierte en un mensaje para la persona.
     */
    open suspend fun pronosticoSede(dias: Int = DIAS_POR_DEFECTO): List<PronosticoDia> =
        aPronosticos(
            api.pronosticoDiario(
                latitud = LATITUD_SEDE,
                longitud = LONGITUD_SEDE,
                variables = VARIABLES_DIARIAS,
                zonaHoraria = ZONA_HORARIA,
                dias = dias,
            ),
        )
}

/**
 * Convierte la respuesta cruda (listas paralelas) en una lista de [PronosticoDia].
 *
 * Open-Meteo devuelve un arreglo por variable, todos del mismo largo y alineados por
 * índice. Se descartan los días cuya fecha o temperaturas falten o vengan corruptas,
 * de modo que una respuesta incompleta nunca produzca un error en pantalla.
 */
fun aPronosticos(respuesta: ClimaRespuestaDto): List<PronosticoDia> {
    val diario = respuesta.diario ?: return emptyList()
    val fechas = diario.fechas ?: return emptyList()

    return fechas.indices.mapNotNull { indice ->
        val fecha = fechas.getOrNull(indice)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return@mapNotNull null

        val maxima = diario.temperaturasMaximas?.getOrNull(indice) ?: return@mapNotNull null
        val minima = diario.temperaturasMinimas?.getOrNull(indice) ?: return@mapNotNull null

        PronosticoDia(
            fecha = fecha,
            temperaturaMaxima = maxima.roundToInt(),
            temperaturaMinima = minima.roundToInt(),
            probabilidadLluvia = diario.probabilidadesLluvia?.getOrNull(indice) ?: 0,
            codigoClima = diario.codigos?.getOrNull(indice) ?: -1,
        )
    }
}

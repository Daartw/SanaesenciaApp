package cl.sanaesencia.app.data.repository

import cl.sanaesencia.app.data.remote.externo.ClimaApi
import cl.sanaesencia.app.data.remote.externo.ClimaDiarioDto
import cl.sanaesencia.app.data.remote.externo.ClimaRespuestaDto
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas de la integración con la API externa (IE 3.1.4).
 *
 * Se usa un doble de [ClimaApi] en vez de llamar a Open-Meteo de verdad: las pruebas
 * unitarias deben correr sin red y dar siempre el mismo resultado.
 */
class ClimaRepositoryTest {

    @Test
    fun convierte_las_listas_paralelas_de_open_meteo_en_pronosticos() {
        val resultado = aPronosticos(
            respuesta(
                fechas = listOf("2026-11-13", "2026-11-14"),
                codigos = listOf(0, 61),
                maximas = listOf(21.4, 18.6),
                minimas = listOf(10.5, 9.4),
                probabilidades = listOf(5, 80),
            ),
        )

        assertEquals(2, resultado.size)

        val primero = resultado.first()
        assertEquals(LocalDate.of(2026, 11, 13), primero.fecha)
        // 21.4 redondea a 21 y 10.5 a 11: la interfaz muestra grados enteros.
        assertEquals(21, primero.temperaturaMaxima)
        assertEquals(11, primero.temperaturaMinima)
        assertEquals(5, primero.probabilidadLluvia)
        assertEquals("Despejado", primero.descripcion)

        assertEquals("Lluvia", resultado[1].descripcion)
        assertTrue(resultado[1].recomiendaPrecaucion)
    }

    @Test
    fun descarta_los_dias_con_fecha_invalida() {
        val resultado = aPronosticos(
            respuesta(
                fechas = listOf("no-es-fecha", "2026-11-14"),
                codigos = listOf(0, 0),
                maximas = listOf(21.0, 18.0),
                minimas = listOf(10.0, 9.0),
                probabilidades = listOf(0, 0),
            ),
        )

        assertEquals(1, resultado.size)
        assertEquals(LocalDate.of(2026, 11, 14), resultado.first().fecha)
    }

    @Test
    fun descarta_los_dias_sin_temperatura() {
        val resultado = aPronosticos(
            respuesta(
                fechas = listOf("2026-11-13", "2026-11-14"),
                codigos = listOf(0, 0),
                maximas = listOf(null, 18.0),
                minimas = listOf(10.0, 9.0),
                probabilidades = listOf(0, 0),
            ),
        )

        assertEquals(1, resultado.size)
        assertEquals(LocalDate.of(2026, 11, 14), resultado.first().fecha)
    }

    @Test
    fun una_respuesta_sin_bloque_diario_devuelve_lista_vacia() {
        assertEquals(emptyList<Any>(), aPronosticos(ClimaRespuestaDto()))
        assertEquals(emptyList<Any>(), aPronosticos(ClimaRespuestaDto(diario = ClimaDiarioDto())))
    }

    @Test
    fun si_falta_la_probabilidad_de_lluvia_se_asume_cero() {
        val resultado = aPronosticos(
            respuesta(
                fechas = listOf("2026-11-13"),
                codigos = null,
                maximas = listOf(20.0),
                minimas = listOf(10.0),
                probabilidades = null,
            ),
        )

        assertEquals(0, resultado.first().probabilidadLluvia)
        assertEquals("Sin información", resultado.first().descripcion)
    }

    @Test
    fun el_repositorio_consulta_las_coordenadas_de_la_sede() = runBlocking {
        val api = ClimaApiFalsa(
            respuesta(
                fechas = listOf("2026-11-13"),
                codigos = listOf(2),
                maximas = listOf(19.0),
                minimas = listOf(8.0),
                probabilidades = listOf(20),
            ),
        )

        val pronostico = ClimaRepository(api).pronosticoSede()

        assertEquals(ClimaRepository.LATITUD_SEDE, api.latitud, 0.0001)
        assertEquals(ClimaRepository.LONGITUD_SEDE, api.longitud, 0.0001)
        assertEquals(ClimaRepository.ZONA_HORARIA, api.zonaHoraria)
        assertEquals(ClimaRepository.DIAS_POR_DEFECTO, api.dias)
        assertEquals(1, pronostico.size)
    }

    @Test(expected = IOException::class)
    fun el_error_de_red_se_propaga_para_que_lo_traduzca_el_viewmodel(): Unit = runBlocking {
        ClimaRepository(ClimaApiQueFalla()).pronosticoSede()
    }

    // ---------------------------------------------------------------- dobles

    private fun respuesta(
        fechas: List<String?>?,
        codigos: List<Int?>?,
        maximas: List<Double?>?,
        minimas: List<Double?>?,
        probabilidades: List<Int?>?,
    ) = ClimaRespuestaDto(
        zonaHoraria = "America/Santiago",
        diario = ClimaDiarioDto(
            fechas = fechas,
            codigos = codigos,
            temperaturasMaximas = maximas,
            temperaturasMinimas = minimas,
            probabilidadesLluvia = probabilidades,
        ),
    )

    private class ClimaApiFalsa(private val respuesta: ClimaRespuestaDto) : ClimaApi {
        var latitud = 0.0
        var longitud = 0.0
        var zonaHoraria = ""
        var dias = 0

        override suspend fun pronosticoDiario(
            latitud: Double,
            longitud: Double,
            variables: String,
            zonaHoraria: String,
            dias: Int,
        ): ClimaRespuestaDto {
            this.latitud = latitud
            this.longitud = longitud
            this.zonaHoraria = zonaHoraria
            this.dias = dias
            return respuesta
        }
    }

    private class ClimaApiQueFalla : ClimaApi {
        override suspend fun pronosticoDiario(
            latitud: Double,
            longitud: Double,
            variables: String,
            zonaHoraria: String,
            dias: Int,
        ): ClimaRespuestaDto = throw IOException("sin conexión")
    }
}

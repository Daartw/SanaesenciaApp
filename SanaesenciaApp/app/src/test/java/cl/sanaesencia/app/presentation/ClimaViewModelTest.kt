package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.data.remote.externo.ClimaApi
import cl.sanaesencia.app.data.remote.externo.ClimaRespuestaDto
import cl.sanaesencia.app.data.repository.ClimaRepository
import cl.sanaesencia.app.domain.model.PronosticoDia
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas del ViewModel de la API externa (IE 3.1.4).
 *
 * Lo importante aquí es que un fallo del servicio de terceros NO rompa la pantalla
 * de inicio del paciente: debe terminar en un estado de error manejado.
 */
class ClimaViewModelTest {

    @Test
    fun una_respuesta_con_datos_deja_el_estado_en_exito() = runBlocking {
        val vm = ClimaViewModel(RepositorioFalso(listOf(dia())), Dispatchers.Unconfined)

        vm.cargar()

        val estado = vm.state.value
        assertTrue(estado is UiState.Exito)
        assertEquals(1, (estado as UiState.Exito).datos.size)
    }

    @Test
    fun una_respuesta_sin_dias_deja_el_estado_vacio() = runBlocking {
        val vm = ClimaViewModel(RepositorioFalso(emptyList()), Dispatchers.Unconfined)

        vm.cargar()

        assertTrue(vm.state.value is UiState.Vacio)
    }

    @Test
    fun un_fallo_de_la_api_externa_no_propaga_la_excepcion_y_deja_un_mensaje() = runBlocking {
        val vm = ClimaViewModel(RepositorioQueFalla(), Dispatchers.Unconfined)

        vm.recargar()

        val estado = vm.state.value
        assertTrue(estado is UiState.Error)
        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            (estado as UiState.Error).mensaje,
        )
    }

    @Test
    fun cargar_no_vuelve_a_consultar_si_ya_hay_datos() = runBlocking {
        val repo = RepositorioFalso(listOf(dia()))
        val vm = ClimaViewModel(repo, Dispatchers.Unconfined)

        vm.cargar()
        vm.cargar()

        assertEquals(1, repo.consultas)
    }

    @Test
    fun recargar_siempre_vuelve_a_consultar() = runBlocking {
        val repo = RepositorioFalso(listOf(dia()))
        val vm = ClimaViewModel(repo, Dispatchers.Unconfined)

        vm.cargar()
        vm.recargar()

        assertEquals(2, repo.consultas)
    }

    // ---------------------------------------------------------------- dobles

    private fun dia() = PronosticoDia(
        fecha = LocalDate.of(2026, 11, 13),
        temperaturaMaxima = 20,
        temperaturaMinima = 10,
        probabilidadLluvia = 15,
        codigoClima = 1,
    )

    private open class ApiNoUsada : ClimaApi {
        override suspend fun pronosticoDiario(
            latitud: Double,
            longitud: Double,
            variables: String,
            zonaHoraria: String,
            dias: Int,
        ): ClimaRespuestaDto = ClimaRespuestaDto()
    }

    private class RepositorioFalso(
        private val resultado: List<PronosticoDia>,
    ) : ClimaRepository(ApiNoUsada()) {
        var consultas = 0

        override suspend fun pronosticoSede(dias: Int): List<PronosticoDia> {
            consultas++
            return resultado
        }
    }

    private class RepositorioQueFalla : ClimaRepository(ApiNoUsada()) {
        override suspend fun pronosticoSede(dias: Int): List<PronosticoDia> =
            throw IOException("sin conexión")
    }
}

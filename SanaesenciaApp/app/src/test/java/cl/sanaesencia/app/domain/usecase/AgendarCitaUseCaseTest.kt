package cl.sanaesencia.app.domain.usecase

import cl.sanaesencia.app.dobles.CitaRepositorioFalso
import cl.sanaesencia.app.dobles.solicitudCitaDemo
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas del caso de uso de agendamiento (RF-07).
 *
 * La regla de las 15 horas se valida en la app antes de gastar una llamada al backend,
 * y el backend la vuelve a validar: aquí se comprueba el lado de la app.
 */
class AgendarCitaUseCaseTest {

    // Reloj fijo: martes 10 de noviembre de 2026, 10:00 en Chile.
    private val reloj: Clock = Clock.fixed(
        Instant.parse("2026-11-10T13:00:00Z"),
        ZoneId.of("America/Santiago"),
    )

    @Test
    fun una_cita_con_anticipacion_suficiente_llega_al_repositorio() = runBlocking {
        val repo = CitaRepositorioFalso()

        AgendarCitaUseCase(repo, clock = reloj)(solicitudCitaDemo(fecha = "2026-11-12"))

        assertEquals(1, repo.creadas.size)
        assertEquals("2026-11-12", repo.creadas.first().fecha)
    }

    @Test
    fun una_cita_sin_las_quince_horas_se_detiene_antes_del_backend() = runBlocking {
        val repo = CitaRepositorioFalso()

        try {
            AgendarCitaUseCase(repo, clock = reloj)(
                solicitudCitaDemo(fecha = "2026-11-10", horaInicio = "13:00"),
            )
            fail("Debía rechazar la cita por falta de anticipación")
        } catch (e: IllegalStateException) {
            assertEquals("Debes agendar con al menos 15 horas de anticipación", e.message)
        }

        assertTrue(repo.creadas.isEmpty())
    }

    @Test
    fun el_limite_exacto_de_quince_horas_se_acepta() = runBlocking {
        val repo = CitaRepositorioFalso()

        AgendarCitaUseCase(repo, clock = reloj)(
            solicitudCitaDemo(fecha = "2026-11-11", horaInicio = "01:00"),
        )

        assertEquals(1, repo.creadas.size)
    }

    @Test(expected = IOException::class)
    fun un_error_del_repositorio_se_propaga(): Unit = runBlocking {
        val repo = CitaRepositorioFalso(error = IOException())

        AgendarCitaUseCase(repo, clock = reloj)(solicitudCitaDemo(fecha = "2026-11-12"))
    }
}

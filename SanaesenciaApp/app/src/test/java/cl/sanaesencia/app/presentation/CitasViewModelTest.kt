package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.data.remote.DisponibilidadResponse
import cl.sanaesencia.app.data.remote.ReagendarCitaRequest
import cl.sanaesencia.app.dobles.CitaRepositorioFalso
import cl.sanaesencia.app.dobles.MainDispatcherRule
import cl.sanaesencia.app.dobles.citaDemo
import cl.sanaesencia.app.dobles.solicitudCitaDemo
import cl.sanaesencia.app.domain.model.ModalidadCita
import cl.sanaesencia.app.domain.usecase.AgendarCitaUseCase
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** IE 3.2.1 — Pruebas del agendamiento del paciente (RF-06, RF-07 y RF-08). */
class CitasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val reloj: Clock = Clock.fixed(
        Instant.parse("2026-11-10T13:00:00Z"),
        ZoneId.of("America/Santiago"),
    )

    private fun viewModel(repo: CitaRepositorioFalso) =
        CitasViewModel(repo, AgendarCitaUseCase(repo, clock = reloj))

    @Test
    fun cargar_trae_las_citas_del_paciente() {
        val repo = CitaRepositorioFalso(citas = listOf(citaDemo(1), citaDemo(2)))
        val vm = viewModel(repo)

        vm.cargar()

        val estado = vm.citas.value
        assertTrue(estado is UiState.Exito)
        assertEquals(2, (estado as UiState.Exito).datos.size)
    }

    @Test
    fun sin_citas_el_estado_queda_vacio() {
        val vm = viewModel(CitaRepositorioFalso(citas = emptyList()))

        vm.cargar()

        assertTrue(vm.citas.value is UiState.Vacio)
    }

    @Test
    fun la_disponibilidad_se_consulta_por_profesional_y_fecha() {
        val repo = CitaRepositorioFalso()
        repo.disponibilidad = listOf(
            DisponibilidadResponse("2026-12-01", "10:00", "11:00", true, null, 4),
        )
        val vm = viewModel(repo)

        vm.disponibilidad(2, LocalDate.of(2026, 12, 1))

        val estado = vm.disponibilidad.value
        assertTrue(estado is UiState.Exito)
        assertEquals(1, (estado as UiState.Exito).datos.size)
    }

    @Test
    fun crear_una_cita_valida_avisa_y_recarga_el_listado() {
        val repo = CitaRepositorioFalso()
        val vm = viewModel(repo)

        vm.crear(solicitudCitaDemo())

        assertEquals(1, repo.creadas.size)
        assertEquals("Cita reservada correctamente", vm.message.value)
        assertTrue(vm.creada.value)
        assertFalse(vm.enviando.value)
    }

    @Test
    fun una_cita_con_menos_de_quince_horas_no_llega_al_backend() {
        val repo = CitaRepositorioFalso()
        val vm = viewModel(repo)

        // El reloj marca el 10-11-2026 a las 10:00: esta cita es para dentro de 3 horas.
        vm.crear(solicitudCitaDemo(fecha = "2026-11-10", horaInicio = "13:00"))

        assertTrue(repo.creadas.isEmpty())
        assertEquals("Debes agendar con al menos 15 horas de anticipación", vm.message.value)
        assertFalse(vm.creada.value)
    }

    @Test
    fun un_fallo_de_red_al_crear_muestra_el_mensaje_por_defecto() {
        val repo = CitaRepositorioFalso(error = IOException())
        val vm = viewModel(repo)

        vm.crear(solicitudCitaDemo())

        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            vm.message.value,
        )
    }

    @Test
    fun reagendar_envia_la_solicitud_y_avisa() {
        val repo = CitaRepositorioFalso()
        val vm = viewModel(repo)

        vm.reagendar(
            citaId = 7,
            request = ReagendarCitaRequest(2, 3, "2026-12-05", "09:00", "10:00", ModalidadCita.ONLINE),
        )

        assertEquals(1, repo.reagendadas.size)
        assertEquals(7L, repo.reagendadas.first().first)
        assertEquals("Cita reagendada correctamente", vm.message.value)
    }

    @Test
    fun cancelar_envia_el_identificador_y_avisa() {
        val repo = CitaRepositorioFalso()
        val vm = viewModel(repo)

        vm.cancelar(9)

        assertEquals(listOf(9L), repo.canceladas)
        assertEquals("Cita cancelada", vm.message.value)
    }

    @Test
    fun un_fallo_al_cancelar_muestra_el_mensaje_por_defecto() {
        val repo = CitaRepositorioFalso(error = IOException())
        val vm = viewModel(repo)

        vm.cancelar(9)

        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            vm.message.value,
        )
    }

    @Test
    fun la_validacion_silenciosa_no_escribe_mensajes() {
        val vm = viewModel(CitaRepositorioFalso())

        assertFalse(vm.validarCitaSilencioso(LocalDate.of(2026, 11, 10), LocalTime.of(13, 0)))
        assertFalse(vm.validarCitaSilencioso(LocalDate.of(2026, 11, 10), null))
        assertEquals(null, vm.message.value)
    }

    @Test
    fun los_avisos_de_creacion_y_mensaje_se_pueden_consumir() {
        val vm = viewModel(CitaRepositorioFalso())
        vm.crear(solicitudCitaDemo())

        vm.consumirCreada()
        vm.clearMessage()

        assertFalse(vm.creada.value)
        assertEquals(null, vm.message.value)
    }
}

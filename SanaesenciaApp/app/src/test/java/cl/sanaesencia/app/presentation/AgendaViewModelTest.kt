package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.data.remote.FichaClinicaResponse
import cl.sanaesencia.app.dobles.CitaRepositorioFalso
import cl.sanaesencia.app.dobles.MainDispatcherRule
import cl.sanaesencia.app.dobles.citaDemo
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.ModalidadCita
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** IE 3.2.1 — Pruebas de la agenda del terapeuta (RF-09 y RF-10). */
class AgendaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargar_trae_la_agenda_del_profesional() {
        val vm = AgendaViewModel(CitaRepositorioFalso(citas = listOf(citaDemo(1), citaDemo(2))))

        vm.cargar()

        val estado = vm.state.value
        assertTrue(estado is UiState.Exito)
        assertEquals(2, (estado as UiState.Exito).datos.size)
    }

    @Test
    fun una_agenda_sin_citas_queda_en_estado_vacio() {
        val vm = AgendaViewModel(CitaRepositorioFalso(citas = emptyList()))

        vm.cargar()

        assertTrue(vm.state.value is UiState.Vacio)
    }

    @Test
    fun marcar_una_atencion_como_completada_tiene_su_propio_aviso() {
        val repo = CitaRepositorioFalso()
        val vm = AgendaViewModel(repo)

        vm.estado(5, EstadoCita.COMPLETADA, "Sesión realizada")

        assertEquals(1, repo.estados.size)
        assertEquals(5L, repo.estados.first().first)
        assertEquals(EstadoCita.COMPLETADA, repo.estados.first().second.estado)
        assertEquals("Sesión realizada", repo.estados.first().second.notasAtencion)
        assertEquals("Atención marcada como completada", vm.message.value)
    }

    @Test
    fun otros_cambios_de_estado_usan_el_aviso_generico() {
        val vm = AgendaViewModel(CitaRepositorioFalso())

        vm.estado(5, EstadoCita.NO_ASISTIO, null)

        assertEquals("Cita actualizada", vm.message.value)
    }

    @Test
    fun un_fallo_al_cambiar_el_estado_muestra_un_mensaje_entendible() {
        val vm = AgendaViewModel(CitaRepositorioFalso(error = IOException()))

        vm.estado(5, EstadoCita.COMPLETADA, null)

        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            vm.message.value,
        )
    }

    @Test
    fun la_ficha_del_paciente_se_carga_por_identificador() {
        val repo = CitaRepositorioFalso()
        repo.fichas = listOf(
            FichaClinicaResponse(1, "2026-10-01", ModalidadCita.PRESENCIAL, EstadoCita.COMPLETADA, "Avance"),
        )
        val vm = AgendaViewModel(repo)

        vm.cargarFicha(3)

        val estado = vm.ficha.value
        assertTrue(estado is UiState.Exito)
        assertEquals("Avance", (estado as UiState.Exito).datos.first().notasAtencion)
    }

    @Test
    fun el_mensaje_se_puede_limpiar() {
        val vm = AgendaViewModel(CitaRepositorioFalso())
        vm.estado(5, EstadoCita.COMPLETADA, null)

        vm.clearMessage()

        assertNull(vm.message.value)
    }
}

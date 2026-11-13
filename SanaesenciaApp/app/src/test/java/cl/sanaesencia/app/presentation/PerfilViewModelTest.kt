package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.dobles.AuthRepositorioFalso
import cl.sanaesencia.app.dobles.MainDispatcherRule
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** IE 3.2.1 — Pruebas del perfil del paciente (RF-02). */
class PerfilViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargar_trae_el_perfil_del_backend() {
        val vm = PerfilViewModel(AuthRepositorioFalso())

        vm.cargar()

        val estado = vm.state.value
        assertTrue(estado is UiState.Exito)
        assertEquals("Ana Paciente", (estado as UiState.Exito).datos.nombreCompleto)
    }

    @Test
    fun un_fallo_de_red_al_cargar_deja_un_mensaje_entendible() {
        val vm = PerfilViewModel(AuthRepositorioFalso(error = IOException()))

        vm.cargar()

        val estado = vm.state.value
        assertTrue(estado is UiState.Error)
        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            (estado as UiState.Error).mensaje,
        )
    }

    @Test
    fun las_validaciones_de_campo_marcan_los_errores() {
        val vm = PerfilViewModel(AuthRepositorioFalso())

        vm.validarCampoRut("12345678-9")
        vm.validarCampoNombre("An")
        vm.validarCampoTelefono("123")
        vm.validarCampoFechaNacimiento("31/13/1998")
        vm.validarConsentimiento(false)

        val errores = vm.errores.value
        assertEquals("RUT inválido", errores.rut)
        assertEquals("Debe tener al menos 3 caracteres", errores.nombre)
        assertEquals("Ingresa un teléfono chileno válido", errores.telefono)
        assertEquals("Fecha de nacimiento inválida", errores.fechaNacimiento)
        assertEquals(
            "Debes aceptar el consentimiento para guardar datos de salud",
            errores.consentimiento,
        )
        assertTrue(errores.tieneErrores)
    }

    @Test
    fun los_datos_correctos_no_dejan_errores() {
        val vm = PerfilViewModel(AuthRepositorioFalso())

        vm.validarCampoRut("11.111.111-1")
        vm.validarCampoNombre("Ana Paciente")
        vm.validarCampoTelefono("+56912345678")
        vm.validarCampoFechaNacimiento("20/05/1998")
        vm.validarConsentimiento(true)

        assertFalse(vm.errores.value.tieneErrores)
    }

    @Test
    fun guardar_con_errores_avisa_y_no_llama_al_backend() {
        val repo = AuthRepositorioFalso()
        val vm = PerfilViewModel(repo)

        vm.guardar(
            rut = "12345678-9",
            nombre = "An",
            telefono = "123",
            fechaNacimiento = "31/13/1998",
            consentimiento = false,
        )

        assertEquals("Revisa los campos marcados antes de guardar", vm.message.value)
        assertNull(repo.ultimaActualizacion)
    }

    @Test
    fun guardar_envia_la_fecha_en_formato_iso() {
        val repo = AuthRepositorioFalso()
        val vm = PerfilViewModel(repo)

        vm.guardar(
            rut = "11111111-1",
            nombre = "Ana Paciente",
            telefono = "+56912345678",
            fechaNacimiento = "20/05/1998",
            consentimiento = true,
        )

        // La interfaz usa dd/MM/yyyy y el backend recibe yyyy-MM-dd.
        assertEquals("1998-05-20", repo.ultimaActualizacion?.fechaNacimiento)
        assertEquals("Perfil actualizado", vm.message.value)
        assertTrue(vm.state.value is UiState.Exito)
    }

    @Test
    fun un_fallo_al_guardar_muestra_el_mensaje_por_defecto() {
        val repo = AuthRepositorioFalso(error = IOException())
        val vm = PerfilViewModel(repo)

        vm.guardar(
            rut = "11111111-1",
            nombre = "Ana Paciente",
            telefono = "+56912345678",
            fechaNacimiento = "20/05/1998",
            consentimiento = true,
        )

        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            vm.message.value,
        )
    }

    @Test
    fun el_mensaje_se_puede_limpiar() {
        val vm = PerfilViewModel(AuthRepositorioFalso())
        vm.guardar("12345678-9", "An", "123", "01/01/1800", false)

        vm.clearMessage()

        assertNull(vm.message.value)
    }
}

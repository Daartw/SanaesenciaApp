package cl.sanaesencia.app.presentation

import cl.sanaesencia.app.dobles.AuthRepositorioFalso
import cl.sanaesencia.app.dobles.MainDispatcherRule
import cl.sanaesencia.app.domain.model.RolUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** IE 3.2.1 — Pruebas del inicio de sesión (RF-01). */
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun el_correo_invalido_se_marca_al_escribir() {
        val vm = LoginViewModel(AuthRepositorioFalso())

        vm.onEmailChanged("sin-arroba")

        assertEquals("Ingresa un correo válido", vm.errors.value.email)
        assertTrue(vm.errors.value.hasErrors)
    }

    @Test
    fun un_correo_valido_no_deja_error() {
        val vm = LoginViewModel(AuthRepositorioFalso())

        vm.onEmailChanged("paciente@sanaesencia.demo")

        assertNull(vm.errors.value.email)
    }

    @Test
    fun la_contrasena_corta_se_marca_al_escribir() {
        val vm = LoginViewModel(AuthRepositorioFalso())

        vm.onPasswordChanged("123")

        assertEquals("La contraseña debe tener al menos 8 caracteres", vm.errors.value.password)
    }

    @Test
    fun no_se_llama_al_backend_si_el_formulario_tiene_errores() {
        val repo = AuthRepositorioFalso()
        val vm = LoginViewModel(repo)

        vm.onEmailChanged("malo")
        vm.onPasswordChanged("123")
        vm.login()

        assertNull(repo.ultimoEmail)
        assertTrue(vm.state.value is UiState.Vacio)
    }

    @Test
    fun un_login_correcto_deja_la_sesion_en_exito() {
        val repo = AuthRepositorioFalso()
        val vm = LoginViewModel(repo)

        vm.onEmailChanged("paciente@sanaesencia.demo")
        vm.onPasswordChanged("Demo123!")
        vm.login()

        val estado = vm.state.value
        assertTrue(estado is UiState.Exito)
        assertEquals(RolUsuario.PACIENTE, (estado as UiState.Exito).datos.rol)
        assertEquals("paciente@sanaesencia.demo", repo.ultimoEmail)
    }

    @Test
    fun credenciales_incorrectas_muestran_el_mensaje_del_repositorio() {
        val repo = AuthRepositorioFalso(
            error = IllegalArgumentException("Correo o contraseña incorrectos"),
        )
        val vm = LoginViewModel(repo)

        vm.onEmailChanged("paciente@sanaesencia.demo")
        vm.onPasswordChanged("Demo123!")
        vm.login()

        val estado = vm.state.value
        assertTrue(estado is UiState.Error)
        assertEquals("Correo o contraseña incorrectos", (estado as UiState.Error).mensaje)
    }

    @Test
    fun al_reiniciar_se_limpian_campos_errores_y_estado() {
        val vm = LoginViewModel(AuthRepositorioFalso())
        vm.onEmailChanged("malo")
        vm.onPasswordChanged("123")

        vm.resetState()

        assertEquals("", vm.email.value)
        assertEquals("", vm.password.value)
        assertFalse(vm.errors.value.hasErrors)
        assertTrue(vm.state.value is UiState.Vacio)
    }
}

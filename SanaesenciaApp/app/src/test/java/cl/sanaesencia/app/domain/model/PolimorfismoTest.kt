package cl.sanaesencia.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolimorfismoTest {
    @Test
    fun paciente_resuelve_pantalla_y_permisos() {
        val usuario: Usuario = Usuario.Paciente()

        assertEquals("home", usuario.pantallaInicial())
        assertTrue(Permiso.AGENDAR in usuario.permisos())
    }

    @Test
    fun profesional_resuelve_pantalla_y_permisos() {
        val usuario: Usuario = Usuario.Profesional()

        assertEquals("agenda", usuario.pantallaInicial())
        assertTrue(Permiso.VER_AGENDA in usuario.permisos())
    }

    @Test
    fun administrador_resuelve_pantalla_y_permisos() {
        val usuario: Usuario = Usuario.Administrador()

        assertEquals("admin", usuario.pantallaInicial())
        assertTrue(Permiso.GESTIONAR_TIENDA in usuario.permisos())
    }

    @Test
    fun producto_y_servicio_sobrescriben_comportamiento_de_catalogo() {
        val producto: ItemCatalogo = Producto(1, "Esencia", "Bienestar natural", 5000, 3, emptyList())
        val servicio: ItemCatalogo = Servicio(2, "Terapia", 30000, "Sesión", 50)

        assertEquals(5000L, producto.precioFinal())
        assertEquals(30000L, servicio.precioFinal())
        assertEquals("Bienestar natural", producto.descripcionCorta())
        assertEquals("50 min · Sesión", servicio.descripcionCorta())
    }
}

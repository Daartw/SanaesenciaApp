package cl.sanaesencia.app.data.remote

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * IE 3.2.1 — Pruebas de la traducción de errores.
 *
 * Ninguna pantalla debe mostrar "HTTP 500" ni el nombre de una excepción: esta capa
 * convierte cualquier fallo de red o del backend en un mensaje entendible.
 */
class ApiErrorsTest {

    @Test
    fun el_401_pide_iniciar_sesion_nuevamente() {
        assertEquals(
            "Tu sesión expiró. Inicia sesión nuevamente.",
            httpException(401).mensajeUsuario(),
        )
    }

    @Test
    fun el_403_explica_que_falta_permiso() {
        assertEquals(
            "No tienes permisos para realizar esta acción.",
            httpException(403).mensajeUsuario(),
        )
    }

    @Test
    fun el_404_indica_que_el_recurso_no_existe() {
        assertEquals(
            "El recurso solicitado no existe.",
            httpException(404).mensajeUsuario(),
        )
    }

    @Test
    fun los_errores_del_servidor_piden_reintentar_mas_tarde() {
        assertEquals(
            "El servidor tuvo un problema. Inténtalo en unos minutos.",
            httpException(500).mensajeUsuario(),
        )
        assertEquals(
            "El servidor tuvo un problema. Inténtalo en unos minutos.",
            httpException(503).mensajeUsuario(),
        )
    }

    @Test
    fun un_codigo_sin_caso_propio_usa_el_mensaje_por_defecto() {
        assertEquals(
            "No fue posible guardar",
            httpException(400).mensajeUsuario("No fue posible guardar"),
        )
    }

    @Test
    fun sin_conexion_se_explica_que_revise_internet_y_el_backend() {
        val esperado =
            "No se pudo conectar con el servidor. Verifica tu conexión y que el backend esté encendido."

        assertEquals(esperado, UnknownHostException().mensajeUsuario())
        assertEquals(esperado, ConnectException().mensajeUsuario())
    }

    @Test
    fun el_timeout_tiene_su_propio_mensaje() {
        assertEquals(
            "El servidor tardó demasiado en responder. Inténtalo nuevamente.",
            SocketTimeoutException().mensajeUsuario(),
        )
    }

    @Test
    fun cualquier_otro_error_de_red_pide_revisar_la_conexion() {
        assertEquals(
            "Problema de conexión. Verifica tu internet e inténtalo nuevamente.",
            IOException("socket closed").mensajeUsuario(),
        )
    }

    @Test
    fun una_excepcion_de_negocio_muestra_su_propio_mensaje() {
        assertEquals(
            "Debes agendar con al menos 15 horas de anticipación",
            IllegalStateException("Debes agendar con al menos 15 horas de anticipación")
                .mensajeUsuario(),
        )
    }

    @Test
    fun un_mensaje_tecnico_o_vacio_se_reemplaza_por_el_texto_por_defecto() {
        assertEquals(
            "Ocurrió un error inesperado. Inténtalo nuevamente.",
            IllegalStateException("HTTP 418 I am a teapot").mensajeUsuario(),
        )
        assertEquals(
            "Ocurrió un error inesperado. Inténtalo nuevamente.",
            IllegalStateException("   ").mensajeUsuario(),
        )
        assertEquals(
            "Ocurrió un error inesperado. Inténtalo nuevamente.",
            RuntimeException().mensajeUsuario(),
        )
    }

    @Test
    fun solo_el_401_marca_la_sesion_como_expirada() {
        assertTrue(httpException(401).esSesionExpirada)
        assertFalse(httpException(403).esSesionExpirada)
        assertFalse(IOException().esSesionExpirada)
    }

    @Test
    fun solo_las_excepciones_de_entrada_salida_son_errores_de_conexion() {
        assertTrue(IOException().esErrorDeConexion)
        assertTrue(UnknownHostException().esErrorDeConexion)
        assertFalse(httpException(500).esErrorDeConexion)
        assertFalse(IllegalStateException().esErrorDeConexion)
    }

    private fun httpException(codigo: Int, cuerpo: String = ""): HttpException =
        HttpException(
            Response.error<Any>(
                codigo,
                cuerpo.toResponseBody("application/json".toMediaTypeOrNull()),
            ),
        )
}

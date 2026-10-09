package cl.sanaesencia.app.domain.validacion

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresTest {
    private val zone = ZoneId.of("America/Santiago")
    private val clock = Clock.fixed(Instant.parse("2026-10-06T15:00:00Z"), zone)

    @Test
    fun cita_14h59_rechaza() {
        val result = ValidadorCita()(LocalDate.of(2026, 10, 7), LocalTime.of(2, 59), clock)
        assertTrue(result is ResultadoValidacion.Invalido)
    }

    @Test
    fun cita_15h_acepta() {
        val result = ValidadorCita()(LocalDate.of(2026, 10, 7), LocalTime.of(3, 0), clock)
        assertEquals(ResultadoValidacion.Valido, result)
    }

    @Test
    fun rut_valido_e_invalido() {
        assertEquals(ResultadoValidacion.Valido, ValidadorRut()("11.111.111-1"))
        assertTrue(ValidadorRut()("11.111.111-2") is ResultadoValidacion.Invalido)
    }

    @Test
    fun telefono_chile_acepta_formatos_validos() {
        assertEquals(ResultadoValidacion.Valido, ValidadorTelefonoChile()("912345678"))
        assertEquals(ResultadoValidacion.Valido, ValidadorTelefonoChile()("+56912345678"))
        assertEquals(ResultadoValidacion.Valido, ValidadorTelefonoChile()("56912345678"))
    }

    @Test
    fun telefono_chile_rechaza_formatos_invalidos() {
        assertTrue(ValidadorTelefonoChile()("5912345678") is ResultadoValidacion.Invalido)
        assertTrue(ValidadorTelefonoChile()("812345678") is ResultadoValidacion.Invalido)
    }

    @Test
    fun stock_respeta_limite() {
        assertEquals(ResultadoValidacion.Valido, ValidadorStockCarrito()(2, 2))
        assertTrue(ValidadorStockCarrito()(3, 2) is ResultadoValidacion.Invalido)
        assertTrue(ValidadorStockCarrito()(0, 2) is ResultadoValidacion.Invalido)
    }

    @Test
    fun validadores_nuevos_cubren_validos_invalidos_y_limites() {
        assertEquals(ResultadoValidacion.Valido, ValidadorNombre()("Ana"))
        assertTrue(ValidadorNombre()("An") is ResultadoValidacion.Invalido)
        assertEquals(ResultadoValidacion.Valido, ValidadorTextoMin3()("abc"))
        assertTrue(ValidadorTextoMin3()("ab") is ResultadoValidacion.Invalido)
        assertEquals(ResultadoValidacion.Valido, ValidadorTitularTarjeta()("Ana Pérez"))
        assertTrue(ValidadorTitularTarjeta()("An") is ResultadoValidacion.Invalido)
        assertEquals(ResultadoValidacion.Valido, ValidadorTarjeta()("4111111111111111"))
        assertTrue(ValidadorTarjeta()("4111111111111112") is ResultadoValidacion.Invalido)
        assertEquals(ResultadoValidacion.Valido, ValidadorCvv()("123"))
        assertTrue(ValidadorCvv()("12") is ResultadoValidacion.Invalido)
        assertEquals(ResultadoValidacion.Valido, ValidadorPrecio()("1"))
        assertTrue(ValidadorPrecio()("0") is ResultadoValidacion.Invalido)
        assertEquals(ResultadoValidacion.Valido, ValidadorStock()("0"))
        assertTrue(ValidadorStock()("-1") is ResultadoValidacion.Invalido)
    }
}

package cl.sanaesencia.app.domain.validacion

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IE 3.2.1 — Cobertura completa de los validadores de dominio.
 *
 * Son las reglas que protegen los datos antes de llegar al backend: RUT chileno,
 * teléfono, tarjeta de pago (Luhn), regla de las 15 horas (RF-07) y stock (RF-04).
 */
class ValidadoresCompletosTest {

    private val relojFijo: Clock = Clock.fixed(
        // Lunes 10 de noviembre de 2026, 10:00 en Chile continental.
        Instant.parse("2026-11-10T13:00:00Z"),
        ZoneId.of("America/Santiago"),
    )

    // ------------------------------------------------------------------ email

    @Test
    fun email_valido_es_aceptado() {
        assertTrue(ValidadorEmail()("paciente@sanaesencia.cl") is ResultadoValidacion.Valido)
        assertTrue(ValidadorEmail()("  paciente@sanaesencia.cl  ") is ResultadoValidacion.Valido)
    }

    @Test
    fun email_sin_arroba_o_sin_dominio_es_rechazado() {
        assertEquals("Ingresa un correo válido", mensaje(ValidadorEmail()("sanaesencia.cl")))
        assertEquals("Ingresa un correo válido", mensaje(ValidadorEmail()("paciente@sanaesencia")))
        assertEquals("Ingresa un correo válido", mensaje(ValidadorEmail()("")))
    }

    // -------------------------------------------------------------- contraseña

    @Test
    fun password_requiere_ocho_caracteres() {
        assertTrue(ValidadorPassword()("Demo123!") is ResultadoValidacion.Valido)
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            mensaje(ValidadorPassword()("Demo12")),
        )
    }

    // --------------------------------------------------------------------- RUT

    @Test
    fun rut_con_digito_verificador_correcto_es_aceptado() {
        // Mismo RUT en los tres formatos de uso habitual en Chile.
        assertTrue(ValidadorRut()("11111111-1") is ResultadoValidacion.Valido)
        assertTrue(ValidadorRut()("11.111.111-1") is ResultadoValidacion.Valido)
        assertTrue(ValidadorRut()("111111111") is ResultadoValidacion.Valido)
    }

    @Test
    fun rut_con_digito_verificador_k_es_aceptado() {
        assertTrue(ValidadorRut()("20347878-K") is ResultadoValidacion.Valido)
        assertTrue(ValidadorRut()("20347878-k") is ResultadoValidacion.Valido)
    }

    @Test
    fun rut_con_digito_verificador_incorrecto_es_rechazado() {
        assertEquals("RUT inválido", mensaje(ValidadorRut()("11111111-2")))
    }

    @Test
    fun rut_con_formato_invalido_es_rechazado() {
        assertEquals("RUT inválido", mensaje(ValidadorRut()("123")))
        assertEquals("RUT inválido", mensaje(ValidadorRut()("abcdefgh-1")))
        assertEquals("RUT inválido", mensaje(ValidadorRut()("")))
    }

    // ---------------------------------------------------------------- teléfono

    @Test
    fun telefono_chileno_acepta_los_formatos_usuales() {
        assertTrue(ValidadorTelefonoChile()("+56912345678") is ResultadoValidacion.Valido)
        assertTrue(ValidadorTelefonoChile()("56912345678") is ResultadoValidacion.Valido)
        assertTrue(ValidadorTelefonoChile()("912345678") is ResultadoValidacion.Valido)
        assertTrue(ValidadorTelefonoChile()("+56 9 1234 5678") is ResultadoValidacion.Valido)
    }

    @Test
    fun telefono_que_no_es_celular_chileno_es_rechazado() {
        assertEquals(
            "Ingresa un teléfono chileno válido",
            mensaje(ValidadorTelefonoChile()("221234567")),
        )
        assertEquals(
            "Ingresa un teléfono chileno válido",
            mensaje(ValidadorTelefonoChile()("91234567")),
        )
    }

    // -------------------------------------------------------- fecha nacimiento

    @Test
    fun fecha_de_nacimiento_pasada_es_valida() {
        val resultado = ValidadorFechaNacimiento()(LocalDate.of(1998, 5, 20), relojFijo)

        assertTrue(resultado is ResultadoValidacion.Valido)
    }

    @Test
    fun fecha_de_nacimiento_futura_nula_o_anterior_a_1900_es_rechazada() {
        assertEquals(
            "Fecha de nacimiento inválida",
            mensaje(ValidadorFechaNacimiento()(LocalDate.of(2030, 1, 1), relojFijo)),
        )
        assertEquals(
            "Fecha de nacimiento inválida",
            mensaje(ValidadorFechaNacimiento()(null, relojFijo)),
        )
        assertEquals(
            "Fecha de nacimiento inválida",
            mensaje(ValidadorFechaNacimiento()(LocalDate.of(1899, 12, 31), relojFijo)),
        )
    }

    // ------------------------------------------------------- RF-07: 15 horas

    @Test
    fun cita_con_mas_de_quince_horas_de_anticipacion_es_valida() {
        // Reloj: 10-11-2026 10:00. Mínimo: 11-11-2026 01:00.
        val resultado = ValidadorCita()(LocalDate.of(2026, 11, 11), LocalTime.of(9, 0), relojFijo)

        assertTrue(resultado is ResultadoValidacion.Valido)
    }

    @Test
    fun cita_justo_en_el_limite_de_quince_horas_es_valida() {
        val resultado = ValidadorCita()(LocalDate.of(2026, 11, 11), LocalTime.of(1, 0), relojFijo)

        assertTrue(resultado is ResultadoValidacion.Valido)
    }

    @Test
    fun cita_con_menos_de_quince_horas_es_rechazada() {
        val resultado = ValidadorCita()(LocalDate.of(2026, 11, 11), LocalTime.of(0, 59), relojFijo)

        assertEquals(
            "Debes agendar con al menos 15 horas de anticipación",
            mensaje(resultado),
        )
    }

    // -------------------------------------------------------- RF-04: carrito

    @Test
    fun cantidad_dentro_del_stock_es_valida() {
        assertTrue(ValidadorStockCarrito()(2, 5) is ResultadoValidacion.Valido)
        assertTrue(ValidadorStockCarrito()(5, 5) is ResultadoValidacion.Valido)
    }

    @Test
    fun cantidad_invalida_o_sobre_el_stock_es_rechazada() {
        assertEquals("Cantidad inválida", mensaje(ValidadorStockCarrito()(0, 5)))
        assertEquals("Stock insuficiente", mensaje(ValidadorStockCarrito()(6, 5)))
    }

    // ----------------------------------------------------------------- pago

    @Test
    fun tarjeta_que_cumple_luhn_es_valida() {
        assertTrue(ValidadorTarjeta()("4111111111111111") is ResultadoValidacion.Valido)
        assertTrue(ValidadorTarjeta()("4111 1111 1111 1111") is ResultadoValidacion.Valido)
    }

    @Test
    fun tarjeta_con_largo_incorrecto_o_que_falla_luhn_es_rechazada() {
        assertEquals("La tarjeta debe tener 16 dígitos", mensaje(ValidadorTarjeta()("41111111")))
        assertEquals("Número de tarjeta inválido", mensaje(ValidadorTarjeta()("4111111111111112")))
    }

    @Test
    fun cvv_de_tres_digitos_es_valido() {
        assertTrue(ValidadorCvv()("123") is ResultadoValidacion.Valido)
        assertEquals("El CVV debe tener 3 dígitos", mensaje(ValidadorCvv()("12")))
        assertEquals("El CVV debe tener 3 dígitos", mensaje(ValidadorCvv()("12a")))
    }

    @Test
    fun titular_de_la_tarjeta_requiere_tres_caracteres() {
        assertTrue(ValidadorTitularTarjeta()("Ana Pérez") is ResultadoValidacion.Valido)
        assertEquals("Ingresa el nombre del titular", mensaje(ValidadorTitularTarjeta()("A ")))
    }

    // ----------------------------------------------- formularios de administración

    @Test
    fun id_precio_y_stock_validan_sus_rangos() {
        assertTrue(ValidadorIdPositivo()("5") is ResultadoValidacion.Valido)
        assertEquals("El ID debe ser mayor que 0", mensaje(ValidadorIdPositivo()("0")))
        assertEquals("El ID debe ser mayor que 0", mensaje(ValidadorIdPositivo()("x")))

        assertTrue(ValidadorPrecio()("9990") is ResultadoValidacion.Valido)
        assertEquals("El precio debe ser mayor que 0", mensaje(ValidadorPrecio()("0")))

        assertTrue(ValidadorStock()("0") is ResultadoValidacion.Valido)
        assertEquals("El stock debe ser un entero no negativo", mensaje(ValidadorStock()("-1")))
        assertEquals("El stock debe ser un entero no negativo", mensaje(ValidadorStock()("dos")))
    }

    @Test
    fun nombre_y_texto_minimo_comparten_la_regla_de_tres_caracteres() {
        assertTrue(ValidadorNombre()("Ana") is ResultadoValidacion.Valido)
        assertEquals("Debe tener al menos 3 caracteres", mensaje(ValidadorNombre()("An")))
        assertEquals("Debe tener al menos 3 caracteres", mensaje(ValidadorTextoMin3()("  ")))
    }

    private fun mensaje(resultado: ResultadoValidacion): String? =
        (resultado as? ResultadoValidacion.Invalido)?.mensaje
}

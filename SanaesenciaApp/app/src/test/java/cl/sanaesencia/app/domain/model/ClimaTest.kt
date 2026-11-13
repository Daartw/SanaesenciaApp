package cl.sanaesencia.app.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas del modelo de dominio del clima (API externa, IE 3.1.4).
 *
 * Se valida la traducción de los códigos WMO y la regla de negocio que decide
 * cuándo avisarle al paciente que puede llover antes de una cita presencial.
 */
class ClimaTest {

    @Test
    fun codigos_wmo_se_traducen_a_descripciones_en_espanol() {
        assertEquals("Despejado", descripcionClimaWmo(0))
        assertEquals("Parcialmente nublado", descripcionClimaWmo(2))
        assertEquals("Neblina", descripcionClimaWmo(45))
        assertEquals("Llovizna", descripcionClimaWmo(53))
        assertEquals("Lluvia", descripcionClimaWmo(63))
        assertEquals("Nieve", descripcionClimaWmo(75))
        assertEquals("Chubascos", descripcionClimaWmo(81))
        assertEquals("Tormenta eléctrica", descripcionClimaWmo(95))
        assertEquals("Tormenta con granizo", descripcionClimaWmo(99))
    }

    @Test
    fun codigo_desconocido_no_rompe_la_interfaz() {
        assertEquals("Sin información", descripcionClimaWmo(-1))
        assertEquals("Sin información", descripcionClimaWmo(1234))
    }

    @Test
    fun solo_los_codigos_de_precipitacion_se_consideran_lluvia() {
        assertTrue(esCodigoDeLluvia(51))
        assertTrue(esCodigoDeLluvia(65))
        assertTrue(esCodigoDeLluvia(82))
        assertTrue(esCodigoDeLluvia(95))

        assertFalse(esCodigoDeLluvia(0))
        assertFalse(esCodigoDeLluvia(3))
        assertFalse(esCodigoDeLluvia(45))
        assertFalse(esCodigoDeLluvia(75))
    }

    @Test
    fun se_recomienda_precaucion_cuando_la_probabilidad_llega_al_umbral() {
        val dia = pronostico(probabilidad = PronosticoDia.UMBRAL_LLUVIA, codigo = 0)

        assertTrue(dia.recomiendaPrecaucion)
    }

    @Test
    fun no_se_recomienda_precaucion_con_dia_despejado_y_baja_probabilidad() {
        val dia = pronostico(probabilidad = 10, codigo = 0)

        assertFalse(dia.recomiendaPrecaucion)
    }

    @Test
    fun se_recomienda_precaucion_si_el_codigo_es_de_lluvia_aunque_la_probabilidad_sea_baja() {
        val dia = pronostico(probabilidad = 5, codigo = 61)

        assertTrue(dia.recomiendaPrecaucion)
    }

    @Test
    fun la_descripcion_del_dia_usa_su_propio_codigo() {
        assertEquals("Nublado", pronostico(probabilidad = 0, codigo = 3).descripcion)
    }

    private fun pronostico(probabilidad: Int, codigo: Int) = PronosticoDia(
        fecha = LocalDate.of(2026, 11, 13),
        temperaturaMaxima = 21,
        temperaturaMinima = 11,
        probabilidadLluvia = probabilidad,
        codigoClima = codigo,
    )
}

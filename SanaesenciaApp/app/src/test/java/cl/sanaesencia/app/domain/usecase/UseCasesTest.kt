package cl.sanaesencia.app.domain.usecase

import cl.sanaesencia.app.domain.model.EstadoProfesional
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas de los casos de uso del dominio.
 *
 * Cubren RF-12 (deserción a los 21 días), RF-04 (stock bajo) y la regla que impide
 * agendar con un profesional ausente, de vacaciones o con la agenda pausada.
 */
class UseCasesTest {

    private val hoy = LocalDate.of(2026, 11, 13)

    // ----------------------------------------------------- RF-12: deserción

    @Test
    fun paciente_con_veintiun_dias_sin_sesion_se_marca_como_desercion() {
        val caso = DetectarDesercionUseCase()

        assertTrue(caso(hoy.minusDays(21), hoy))
        assertTrue(caso(hoy.minusDays(40), hoy))
    }

    @Test
    fun paciente_con_menos_de_veintiun_dias_no_es_desercion() {
        val caso = DetectarDesercionUseCase()

        assertFalse(caso(hoy.minusDays(20), hoy))
        assertFalse(caso(hoy, hoy))
    }

    @Test
    fun el_umbral_de_desercion_es_configurable() {
        val caso = DetectarDesercionUseCase(umbralDias = 7)

        assertTrue(caso(hoy.minusDays(7), hoy))
        assertFalse(caso(hoy.minusDays(6), hoy))
    }

    // ---------------------------------------------------- RF-04: stock bajo

    @Test
    fun el_stock_bajo_se_detecta_desde_tres_unidades() {
        val caso = VerificarStockBajoUseCase()

        assertTrue(caso(0))
        assertTrue(caso(3))
        assertFalse(caso(4))
    }

    @Test
    fun el_umbral_de_stock_es_configurable() {
        val caso = VerificarStockBajoUseCase(umbral = 10)

        assertTrue(caso(10))
        assertFalse(caso(11))
    }

    // ------------------------------------------- RF-05/RF-06: disponibilidad

    @Test
    fun solo_un_profesional_disponible_puede_recibir_reservas() {
        val caso = PuedeAgendarUseCase()

        assertTrue(caso(EstadoProfesional.DISPONIBLE))
        assertFalse(caso(EstadoProfesional.AUSENTE))
        assertFalse(caso(EstadoProfesional.VACACIONES))
        assertFalse(caso(EstadoProfesional.AGENDA_PAUSADA))
    }
}

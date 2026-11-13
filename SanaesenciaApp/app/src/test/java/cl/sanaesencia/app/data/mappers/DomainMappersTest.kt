package cl.sanaesencia.app.data.mappers

import cl.sanaesencia.app.data.local.CitaEntity
import cl.sanaesencia.app.data.local.ProductoEntity
import cl.sanaesencia.app.data.local.ProfesionalEntity
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.ProductoResponse
import cl.sanaesencia.app.data.remote.ProfesionalResponse
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.model.ModalidadCita
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * IE 3.2.1 — Pruebas de los mapeadores entre las tres capas: respuesta del backend,
 * entidad de Room (caché local, RNF-04) y modelo de dominio.
 */
class DomainMappersTest {

    @Test
    fun profesional_del_backend_se_guarda_como_entidad_local() {
        val entidad = ProfesionalResponse(
            id = 7,
            usuarioId = 70,
            titulo = "Psicóloga clínica",
            categoriaEspecialidad = "Salud mental infanto-juvenil",
            descripcionEnfoque = "Terapia sistémica",
            estado = EstadoProfesional.DISPONIBLE,
        ).toEntity()

        assertEquals(7L, entidad.id)
        assertEquals(70L, entidad.usuarioId)
        assertEquals("Salud mental infanto-juvenil", entidad.categoria)
        assertEquals("Terapia sistémica", entidad.descripcion)
        assertEquals(EstadoProfesional.DISPONIBLE, entidad.estado)
    }

    @Test
    fun entidad_local_de_profesional_vuelve_al_dominio() {
        val dominio = ProfesionalEntity(
            id = 7,
            usuarioId = 70,
            titulo = "Psicóloga clínica",
            categoria = "Salud mental",
            descripcion = "Terapia sistémica",
            estado = EstadoProfesional.VACACIONES,
        ).toDomain()

        assertEquals("Salud mental", dominio.categoriaEspecialidad)
        assertEquals("Terapia sistémica", dominio.descripcionEnfoque)
        assertEquals(EstadoProfesional.VACACIONES, dominio.estado)
    }

    @Test
    fun las_etiquetas_terapeuticas_viajan_como_texto_y_vuelven_como_lista() {
        val entidad = ProductoResponse(
            id = 3,
            nombre = "Bruma de lavanda",
            descripcion = "Relajante",
            precio = 8990,
            stock = 12,
            etiquetasTerapeuticas = "relajación,sueño,ansiedad",
        ).toEntity()

        assertEquals("relajación,sueño,ansiedad", entidad.etiquetas)

        val dominio = entidad.toDomain()
        assertEquals(listOf("relajación", "sueño", "ansiedad"), dominio.etiquetas)
        assertEquals(8990L, dominio.precio)
        assertEquals(12, dominio.stock)
    }

    @Test
    fun un_producto_sin_etiquetas_devuelve_lista_vacia() {
        val dominio = ProductoEntity(
            id = 1,
            nombre = "Aceite esencial",
            descripcion = "",
            precio = 5990,
            stock = 2,
            etiquetas = "",
        ).toDomain()

        assertEquals(emptyList<String>(), dominio.etiquetas)
    }

    @Test
    fun la_cita_del_backend_se_convierte_en_entidad_con_fecha_y_horas_tipadas() {
        val entidad = citaResponse().toEntity()

        assertEquals(LocalDate.of(2026, 11, 20), entidad.fecha)
        assertEquals(LocalTime.of(10, 0), entidad.inicio)
        assertEquals(LocalTime.of(11, 0), entidad.fin)
        assertEquals(ModalidadCita.PRESENCIAL, entidad.modalidad)
        assertEquals(EstadoCita.RESERVADA, entidad.estado)
        assertEquals(4L, entidad.boxId)
    }

    @Test
    fun una_cita_con_fecha_corrupta_no_rompe_la_cache_local() {
        val entidad = citaResponse(fecha = "20-11-2026", horaInicio = "25:00").toEntity()

        // Se usan valores por defecto en vez de lanzar una excepción.
        assertEquals(LocalDate.now(), entidad.fecha)
        assertEquals(LocalTime.of(9, 0), entidad.inicio)
    }

    @Test
    fun la_entidad_local_puede_volver_a_la_forma_de_respuesta_para_modo_sin_conexion() {
        val respuesta = CitaEntity(
            id = 9,
            pacienteId = 1,
            profesionalId = 2,
            boxId = null,
            fecha = LocalDate.of(2026, 12, 1),
            inicio = LocalTime.of(15, 30),
            fin = LocalTime.of(16, 30),
            modalidad = ModalidadCita.ONLINE,
            estado = EstadoCita.CONFIRMADA,
        ).toResponse()

        assertEquals("2026-12-01", respuesta.fecha)
        assertEquals("15:30", respuesta.horaInicio)
        assertEquals("16:30", respuesta.horaFin)
        assertEquals(ModalidadCita.ONLINE, respuesta.modalidad)
        assertEquals(null, respuesta.boxId)
    }

    @Test
    fun ida_y_vuelta_entre_respuesta_y_entidad_conserva_los_datos() {
        val original = citaResponse()

        val resultado = original.toEntity().toResponse()

        assertEquals(original, resultado)
    }

    private fun citaResponse(
        fecha: String = "2026-11-20",
        horaInicio: String = "10:00",
    ) = CitaResponse(
        id = 9,
        pacienteId = 1,
        profesionalId = 2,
        boxId = 4,
        fecha = fecha,
        horaInicio = horaInicio,
        horaFin = "11:00",
        modalidad = ModalidadCita.PRESENCIAL,
        estado = EstadoCita.RESERVADA,
    )
}

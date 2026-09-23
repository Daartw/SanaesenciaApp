package com.example.sanaesencia_app.data.repository

import com.example.sanaesencia_app.data.local.AppDatabase
import com.example.sanaesencia_app.data.local.CitaEntity
import com.example.sanaesencia_app.domain.model.*
import com.example.sanaesencia_app.domain.repository.SanaesenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class SanaesenciaRepositoryImpl(private val db: AppDatabase) : SanaesenciaRepository {
    private val dao = db.citaDao()
    private val profesionalesState = demoProfesionales.toMutableList()
    private val boxesState = MutableList(8) { Box(it + 1, if (it < 3) EstadoBox.OCUPADO else EstadoBox.DISPONIBLE) }


    suspend fun seedDemoDataIfNeeded() {
        if (dao.getTodasLasCitas().first().isNotEmpty()) return
        listOf(
            Cita("C001", "P001", "T001", "Camila Ríos", "Psicología", "2026-09-25", "10:00", ModalidadAtencion.PRESENCIAL, 3, EstadoCita.AGENDADA),
            Cita("C002", "P001", "T002", "Matías Soto", "Terapia Ocupacional", "2026-09-18", "15:30", ModalidadAtencion.ONLINE, null, EstadoCita.COMPLETADA),
            Cita("C003", "P001", "T004", "Valentina Muñoz", "Nutrición", "2026-08-30", "09:00", ModalidadAtencion.PRESENCIAL, 5, EstadoCita.INACTIVA)
        ).forEach { dao.insertCita(toEntity(it)) }
    }

    override fun citasPaciente(pacienteId: String): Flow<List<Cita>> = dao.getCitasByPaciente(pacienteId).map { it.map(::toDomain) }
    override fun citasTerapeuta(terapeutaId: String): Flow<List<Cita>> = dao.getCitasByTerapeuta(terapeutaId).map { it.map(::toDomain) }
    override fun todasLasCitas(): Flow<List<Cita>> = dao.getTodasLasCitas().map { it.map(::toDomain) }

    override suspend fun guardarCita(cita: Cita) { dao.insertCita(toEntity(cita)) }

    override suspend fun cambiarEstadoCita(cita: Cita, estado: EstadoCita) {
        dao.updateCita(toEntity(cita.copy(estado = estado)))
    }

    override fun profesionales(): List<Profesional> = profesionalesState.toList()

    override fun disponibilidades(profesionalId: String): List<Disponibilidad> = demoDisponibilidades.filter { it.profesionalId == profesionalId }

    override fun especialidades(): List<Especialidad> = profesionalesState
        .map { it.especialidad }
        .distinct()
        .mapIndexed { index, name -> Especialidad("E${index + 1}", name, "Atención profesional especializada en $name.", profesionalesState.filter { it.especialidad == name }.flatMap { it.modalidades }.distinct()) }

    override fun productos(): List<Producto> = demoProductos
    override fun promociones(): List<Promocion> = demoPromociones
    override fun pacientesDemo(): List<Paciente> = demoPacientes
    override fun boxes(): List<Box> = boxesState.toList()

    override suspend fun agregarProfesional(profesional: Profesional) { profesionalesState.add(profesional) }

    override suspend fun cambiarEstadoProfesional(id: String, estado: EstadoTerapeuta) {
        val index = profesionalesState.indexOfFirst { it.id == id }
        if (index >= 0) profesionalesState[index] = profesionalesState[index].copy(estado = estado)
    }

    override suspend fun cambiarEstadoBox(numero: Int, estado: EstadoBox) {
        val index = boxesState.indexOfFirst { it.numero == numero }
        if (index >= 0) boxesState[index] = boxesState[index].copy(estado = estado)
    }

    private fun toDomain(entity: CitaEntity) = Cita(
        entity.id, entity.pacienteId, entity.terapeutaId, entity.terapeutaNombre,
        entity.especialidad, entity.fecha, entity.hora,
        ModalidadAtencion.valueOf(entity.modalidad), entity.boxAsignado,
        EstadoCita.valueOf(entity.estado)
    )

    private fun toEntity(cita: Cita) = CitaEntity(
        cita.id, cita.pacienteId, cita.terapeutaId, cita.terapeutaNombre,
        cita.especialidad, cita.fecha, cita.hora, cita.modalidad.name,
        cita.boxAsignado, cita.estado.name
    )

    companion object {
        val demoProfesionales = listOf(
            Profesional("T001", "Camila Ríos", "Psicología", "Psicología", listOf("Diplomado en Psicología Infanto-Juvenil"), listOf("Intervenciones basadas en juego", "Primeros auxilios psicológicos"), 8, EstadoTerapeuta.DISPONIBLE, listOf(ModalidadAtencion.PRESENCIAL, ModalidadAtencion.ONLINE), "Acompañamiento psicológico para niños, adolescentes y familias."),
            Profesional("T002", "Matías Soto", "Terapia Ocupacional", "Terapia Ocupacional", listOf("Integración Sensorial"), listOf("Neurodesarrollo", "Estimulación de habilidades funcionales"), 6, EstadoTerapeuta.DISPONIBLE, listOf(ModalidadAtencion.PRESENCIAL, ModalidadAtencion.ONLINE), "Intervención centrada en autonomía, participación y desarrollo funcional."),
            Profesional("T003", "Daniela Pérez", "Fonoaudiología", "Fonoaudiología", listOf("Evaluación del lenguaje"), listOf("Comunicación aumentativa", "Trastornos del habla"), 7, EstadoTerapeuta.VACACIONES, listOf(ModalidadAtencion.PRESENCIAL), "Evaluación y acompañamiento de habla, lenguaje y comunicación."),
            Profesional("T004", "Valentina Muñoz", "Nutrición", "Nutrición y Dietética", listOf("Nutrición clínica"), listOf("Educación alimentaria", "Nutrición familiar"), 5, EstadoTerapeuta.DISPONIBLE, listOf(ModalidadAtencion.PRESENCIAL, ModalidadAtencion.ONLINE), "Orientación nutricional y seguimiento personalizado."),
            Profesional("T005", "Sofía Herrera", "Psicopedagogía", "Psicopedagogía", listOf("Evaluación psicopedagógica"), listOf("Dificultades de aprendizaje", "Estrategias de estudio"), 4, EstadoTerapeuta.AUSENTE, listOf(ModalidadAtencion.ONLINE), "Apoyo en procesos de aprendizaje y estrategias educativas.")
        )

        val demoDisponibilidades = listOf(
            Disponibilidad("D001", "T001", "2026-09-24", "09:00", ModalidadAtencion.PRESENCIAL, 1),
            Disponibilidad("D002", "T001", "2026-09-24", "11:00", ModalidadAtencion.ONLINE, null),
            Disponibilidad("D003", "T001", "2026-09-25", "15:00", ModalidadAtencion.PRESENCIAL, 2),
            Disponibilidad("D004", "T002", "2026-09-24", "10:00", ModalidadAtencion.PRESENCIAL, 3),
            Disponibilidad("D005", "T002", "2026-09-26", "16:00", ModalidadAtencion.ONLINE, null),
            Disponibilidad("D006", "T004", "2026-09-25", "09:30", ModalidadAtencion.PRESENCIAL, 4),
            Disponibilidad("D007", "T004", "2026-09-26", "12:00", ModalidadAtencion.ONLINE, null)
        )

        val demoProductos = listOf(
            Producto("P001", "Kit Bienestar", "Selección de productos naturales para acompañar rutinas de bienestar.", "Kits", 15990),
            Producto("P002", "Roll-on Relax", "Roll-on aromático de uso externo.", "Roll-ons", 6990),
            Producto("P003", "Bruma Ambiental", "Bruma aromática para espacios de relajación.", "Brumas", 8990)
        )

        val demoPromociones = listOf(
            Promocion("PR001", "Bienestar de primavera", "Beneficio demostrativo para atenciones en periodos de menor demanda.", "30/09/2026"),
            Promocion("PR002", "Primera orientación online", "Promoción ficticia para demostración académica.", "15/10/2026")
        )

        val demoPacientes = listOf(
            Paciente("P001", "Paciente Demo", "paciente.demo@example.test", "2026-08-30", 23),
            Paciente("P002", "Paciente Demo 2", "paciente2.demo@example.test", "2026-09-15", 7),
            Paciente("P003", "Paciente Demo 3", "paciente3.demo@example.test", null, 45)
        )
    }
}

package com.example.sanaesencia_app.data.repository

import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.model.EstadoCita
import com.example.sanaesencia_app.domain.model.Especialidad
import com.example.sanaesencia_app.domain.model.Modalidad
import com.example.sanaesencia_app.domain.repository.CitaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación mock del repositorio, con datos de prueba anonimizados/ficticios.
 * Sirve para desarrollar y probar la UI (Compose) sin depender aún de Room ni del
 * backend Spring Boot. Se puede reemplazar más adelante por una versión que use
 * CitaDao (Room) y SanaesenciaApi (Retrofit) sin tocar la capa de UI.
 */
class CitaRepositoryImpl : CitaRepository {

    private val citasMock = MutableStateFlow(
        listOf(
            Cita(
                id = "C001",
                pacienteId = "P001",
                terapeutaNombre = "Camila Ríos",
                especialidad = "Psicología Infantil",
                fecha = "2026-09-25",
                hora = "10:00",
                modalidad = Modalidad.PRESENCIAL,
                boxAsignado = 3,
                estado = EstadoCita.AGENDADA
            ),
            Cita(
                id = "C002",
                pacienteId = "P001",
                terapeutaNombre = "Matías Soto",
                especialidad = "Terapia Ocupacional",
                fecha = "2026-09-18",
                hora = "15:30",
                modalidad = Modalidad.ONLINE,
                boxAsignado = null,
                estado = EstadoCita.COMPLETADA
            ),
            Cita(
                id = "C003",
                pacienteId = "P001",
                terapeutaNombre = "Valentina Muñoz",
                especialidad = "Nutrición",
                fecha = "2026-08-30",
                hora = "09:00",
                modalidad = Modalidad.PRESENCIAL,
                boxAsignado = 5,
                estado = EstadoCita.INACTIVA
            )
        )
    )

    private val especialidadesMock = MutableStateFlow(
        listOf(
            Especialidad(
                id = "E001",
                nombre = "Psicología Infantil",
                descripcion = "Atención psicológica para niños, niñas y adolescentes.",
                profesional = "Camila Ríos",
                modalidades = listOf(Modalidad.PRESENCIAL, Modalidad.ONLINE)
            ),
            Especialidad(
                id = "E002",
                nombre = "Terapia Ocupacional",
                descripcion = "Intervención orientada al desarrollo de habilidades funcionales.",
                profesional = "Matías Soto",
                modalidades = listOf(Modalidad.ONLINE)
            ),
            Especialidad(
                id = "E003",
                nombre = "Fonoaudiología",
                descripcion = "Evaluación y tratamiento de trastornos del habla y lenguaje.",
                profesional = "Daniela Pérez",
                modalidades = listOf(Modalidad.PRESENCIAL)
            ),
            Especialidad(
                id = "E004",
                nombre = "Nutrición",
                descripcion = "Planes alimentarios personalizados y seguimiento nutricional.",
                profesional = "Valentina Muñoz",
                modalidades = listOf(Modalidad.PRESENCIAL, Modalidad.ONLINE)
            )
        )
    )

    override fun getCitasByPaciente(pacienteId: String): Flow<List<Cita>> =
        MutableStateFlow(citasMock.value.filter { it.pacienteId == pacienteId }).asStateFlow()

    override fun getEspecialidades(): Flow<List<Especialidad>> = especialidadesMock.asStateFlow()

    override suspend fun agendarCita(cita: Cita) {
        citasMock.value = citasMock.value + cita
    }
}

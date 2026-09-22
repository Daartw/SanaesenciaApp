package com.example.sanaesencia_app.domain.model

data class Cita(
    val id: String,
    val pacienteId: String,
    val terapeutaNombre: String,
    val especialidad: String,
    val fecha: String,
    val hora: String,
    val modalidad: Modalidad,
    val boxAsignado: Int?,
    val estado: EstadoCita
)

enum class Modalidad { PRESENCIAL, ONLINE }

enum class EstadoCita { AGENDADA, COMPLETADA, CANCELADA, INACTIVA }

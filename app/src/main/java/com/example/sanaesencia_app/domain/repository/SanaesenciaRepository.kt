package com.example.sanaesencia_app.domain.repository

import com.example.sanaesencia_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SanaesenciaRepository {
    fun citasPaciente(pacienteId: String): Flow<List<Cita>>
    fun citasTerapeuta(terapeutaId: String): Flow<List<Cita>>
    fun todasLasCitas(): Flow<List<Cita>>
    suspend fun guardarCita(cita: Cita)
    suspend fun cambiarEstadoCita(cita: Cita, estado: EstadoCita)
    fun profesionales(): List<Profesional>
    fun disponibilidades(profesionalId: String): List<Disponibilidad>
    fun especialidades(): List<Especialidad>
    fun productos(): List<Producto>
    fun promociones(): List<Promocion>
    fun pacientesDemo(): List<Paciente>
    fun boxes(): List<Box>
    suspend fun agregarProfesional(profesional: Profesional)
    suspend fun cambiarEstadoProfesional(id: String, estado: EstadoTerapeuta)
    suspend fun cambiarEstadoBox(numero: Int, estado: EstadoBox)
}

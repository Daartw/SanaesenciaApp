package com.example.sanaesencia_app.domain.repository

import com.example.sanaesencia_app.domain.model.Cita
import com.example.sanaesencia_app.domain.model.Especialidad
import kotlinx.coroutines.flow.Flow

interface CitaRepository {
    fun getCitasByPaciente(pacienteId: String): Flow<List<Cita>>
    fun getEspecialidades(): Flow<List<Especialidad>>
    suspend fun agendarCita(cita: Cita)
}

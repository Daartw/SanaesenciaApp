package com.example.sanaesencia_app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// DTOs simples para el consumo de la API REST (Spring Boot)
data class CitaDto(
    val id: String,
    val pacienteId: String,
    val terapeutaNombre: String,
    val especialidad: String,
    val fecha: String,
    val hora: String,
    val modalidad: String,
    val boxAsignado: Int?,
    val estado: String
)

interface SanaesenciaApi {

    @GET("api/citas/paciente/{id}")
    suspend fun getCitasByPaciente(@Path("id") pacienteId: String): List<CitaDto>

    @POST("api/citas")
    suspend fun crearCita(@Body cita: CitaDto): CitaDto
}

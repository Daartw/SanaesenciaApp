package com.example.sanaesencia_app.data.remote

import com.example.sanaesencia_app.domain.model.ModalidadAtencion
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class CitaDto(
    val id: String,
    val pacienteId: String,
    val terapeutaId: String,
    val terapeutaNombre: String,
    val especialidad: String,
    val fecha: String,
    val hora: String,
    val modalidad: String,
    val boxAsignado: Int?,
    val estado: String
)

data class ProfesionalDto(
    val id: String,
    val nombre: String,
    val especialidad: String,
    val carrera: String,
    val certificaciones: List<String>,
    val capacitaciones: List<String>,
    val aniosExperiencia: Int,
    val estado: String,
    val modalidades: List<ModalidadAtencion>,
    val descripcion: String
)

data class DisponibilidadDto(
    val id: String,
    val profesionalId: String,
    val fecha: String,
    val hora: String,
    val modalidad: String,
    val box: Int?
)

interface SanaesenciaApi {
    @GET("api/citas/paciente/{id}") suspend fun getCitasByPaciente(@Path("id") pacienteId: String): List<CitaDto>
    @GET("api/citas/terapeuta/{id}") suspend fun getCitasByTerapeuta(@Path("id") terapeutaId: String): List<CitaDto>
    @POST("api/citas") suspend fun crearCita(@Body cita: CitaDto): CitaDto
    @GET("api/profesionales") suspend fun getProfesionales(@Query("especialidad") especialidad: String? = null): List<ProfesionalDto>
    @GET("api/profesionales/{id}/disponibilidad") suspend fun getDisponibilidad(@Path("id") profesionalId: String): List<DisponibilidadDto>
    @PUT("api/profesionales/{id}/estado") suspend fun cambiarEstado(@Path("id") profesionalId: String, @Body estado: Map<String, String>): ProfesionalDto
}

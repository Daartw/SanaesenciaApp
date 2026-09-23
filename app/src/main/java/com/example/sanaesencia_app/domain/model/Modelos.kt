package com.example.sanaesencia_app.domain.model

enum class RolUsuario { PACIENTE, TERAPEUTA, ADMIN }
enum class EstadoTerapeuta { DISPONIBLE, AUSENTE, VACACIONES }
enum class ModalidadAtencion { PRESENCIAL, ONLINE }
enum class EstadoCita { AGENDADA, COMPLETADA, CANCELADA, INACTIVA }
enum class EstadoBox { DISPONIBLE, OCUPADO, MANTENCION }

data class Profesional(
    val id: String,
    val nombre: String,
    val especialidad: String,
    val carrera: String,
    val certificaciones: List<String>,
    val capacitaciones: List<String>,
    val aniosExperiencia: Int,
    val estado: EstadoTerapeuta,
    val modalidades: List<ModalidadAtencion>,
    val descripcion: String
)

data class Disponibilidad(
    val id: String,
    val profesionalId: String,
    val fecha: String,
    val hora: String,
    val modalidad: ModalidadAtencion,
    val box: Int?
)

data class Cita(
    val id: String,
    val pacienteId: String,
    val terapeutaId: String,
    val terapeutaNombre: String,
    val especialidad: String,
    val fecha: String,
    val hora: String,
    val modalidad: ModalidadAtencion,
    val boxAsignado: Int?,
    val estado: EstadoCita
)

data class Paciente(
    val id: String,
    val nombre: String,
    val correo: String,
    val ultimaSesion: String?,
    val diasInactivo: Int
)

data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precioReferencial: Int
)

data class Promocion(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val vigencia: String,
    val activa: Boolean = true
)

data class Box(
    val numero: Int,
    val estado: EstadoBox,
    val profesionalAsignado: String? = null
)

data class Especialidad(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val modalidades: List<ModalidadAtencion>
)

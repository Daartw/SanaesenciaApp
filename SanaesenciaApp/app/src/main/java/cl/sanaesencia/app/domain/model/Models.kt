package cl.sanaesencia.app.domain.model
enum class RolUsuario{PACIENTE,PROFESIONAL,ADMIN}
enum class EstadoProfesional{DISPONIBLE,AUSENTE,VACACIONES,AGENDA_PAUSADA}
enum class ModalidadCita{PRESENCIAL,ONLINE}
enum class EstadoCita{RESERVADA,CONFIRMADA,COMPLETADA,CANCELADA,NO_ASISTIO}
data class Profesional(val id:Long,val usuarioId:Long,val titulo:String,val categoriaEspecialidad:String,val descripcionEnfoque:String,val estado:EstadoProfesional)
data class Box(val id:Long,val numeroBox:Int,val estaHabilitado:Boolean)
data class Producto(val id:Long,val nombre:String,val descripcion:String,val precio:Double,val stock:Int,val etiquetas:List<String>)

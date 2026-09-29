package cl.sanaesencia.api
import cl.sanaesencia.domain.*
import java.time.LocalDate
import java.time.LocalTime

data class LoginRequest(val email:String,val password:String)
data class LoginResponse(val token:String,val usuarioId:Long,val nombre:String,val rol:RolUsuario)

data class CrearCitaRequest(
    val pacienteId:Long,val profesionalId:Long,val boxId:Long?,
    val fecha:LocalDate,val horaInicio:LocalTime,val horaFin:LocalTime,
    val modalidad:ModalidadCita
)

data class CitaResponse(
    val id:Long,val pacienteId:Long,val profesionalId:Long,val boxId:Long?,
    val fecha:LocalDate,val horaInicio:LocalTime,val horaFin:LocalTime,
    val modalidad:ModalidadCita,val estado:EstadoCita
)

data class DesercionResponse(val pacienteId:Long,val ultimaSesion:LocalDate?,val diasSinSesion:Long,val alerta:Boolean)

package cl.sanaesencia.app.data.remote
import cl.sanaesencia.app.domain.model.*
import retrofit2.http.*
import java.time.LocalDate
import java.time.LocalTime
data class LoginRequest(val email:String,val password:String)
data class LoginResponse(val token:String,val usuarioId:Long,val nombre:String,val rol:RolUsuario)
data class ProfesionalResponse(val id:Long,val usuarioId:Long,val titulo:String,val categoriaEspecialidad:String,val descripcionEnfoque:String,val estado:EstadoProfesional,val fotoUrl:String?)
data class ProductoResponse(val id:Long,val nombre:String,val descripcion:String,val precio:Double,val stock:Int,val imagenUrl:String?,val etiquetasTerapeuticas:String,val estaActivo:Boolean)
data class BoxResponse(val id:Long,val numeroBox:Int,val estaHabilitado:Boolean)
data class CrearCitaRequest(val pacienteId:Long,val profesionalId:Long,val boxId:Long?,val fecha:LocalDate,val horaInicio:LocalTime,val horaFin:LocalTime,val modalidad:ModalidadCita)
data class CitaResponse(val id:Long,val pacienteId:Long,val profesionalId:Long,val boxId:Long?,val fecha:LocalDate,val horaInicio:LocalTime,val horaFin:LocalTime,val modalidad:ModalidadCita,val estado:EstadoCita)
interface SanaesenciaApi{
    @POST("auth/login") suspend fun login(@Body r:LoginRequest):LoginResponse
    @GET("profesionales") suspend fun profesionales():List<ProfesionalResponse>
    @GET("productos") suspend fun productos():List<ProductoResponse>
    @GET("boxes") suspend fun boxes():List<BoxResponse>
    @POST("citas") suspend fun crearCita(@Body r:CrearCitaRequest):CitaResponse
    @GET("citas/paciente/{id}") suspend fun historial(@Path("id") id:Long):List<CitaResponse>
}

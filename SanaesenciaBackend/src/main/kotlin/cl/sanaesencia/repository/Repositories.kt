package cl.sanaesencia.repository
import cl.sanaesencia.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface UsuarioRepository: JpaRepository<Usuario,Long> { fun findByEmail(email:String): Usuario? }
interface ProfesionalRepository: JpaRepository<Profesional,Long>
interface BoxRepository: JpaRepository<Box,Long> { fun findAllByOrderByNumeroBoxAsc():List<Box> }
interface ProductoRepository: JpaRepository<Producto,Long>
interface PromocionRepository: JpaRepository<Promocion,Long>

interface CitaRepository: JpaRepository<Cita,Long> {
    fun findAllByPacienteIdOrderByFechaDescHoraInicioDesc(pacienteId:Long):List<Cita>

    @Query("""
        select count(c) > 0 from Cita c
        where c.profesionalId = :profesionalId and c.fecha = :fecha
        and c.estado not in :cancelados
        and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun profesionalSolapa(profesionalId:Long, fecha:LocalDate, inicio:LocalTime, fin:LocalTime, cancelados:Set<EstadoCita>):Boolean

    @Query("""
        select count(c) > 0 from Cita c
        where c.pacienteId = :pacienteId and c.fecha = :fecha
        and c.estado not in :cancelados
        and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun pacienteSolapa(pacienteId:Long, fecha:LocalDate, inicio:LocalTime, fin:LocalTime, cancelados:Set<EstadoCita>):Boolean

    @Query("""
        select count(c) > 0 from Cita c
        where c.boxId = :boxId and c.fecha = :fecha
        and c.estado not in :cancelados
        and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun boxSolapa(boxId:Long, fecha:LocalDate, inicio:LocalTime, fin:LocalTime, cancelados:Set<EstadoCita>):Boolean
}

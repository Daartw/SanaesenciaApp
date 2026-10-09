package cl.sanaesencia.repository

import cl.sanaesencia.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
}

interface ProfesionalRepository : JpaRepository<Profesional, Long> {
    fun findByUsuarioId(usuarioId: Long): Profesional?
}

interface BoxRepository : JpaRepository<Box, Long> {
    fun findAllByOrderByNumeroBoxAsc(): List<Box>
}

interface ProductoRepository : JpaRepository<Producto, Long>
interface PromocionRepository : JpaRepository<Promocion, Long>
interface PedidoRepository : JpaRepository<Pedido, Long> {
    fun findAllByPacienteIdOrderByIdDesc(pacienteId: Long): List<Pedido>
}
interface PedidoItemRepository : JpaRepository<PedidoItem, Long> {
    fun findAllByPedidoId(pedidoId: Long): List<PedidoItem>
}

interface CitaRepository : JpaRepository<Cita, Long> {
    fun findAllByPacienteIdOrderByFechaDescHoraInicioDesc(pacienteId: Long): List<Cita>
    fun findAllByProfesionalIdOrderByFechaAscHoraInicioAsc(profesionalId: Long): List<Cita>

    @Query("""
        select count(c) > 0 from Cita c
        where c.id <> :citaId and c.profesionalId = :profesionalId and c.fecha = :fecha
        and c.estado <> :cancelada and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun profesionalSolapaExcluyendo(citaId: Long, profesionalId: Long, fecha: LocalDate, inicio: LocalTime, fin: LocalTime, cancelada: EstadoCita): Boolean

    @Query("""
        select count(c) > 0 from Cita c
        where c.pacienteId = :pacienteId and c.fecha = :fecha
        and c.estado <> :cancelada and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun pacienteSolapa(pacienteId: Long, fecha: LocalDate, inicio: LocalTime, fin: LocalTime, cancelada: EstadoCita): Boolean

    @Query("""
        select count(c) > 0 from Cita c
        where c.id <> :citaId and c.pacienteId = :pacienteId and c.fecha = :fecha
        and c.estado <> :cancelada and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun pacienteSolapaExcluyendo(citaId: Long, pacienteId: Long, fecha: LocalDate, inicio: LocalTime, fin: LocalTime, cancelada: EstadoCita): Boolean

    @Query("""
        select count(c) > 0 from Cita c
        where c.boxId = :boxId and c.fecha = :fecha
        and c.estado <> :cancelada and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun boxSolapa(boxId: Long, fecha: LocalDate, inicio: LocalTime, fin: LocalTime, cancelada: EstadoCita): Boolean

    @Query("""
        select count(c) > 0 from Cita c
        where c.id <> :citaId and c.boxId = :boxId and c.fecha = :fecha
        and c.estado <> :cancelada and c.horaInicio < :fin and c.horaFin > :inicio
    """)
    fun boxSolapaExcluyendo(citaId: Long, boxId: Long, fecha: LocalDate, inicio: LocalTime, fin: LocalTime, cancelada: EstadoCita): Boolean
}

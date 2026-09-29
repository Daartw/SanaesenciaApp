package cl.sanaesencia.service

import cl.sanaesencia.api.*
import cl.sanaesencia.domain.*
import cl.sanaesencia.repository.*
import cl.sanaesencia.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AuthService(
    private val users: UsuarioRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService
) {

    fun login(r: LoginRequest): LoginResponse {
        val u = users.findByEmail(r.email)
            ?: error("Credenciales inválidas")

        require(encoder.matches(r.password, u.passwordHash)) {
            "Credenciales inválidas"
        }

        return LoginResponse(
            jwt.generate(u.id, u.email, u.rol),
            u.id,
            u.nombreCompleto,
            u.rol
        )
    }
}

@Service
class CitaService(
    private val citas: CitaRepository,
    private val profesionales: ProfesionalRepository,
    private val boxes: BoxRepository
) {

    private val cancelados = setOf(EstadoCita.CANCELADA)

    @Transactional
    fun crear(r: CrearCitaRequest): CitaResponse {

        require(r.horaFin.isAfter(r.horaInicio)) {
            "La hora de término debe ser posterior a la de inicio"
        }

        val p = profesionales.findById(r.profesionalId)
            .orElseThrow {
                IllegalArgumentException("Profesional no encontrado")
            }

        require(p.estado == EstadoProfesional.DISPONIBLE) {
            "El profesional no tiene agenda disponible"
        }

        if (r.modalidad == ModalidadCita.PRESENCIAL) {

            require(r.boxId != null) {
                "La atención presencial requiere box"
            }

            val b = boxes.findById(r.boxId)
                .orElseThrow {
                    IllegalArgumentException("Box no encontrado")
                }

            require(b.estaHabilitado && b.numeroBox in 1..8) {
                "Box no habilitado"
            }

        } else {

            require(r.boxId == null) {
                "Una atención online no usa box"
            }
        }

        require(
            !citas.profesionalSolapa(
                r.profesionalId,
                r.fecha,
                r.horaInicio,
                r.horaFin,
                cancelados
            )
        ) {
            "El profesional ya está ocupado"
        }

        require(
            !citas.pacienteSolapa(
                r.pacienteId,
                r.fecha,
                r.horaInicio,
                r.horaFin,
                cancelados
            )
        ) {
            "El paciente ya tiene una cita en ese horario"
        }

        if (r.boxId != null) {
            require(
                !citas.boxSolapa(
                    r.boxId,
                    r.fecha,
                    r.horaInicio,
                    r.horaFin,
                    cancelados
                )
            ) {
                "El box ya está ocupado"
            }
        }

        val c = citas.save(
            Cita(
                pacienteId = r.pacienteId,
                profesionalId = r.profesionalId,
                boxId = r.boxId,
                fecha = r.fecha,
                horaInicio = r.horaInicio,
                horaFin = r.horaFin,
                modalidad = r.modalidad
            )
        )

        return CitaResponse(
            c.id,
            c.pacienteId,
            c.profesionalId,
            c.boxId,
            c.fecha,
            c.horaInicio,
            c.horaFin,
            c.modalidad,
            c.estado
        )
    }

    fun historial(id: Long) =
        citas.findAllByPacienteIdOrderByFechaDescHoraInicioDesc(id)
            .map {
                CitaResponse(
                    it.id,
                    it.pacienteId,
                    it.profesionalId,
                    it.boxId,
                    it.fecha,
                    it.horaInicio,
                    it.horaFin,
                    it.modalidad,
                    it.estado
                )
            }
}

@Service
class DesercionService(
    private val users: UsuarioRepository,
    private val citas: CitaRepository,
    @Value("\${app.desercion.dias-inactividad}")
    private val dias: Int
) {

    fun alertas(): List<DesercionResponse> {

        val hoy = LocalDate.now()

        return users.findAll()
            .filter {
                it.rol == RolUsuario.PACIENTE
            }
            .map { u ->

                val ultima =
                    citas.findAllByPacienteIdOrderByFechaDescHoraInicioDesc(u.id)
                        .firstOrNull {
                            it.estado == EstadoCita.COMPLETADA
                        }
                        ?.fecha

                val d = ultima?.let {
                    ChronoUnit.DAYS.between(it, hoy)
                } ?: Long.MAX_VALUE

                DesercionResponse(
                    u.id,
                    ultima,
                    d,
                    d >= dias
                )
            }
            .filter {
                it.alerta
            }
    }
}
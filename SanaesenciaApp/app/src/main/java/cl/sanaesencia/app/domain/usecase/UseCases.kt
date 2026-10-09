package cl.sanaesencia.app.domain.usecase

import cl.sanaesencia.app.data.remote.CrearCitaRequest
import cl.sanaesencia.app.data.repository.CitaRepository
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.validacion.ResultadoValidacion
import cl.sanaesencia.app.domain.validacion.ValidadorCita
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class AgendarCitaUseCase(
    private val repository: CitaRepository,
    private val validator: ValidadorCita = ValidadorCita(),
    private val clock: Clock = Clock.system(
        ZoneId.of("America/Santiago")
    ),
) {

    suspend operator fun invoke(
        request: CrearCitaRequest
    ) {
        val fecha = LocalDate.parse(request.fecha)
        val horaInicio = LocalTime.parse(request.horaInicio)

        when (
            val result = validator(
                fecha,
                horaInicio,
                clock
            )
        ) {
            ResultadoValidacion.Valido -> {
                repository.crear(request)
            }

            is ResultadoValidacion.Invalido -> {
                error(result.mensaje)
            }
        }
    }
}

class DetectarDesercionUseCase(
    private val umbralDias: Long = 21
) {

    operator fun invoke(
        ultimaSesion: LocalDate,
        hoy: LocalDate = LocalDate.now()
    ): Boolean {
        return ChronoUnit.DAYS.between(
            ultimaSesion,
            hoy
        ) >= umbralDias
    }
}

class VerificarStockBajoUseCase(
    private val umbral: Int = 3
) {

    operator fun invoke(
        stock: Int
    ): Boolean {
        return stock <= umbral
    }
}

class PuedeAgendarUseCase {

    operator fun invoke(
        estado: EstadoProfesional
    ): Boolean {
        return estado == EstadoProfesional.DISPONIBLE
    }
}
package cl.sanaesencia.app.domain.usecase
import cl.sanaesencia.app.domain.model.EstadoProfesional
import java.time.LocalDate
import java.time.temporal.ChronoUnit
class DetectarDesercionUseCase(private val umbralDias:Long=21){
    operator fun invoke(ultimaSesion:LocalDate,hoy:LocalDate=LocalDate.now())=ChronoUnit.DAYS.between(ultimaSesion,hoy)>=umbralDias
}
class VerificarStockBajoUseCase{operator fun invoke(stock:Int)=stock<=3}
class PuedeAgendarUseCase{operator fun invoke(estado:EstadoProfesional)=estado==EstadoProfesional.DISPONIBLE}

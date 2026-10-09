package cl.sanaesencia.app.domain.validacion

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

sealed interface ResultadoValidacion {
    data object Valido : ResultadoValidacion
    data class Invalido(val mensaje: String) : ResultadoValidacion
}

fun interface Validador<T> {
    operator fun invoke(valor: T): ResultadoValidacion
}

class ValidadorEmail : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""").matches(valor.trim())) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Ingresa un correo válido")
}

class ValidadorPassword : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.length >= 8) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("La contraseña debe tener al menos 8 caracteres")
}

class ValidadorRut : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion {
        val limpio = valor.replace(".", "").replace("-", "").uppercase()
        if (!Regex("\\d{7,8}[0-9K]").matches(limpio)) {
            return ResultadoValidacion.Invalido("RUT inválido")
        }
        var suma = 0
        var multiplicador = 2
        for (i in limpio.length - 2 downTo 0) {
            suma += (limpio[i] - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val dv = when (val value = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> '0' + value
        }
        return if (dv == limpio.last()) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("RUT inválido")
    }
}

class ValidadorTelefonoChile : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion {
        val limpio = valor.replace(" ", "")
        return if (Regex("^(\\+?56)?9\\d{8}$").matches(limpio)) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Ingresa un teléfono chileno válido")
    }
}

class ValidadorFechaNacimiento {
    operator fun invoke(
        valor: LocalDate?,
        clock: Clock = Clock.system(ZoneId.of("America/Santiago")),
    ): ResultadoValidacion =
        if (valor != null && !valor.isAfter(LocalDate.now(clock)) && valor.isAfter(LocalDate.of(1900, 1, 1))) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Fecha de nacimiento inválida")
}

class ValidadorCita {
    operator fun invoke(
        fecha: LocalDate,
        hora: LocalTime,
        clock: Clock = Clock.system(ZoneId.of("America/Santiago")),
    ): ResultadoValidacion {
        val inicio = fecha.atTime(hora)
        val minimo = LocalDateTime.now(clock).plusHours(15)
        return if (!inicio.isBefore(minimo)) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Debes agendar con al menos 15 horas de anticipación")
    }
}

class ValidadorStockCarrito {
    operator fun invoke(cantidad: Int, stock: Int): ResultadoValidacion = when {
        cantidad < 1 -> ResultadoValidacion.Invalido("Cantidad inválida")
        cantidad > stock -> ResultadoValidacion.Invalido("Stock insuficiente")
        else -> ResultadoValidacion.Valido
    }
}

class ValidadorNombre : Validador<String> {
    override operator fun invoke(valor: String) = ValidadorTextoMin3()(valor)
}

class ValidadorTitularTarjeta : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.trim().length >= 3) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Ingresa el nombre del titular")
}

class ValidadorTarjeta : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion {
        val digits = valor.filter(Char::isDigit)
        if (digits.length != 16) return ResultadoValidacion.Invalido("La tarjeta debe tener 16 dígitos")
        var sum = 0
        var alternate = false
        for (i in digits.lastIndex downTo 0) {
            var n = digits[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return if (sum % 10 == 0) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Número de tarjeta inválido")
    }
}

class ValidadorCvv : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.matches(Regex("\\d{3}"))) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("El CVV debe tener 3 dígitos")
}

class ValidadorIdPositivo : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.toLongOrNull()?.let { it > 0 } == true) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("El ID debe ser mayor que 0")
}

class ValidadorPrecio : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.toLongOrNull()?.let { it > 0 } == true) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("El precio debe ser mayor que 0")
}

class ValidadorStock : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.toIntOrNull()?.let { it >= 0 } == true) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("El stock debe ser un entero no negativo")
}

class ValidadorTextoMin3 : Validador<String> {
    override operator fun invoke(valor: String): ResultadoValidacion =
        if (valor.trim().length >= 3) ResultadoValidacion.Valido
        else ResultadoValidacion.Invalido("Debe tener al menos 3 caracteres")
}

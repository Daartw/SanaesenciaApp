package cl.sanaesencia.app.domain.model

import java.time.LocalDate

/**
 * IE 3.1.4 — Pronóstico de un día para la sede presencial de Los Vilos.
 *
 * Es el modelo de dominio que consume la interfaz: la respuesta cruda de la API externa
 * (Open-Meteo) se traduce a esta clase en el repositorio, de modo que la app nunca
 * depende del formato de un tercero.
 */
data class PronosticoDia(
    val fecha: LocalDate,
    val temperaturaMaxima: Int,
    val temperaturaMinima: Int,
    val probabilidadLluvia: Int,
    val codigoClima: Int,
) {

    /** Descripción en español del código WMO devuelto por la API. */
    val descripcion: String
        get() = descripcionClimaWmo(codigoClima)

    /** true cuando conviene avisar al paciente que lleve abrigo o paraguas. */
    val recomiendaPrecaucion: Boolean
        get() = probabilidadLluvia >= UMBRAL_LLUVIA || esCodigoDeLluvia(codigoClima)

    companion object {
        /** Desde este porcentaje de probabilidad se considera que puede llover. */
        const val UMBRAL_LLUVIA = 50
    }
}

/**
 * Traduce un código WMO (World Meteorological Organization) a una descripción legible.
 * Es la tabla oficial que publica Open-Meteo junto a su API.
 */
fun descripcionClimaWmo(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Neblina"
    51, 53, 55 -> "Llovizna"
    56, 57 -> "Llovizna helada"
    61, 63, 65 -> "Lluvia"
    66, 67 -> "Lluvia helada"
    71, 73, 75, 77 -> "Nieve"
    80, 81, 82 -> "Chubascos"
    85, 86 -> "Chubascos de nieve"
    95 -> "Tormenta eléctrica"
    96, 99 -> "Tormenta con granizo"
    else -> "Sin información"
}

/** true si el código WMO corresponde a algún tipo de precipitación. */
fun esCodigoDeLluvia(codigo: Int): Boolean =
    codigo in 51..67 || codigo in 80..82 || codigo in 95..99

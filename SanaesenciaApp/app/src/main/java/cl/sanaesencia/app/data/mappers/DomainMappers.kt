package cl.sanaesencia.app.data.mappers

import cl.sanaesencia.app.data.local.CitaEntity
import cl.sanaesencia.app.data.local.ProductoEntity
import cl.sanaesencia.app.data.local.ProfesionalEntity
import cl.sanaesencia.app.data.remote.CitaResponse
import cl.sanaesencia.app.data.remote.ProductoResponse
import cl.sanaesencia.app.data.remote.ProfesionalResponse
import cl.sanaesencia.app.domain.model.Producto
import cl.sanaesencia.app.domain.model.Profesional
import java.time.LocalDate
import java.time.LocalTime

fun ProfesionalEntity.toDomain(): Profesional {
    return Profesional(
        id = id,
        usuarioId = usuarioId,
        titulo = titulo,
        categoriaEspecialidad = categoria,
        descripcionEnfoque = descripcion,
        estado = estado
    )
}

fun ProductoEntity.toDomain(): Producto {
    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock,
        etiquetas = etiquetas
            .split(",")
            .filter { it.isNotBlank() }
    )
}

fun ProfesionalResponse.toEntity(): ProfesionalEntity {
    return ProfesionalEntity(
        id = id,
        usuarioId = usuarioId,
        titulo = titulo,
        categoria = categoriaEspecialidad,
        descripcion = descripcionEnfoque,
        estado = estado
    )
}

fun ProductoResponse.toEntity(): ProductoEntity {
    return ProductoEntity(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        stock = stock,
        etiquetas = etiquetasTerapeuticas
    )
}

fun CitaResponse.toEntity(): CitaEntity {
    return CitaEntity(
        id = id,
        pacienteId = pacienteId,
        profesionalId = profesionalId,
        boxId = boxId,
        fecha = runCatching { LocalDate.parse(fecha) }.getOrDefault(LocalDate.now()),
        inicio = runCatching { LocalTime.parse(horaInicio) }.getOrDefault(LocalTime.of(9, 0)),
        fin = runCatching { LocalTime.parse(horaFin) }.getOrDefault(LocalTime.of(10, 0)),
        modalidad = modalidad,
        estado = estado
    )
}

fun CitaEntity.toResponse(): CitaResponse {
    return CitaResponse(
        id = id,
        pacienteId = pacienteId,
        profesionalId = profesionalId,
        boxId = boxId,
        fecha = fecha.toString(),
        horaInicio = inicio.toString(),
        horaFin = fin.toString(),
        modalidad = modalidad,
        estado = estado
    )
}
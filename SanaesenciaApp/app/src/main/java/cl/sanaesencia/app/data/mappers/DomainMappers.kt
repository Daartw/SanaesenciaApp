package cl.sanaesencia.app.data.mappers

import cl.sanaesencia.app.data.local.ProfesionalEntity
import cl.sanaesencia.app.data.local.ProductoEntity
import cl.sanaesencia.app.data.remote.ProfesionalResponse
import cl.sanaesencia.app.data.remote.ProductoResponse
import cl.sanaesencia.app.domain.model.Profesional
import cl.sanaesencia.app.domain.model.Producto

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
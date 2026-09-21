package com.example.sanaesencia_app.domain.model

data class Paciente(
    val id: String,
    val nombre: String,
    val correo: String,
    val ultimaSesion: String?,
    val diasInactivo: Int
)

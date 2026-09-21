package com.example.sanaesencia_app.domain.model

data class Especialidad(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val profesional: String,
    val modalidades: List<Modalidad>
)

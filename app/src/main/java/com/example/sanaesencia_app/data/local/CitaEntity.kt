package com.example.sanaesencia_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
    val terapeutaId: String,
    val terapeutaNombre: String,
    val especialidad: String,
    val fecha: String,
    val hora: String,
    val modalidad: String,
    val boxAsignado: Int?,
    val estado: String
)

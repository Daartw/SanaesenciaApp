package com.example.sanaesencia_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas WHERE pacienteId = :pacienteId ORDER BY fecha ASC, hora ASC")
    fun getCitasByPaciente(pacienteId: String): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas ORDER BY fecha ASC, hora ASC")
    fun getTodasLasCitas(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas WHERE terapeutaId = :terapeutaId ORDER BY fecha ASC, hora ASC")
    fun getCitasByTerapeuta(terapeutaId: String): Flow<List<CitaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCita(cita: CitaEntity)

    @Update
    suspend fun updateCita(cita: CitaEntity)

    @Query("DELETE FROM citas WHERE id = :citaId")
    suspend fun deleteCita(citaId: String)
}

package cl.sanaesencia.app.data.local

import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.sanaesencia.app.domain.model.EstadoCita
import cl.sanaesencia.app.domain.model.EstadoProfesional
import cl.sanaesencia.app.domain.model.ModalidadCita
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profesionales")
data class ProfesionalEntity(
    @PrimaryKey val id: Long,
    val usuarioId: Long,
    val titulo: String,
    val categoria: String,
    val descripcion: String,
    val estado: EstadoProfesional,
)

@Entity(tableName = "boxes", indices = [Index(value = ["numero"], unique = true)])
data class BoxEntity(
    @PrimaryKey val id: Long,
    val numero: Int,
    val habilitado: Boolean,
)

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Long,
    val stock: Int,
    val etiquetas: String,
)

@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey val id: Long,
    val pacienteId: Long,
    val profesionalId: Long,
    val boxId: Long?,
    val fecha: LocalDate,
    val inicio: LocalTime,
    val fin: LocalTime,
    val modalidad: ModalidadCita,
    val estado: EstadoCita,
)

@Entity(tableName = "carrito_items", primaryKeys = ["productoId"])
data class CarritoItemEntity(
    val productoId: Long,
    val nombre: String,
    val precio: Long,
    val cantidad: Int,
    val stock: Int,
)

class Converters {
    @TypeConverter
    fun dateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun timeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun profesionalEstadoToString(value: EstadoProfesional): String = value.name

    @TypeConverter
    fun stringToProfesionalEstado(value: String): EstadoProfesional =
        EstadoProfesional.valueOf(value)

    @TypeConverter
    fun modalidadToString(value: ModalidadCita): String = value.name

    @TypeConverter
    fun stringToModalidad(value: String): ModalidadCita = ModalidadCita.valueOf(value)

    @TypeConverter
    fun citaEstadoToString(value: EstadoCita): String = value.name

    @TypeConverter
    fun stringToCitaEstado(value: String): EstadoCita = EstadoCita.valueOf(value)
}

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas WHERE pacienteId = :id ORDER BY fecha DESC, inicio DESC")
    fun historial(id: Long): Flow<List<CitaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CitaEntity>)
}

@Dao
interface ProfesionalDao {
    @Query("SELECT * FROM profesionales ORDER BY categoria")
    fun all(): Flow<List<ProfesionalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ProfesionalEntity>)
}

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY nombre")
    fun all(): Flow<List<ProductoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ProductoEntity>)
}

@Dao
interface CarritoDao {
    @Query("SELECT * FROM carrito_items ORDER BY nombre")
    fun all(): Flow<List<CarritoItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CarritoItemEntity)

    @Query("DELETE FROM carrito_items WHERE productoId = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM carrito_items")
    suspend fun clear()
}

@Database(
    entities = [
        ProfesionalEntity::class,
        BoxEntity::class,
        ProductoEntity::class,
        CitaEntity::class,
        CarritoItemEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citas(): CitaDao
    abstract fun profesionales(): ProfesionalDao
    abstract fun productos(): ProductoDao
    abstract fun carrito(): CarritoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE productos_new (
                        id INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        precio INTEGER NOT NULL,
                        stock INTEGER NOT NULL,
                        etiquetas TEXT NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO productos_new(id,nombre,descripcion,precio,stock,etiquetas)
                    SELECT id,nombre,descripcion,CAST(precio AS INTEGER),stock,etiquetas
                    FROM productos
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE productos")
                db.execSQL("ALTER TABLE productos_new RENAME TO productos")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS carrito_items (
                        productoId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        precio INTEGER NOT NULL,
                        cantidad INTEGER NOT NULL,
                        stock INTEGER NOT NULL,
                        PRIMARY KEY(productoId)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}

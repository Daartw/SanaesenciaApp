package cl.sanaesencia.app.data.local
import androidx.room.*
import java.time.LocalDate
import java.time.LocalTime
import cl.sanaesencia.app.domain.model.*
@Entity(tableName="profesionales")
data class ProfesionalEntity(@PrimaryKey val id:Long,val usuarioId:Long,val titulo:String,val categoria:String,val descripcion:String,val estado:EstadoProfesional)
@Entity(tableName="boxes",indices=[Index(value=["numero"],unique=true)])
data class BoxEntity(@PrimaryKey val id:Long,val numero:Int,val habilitado:Boolean)
@Entity(tableName="productos")
data class ProductoEntity(@PrimaryKey val id:Long,val nombre:String,val descripcion:String,val precio:Double,val stock:Int,val etiquetas:String)
@Entity(tableName="citas")
data class CitaEntity(@PrimaryKey val id:Long,val pacienteId:Long,val profesionalId:Long,val boxId:Long?,val fecha:LocalDate,val inicio:LocalTime,val fin:LocalTime,val modalidad:ModalidadCita,val estado:EstadoCita)
class Converters{
    @TypeConverter fun d(v:LocalDate?):String?=v?.toString()
    @TypeConverter fun d2(v:String?):LocalDate?=v?.let(LocalDate::parse)
    @TypeConverter fun t(v:LocalTime?):String?=v?.toString()
    @TypeConverter fun t2(v:String?):LocalTime?=v?.let(LocalTime::parse)
    @TypeConverter fun role(v:RolUsuario)=v.name
    @TypeConverter fun role2(v:String)=RolUsuario.valueOf(v)
    @TypeConverter fun ep(v:EstadoProfesional)=v.name
    @TypeConverter fun ep2(v:String)=EstadoProfesional.valueOf(v)
    @TypeConverter fun mc(v:ModalidadCita)=v.name
    @TypeConverter fun mc2(v:String)=ModalidadCita.valueOf(v)
    @TypeConverter fun ec(v:EstadoCita)=v.name
    @TypeConverter fun ec2(v:String)=EstadoCita.valueOf(v)
}
@Dao interface CitaDao{
    @Query("SELECT * FROM citas WHERE pacienteId=:id ORDER BY fecha DESC,inicio DESC")
    fun historial(id:Long):kotlinx.coroutines.flow.Flow<List<CitaEntity>>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertAll(items:List<CitaEntity>)
}
@Dao interface ProfesionalDao{
    @Query("SELECT * FROM profesionales ORDER BY categoria")
    fun all():kotlinx.coroutines.flow.Flow<List<ProfesionalEntity>>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertAll(items:List<ProfesionalEntity>)
}
@Dao interface ProductoDao{
    @Query("SELECT * FROM productos ORDER BY nombre")
    fun all():kotlinx.coroutines.flow.Flow<List<ProductoEntity>>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsertAll(items:List<ProductoEntity>)
}
@Database(entities=[ProfesionalEntity::class,BoxEntity::class,ProductoEntity::class,CitaEntity::class],version=1,exportSchema=true)
@TypeConverters(Converters::class)
abstract class AppDatabase:RoomDatabase(){
    abstract fun citas():CitaDao
    abstract fun profesionales():ProfesionalDao
    abstract fun productos():ProductoDao
}

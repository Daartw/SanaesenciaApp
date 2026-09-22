package com.example.sanaesencia_app.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CitaDao_Impl implements CitaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CitaEntity> __insertionAdapterOfCitaEntity;

  private final EntityDeletionOrUpdateAdapter<CitaEntity> __updateAdapterOfCitaEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCita;

  public CitaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCitaEntity = new EntityInsertionAdapter<CitaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `citas` (`id`,`pacienteId`,`terapeutaNombre`,`especialidad`,`fecha`,`hora`,`modalidad`,`boxAsignado`,`estado`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CitaEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPacienteId());
        statement.bindString(3, entity.getTerapeutaNombre());
        statement.bindString(4, entity.getEspecialidad());
        statement.bindString(5, entity.getFecha());
        statement.bindString(6, entity.getHora());
        statement.bindString(7, entity.getModalidad());
        if (entity.getBoxAsignado() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getBoxAsignado());
        }
        statement.bindString(9, entity.getEstado());
      }
    };
    this.__updateAdapterOfCitaEntity = new EntityDeletionOrUpdateAdapter<CitaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `citas` SET `id` = ?,`pacienteId` = ?,`terapeutaNombre` = ?,`especialidad` = ?,`fecha` = ?,`hora` = ?,`modalidad` = ?,`boxAsignado` = ?,`estado` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CitaEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPacienteId());
        statement.bindString(3, entity.getTerapeutaNombre());
        statement.bindString(4, entity.getEspecialidad());
        statement.bindString(5, entity.getFecha());
        statement.bindString(6, entity.getHora());
        statement.bindString(7, entity.getModalidad());
        if (entity.getBoxAsignado() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getBoxAsignado());
        }
        statement.bindString(9, entity.getEstado());
        statement.bindString(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCita = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM citas WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCita(final CitaEntity cita, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCitaEntity.insert(cita);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCita(final CitaEntity cita, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCitaEntity.handle(cita);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCita(final String citaId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCita.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, citaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCita.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CitaEntity>> getCitasByPaciente(final String pacienteId) {
    final String _sql = "SELECT * FROM citas WHERE pacienteId = ? ORDER BY fecha ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, pacienteId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"citas"}, new Callable<List<CitaEntity>>() {
      @Override
      @NonNull
      public List<CitaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPacienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "pacienteId");
          final int _cursorIndexOfTerapeutaNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "terapeutaNombre");
          final int _cursorIndexOfEspecialidad = CursorUtil.getColumnIndexOrThrow(_cursor, "especialidad");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfModalidad = CursorUtil.getColumnIndexOrThrow(_cursor, "modalidad");
          final int _cursorIndexOfBoxAsignado = CursorUtil.getColumnIndexOrThrow(_cursor, "boxAsignado");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final List<CitaEntity> _result = new ArrayList<CitaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CitaEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPacienteId;
            _tmpPacienteId = _cursor.getString(_cursorIndexOfPacienteId);
            final String _tmpTerapeutaNombre;
            _tmpTerapeutaNombre = _cursor.getString(_cursorIndexOfTerapeutaNombre);
            final String _tmpEspecialidad;
            _tmpEspecialidad = _cursor.getString(_cursorIndexOfEspecialidad);
            final String _tmpFecha;
            _tmpFecha = _cursor.getString(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpModalidad;
            _tmpModalidad = _cursor.getString(_cursorIndexOfModalidad);
            final Integer _tmpBoxAsignado;
            if (_cursor.isNull(_cursorIndexOfBoxAsignado)) {
              _tmpBoxAsignado = null;
            } else {
              _tmpBoxAsignado = _cursor.getInt(_cursorIndexOfBoxAsignado);
            }
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            _item = new CitaEntity(_tmpId,_tmpPacienteId,_tmpTerapeutaNombre,_tmpEspecialidad,_tmpFecha,_tmpHora,_tmpModalidad,_tmpBoxAsignado,_tmpEstado);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

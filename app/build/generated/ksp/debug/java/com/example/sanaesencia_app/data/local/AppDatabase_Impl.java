package com.example.sanaesencia_app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CitaDao _citaDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `citas` (`id` TEXT NOT NULL, `pacienteId` TEXT NOT NULL, `terapeutaNombre` TEXT NOT NULL, `especialidad` TEXT NOT NULL, `fecha` TEXT NOT NULL, `hora` TEXT NOT NULL, `modalidad` TEXT NOT NULL, `boxAsignado` INTEGER, `estado` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '65ebbac2ccbf8587319300b8361b5808')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `citas`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCitas = new HashMap<String, TableInfo.Column>(9);
        _columnsCitas.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("pacienteId", new TableInfo.Column("pacienteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("terapeutaNombre", new TableInfo.Column("terapeutaNombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("especialidad", new TableInfo.Column("especialidad", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("fecha", new TableInfo.Column("fecha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("hora", new TableInfo.Column("hora", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("modalidad", new TableInfo.Column("modalidad", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("boxAsignado", new TableInfo.Column("boxAsignado", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("estado", new TableInfo.Column("estado", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCitas = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCitas = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCitas = new TableInfo("citas", _columnsCitas, _foreignKeysCitas, _indicesCitas);
        final TableInfo _existingCitas = TableInfo.read(db, "citas");
        if (!_infoCitas.equals(_existingCitas)) {
          return new RoomOpenHelper.ValidationResult(false, "citas(com.example.sanaesencia_app.data.local.CitaEntity).\n"
                  + " Expected:\n" + _infoCitas + "\n"
                  + " Found:\n" + _existingCitas);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "65ebbac2ccbf8587319300b8361b5808", "a6af1bfb919c46a4809c522b8c0dd1b0");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "citas");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `citas`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CitaDao.class, CitaDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CitaDao citaDao() {
    if (_citaDao != null) {
      return _citaDao;
    } else {
      synchronized(this) {
        if(_citaDao == null) {
          _citaDao = new CitaDao_Impl(this);
        }
        return _citaDao;
      }
    }
  }
}

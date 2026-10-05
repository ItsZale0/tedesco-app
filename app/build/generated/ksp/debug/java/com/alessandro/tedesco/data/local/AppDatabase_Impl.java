package com.alessandro.tedesco.data.local;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile WordDao _wordDao;

  private volatile ReviewDao _reviewDao;

  private volatile FeedLogDao _feedLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `words` (`id` TEXT NOT NULL, `german` TEXT NOT NULL, `italian` TEXT NOT NULL, `example` TEXT NOT NULL, `article` TEXT, `pronunciation` TEXT, `level` TEXT NOT NULL, `lesson` INTEGER NOT NULL, `tags` TEXT NOT NULL, `archived` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reviews` (`wordId` TEXT NOT NULL, `easeFactor` REAL NOT NULL, `intervalDays` INTEGER NOT NULL, `repetitions` INTEGER NOT NULL, `lapses` INTEGER NOT NULL, `dueAt` INTEGER NOT NULL, `lastReviewedAt` INTEGER, PRIMARY KEY(`wordId`), FOREIGN KEY(`wordId`) REFERENCES `words`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_wordId` ON `reviews` (`wordId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reviews_dueAt` ON `reviews` (`dueAt`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `feed_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncedAt` INTEGER NOT NULL, `newWords` INTEGER NOT NULL, `updatedWords` INTEGER NOT NULL, `status` TEXT NOT NULL, `message` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_feed_log_syncedAt` ON `feed_log` (`syncedAt`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '57206ed7d8d4aa0da7a732c45e451764')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `words`");
        db.execSQL("DROP TABLE IF EXISTS `reviews`");
        db.execSQL("DROP TABLE IF EXISTS `feed_log`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsWords = new HashMap<String, TableInfo.Column>(11);
        _columnsWords.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("german", new TableInfo.Column("german", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("italian", new TableInfo.Column("italian", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("example", new TableInfo.Column("example", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("article", new TableInfo.Column("article", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("pronunciation", new TableInfo.Column("pronunciation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("level", new TableInfo.Column("level", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("lesson", new TableInfo.Column("lesson", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("archived", new TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWords = new TableInfo("words", _columnsWords, _foreignKeysWords, _indicesWords);
        final TableInfo _existingWords = TableInfo.read(db, "words");
        if (!_infoWords.equals(_existingWords)) {
          return new RoomOpenHelper.ValidationResult(false, "words(com.alessandro.tedesco.data.local.WordEntity).\n"
                  + " Expected:\n" + _infoWords + "\n"
                  + " Found:\n" + _existingWords);
        }
        final HashMap<String, TableInfo.Column> _columnsReviews = new HashMap<String, TableInfo.Column>(7);
        _columnsReviews.put("wordId", new TableInfo.Column("wordId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviews.put("easeFactor", new TableInfo.Column("easeFactor", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviews.put("intervalDays", new TableInfo.Column("intervalDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviews.put("repetitions", new TableInfo.Column("repetitions", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviews.put("lapses", new TableInfo.Column("lapses", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviews.put("dueAt", new TableInfo.Column("dueAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviews.put("lastReviewedAt", new TableInfo.Column("lastReviewedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReviews = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysReviews.add(new TableInfo.ForeignKey("words", "CASCADE", "NO ACTION", Arrays.asList("wordId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReviews = new HashSet<TableInfo.Index>(2);
        _indicesReviews.add(new TableInfo.Index("index_reviews_wordId", false, Arrays.asList("wordId"), Arrays.asList("ASC")));
        _indicesReviews.add(new TableInfo.Index("index_reviews_dueAt", false, Arrays.asList("dueAt"), Arrays.asList("ASC")));
        final TableInfo _infoReviews = new TableInfo("reviews", _columnsReviews, _foreignKeysReviews, _indicesReviews);
        final TableInfo _existingReviews = TableInfo.read(db, "reviews");
        if (!_infoReviews.equals(_existingReviews)) {
          return new RoomOpenHelper.ValidationResult(false, "reviews(com.alessandro.tedesco.data.local.ReviewEntity).\n"
                  + " Expected:\n" + _infoReviews + "\n"
                  + " Found:\n" + _existingReviews);
        }
        final HashMap<String, TableInfo.Column> _columnsFeedLog = new HashMap<String, TableInfo.Column>(6);
        _columnsFeedLog.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedLog.put("syncedAt", new TableInfo.Column("syncedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedLog.put("newWords", new TableInfo.Column("newWords", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedLog.put("updatedWords", new TableInfo.Column("updatedWords", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedLog.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedLog.put("message", new TableInfo.Column("message", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFeedLog = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFeedLog = new HashSet<TableInfo.Index>(1);
        _indicesFeedLog.add(new TableInfo.Index("index_feed_log_syncedAt", false, Arrays.asList("syncedAt"), Arrays.asList("ASC")));
        final TableInfo _infoFeedLog = new TableInfo("feed_log", _columnsFeedLog, _foreignKeysFeedLog, _indicesFeedLog);
        final TableInfo _existingFeedLog = TableInfo.read(db, "feed_log");
        if (!_infoFeedLog.equals(_existingFeedLog)) {
          return new RoomOpenHelper.ValidationResult(false, "feed_log(com.alessandro.tedesco.data.local.FeedLogEntity).\n"
                  + " Expected:\n" + _infoFeedLog + "\n"
                  + " Found:\n" + _existingFeedLog);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "57206ed7d8d4aa0da7a732c45e451764", "5adfe6388038d08e435daaed4e3ad3b5");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "words","reviews","feed_log");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `words`");
      _db.execSQL("DELETE FROM `reviews`");
      _db.execSQL("DELETE FROM `feed_log`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(WordDao.class, WordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReviewDao.class, ReviewDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FeedLogDao.class, FeedLogDao_Impl.getRequiredConverters());
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
  public WordDao wordDao() {
    if (_wordDao != null) {
      return _wordDao;
    } else {
      synchronized(this) {
        if(_wordDao == null) {
          _wordDao = new WordDao_Impl(this);
        }
        return _wordDao;
      }
    }
  }

  @Override
  public ReviewDao reviewDao() {
    if (_reviewDao != null) {
      return _reviewDao;
    } else {
      synchronized(this) {
        if(_reviewDao == null) {
          _reviewDao = new ReviewDao_Impl(this);
        }
        return _reviewDao;
      }
    }
  }

  @Override
  public FeedLogDao feedLogDao() {
    if (_feedLogDao != null) {
      return _feedLogDao;
    } else {
      synchronized(this) {
        if(_feedLogDao == null) {
          _feedLogDao = new FeedLogDao_Impl(this);
        }
        return _feedLogDao;
      }
    }
  }
}

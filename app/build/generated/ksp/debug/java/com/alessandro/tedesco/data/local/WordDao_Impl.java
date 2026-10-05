package com.alessandro.tedesco.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
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
public final class WordDao_Impl implements WordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordEntity> __insertionAdapterOfWordEntity;

  private final SharedSQLiteStatement __preparedStmtOfSetArchived;

  public WordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordEntity = new EntityInsertionAdapter<WordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `words` (`id`,`german`,`italian`,`example`,`article`,`pronunciation`,`level`,`lesson`,`tags`,`archived`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getGerman());
        statement.bindString(3, entity.getItalian());
        statement.bindString(4, entity.getExample());
        if (entity.getArticle() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getArticle());
        }
        if (entity.getPronunciation() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPronunciation());
        }
        statement.bindString(7, entity.getLevel());
        statement.bindLong(8, entity.getLesson());
        statement.bindString(9, entity.getTags());
        final int _tmp = entity.getArchived() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfSetArchived = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE words SET archived = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<WordEntity> words,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordEntity.insert(words);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setArchived(final String id, final boolean archived,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetArchived.acquire();
        int _argIndex = 1;
        final int _tmp = archived ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetArchived.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final String id, final Continuation<? super WordEntity> $completion) {
    final String _sql = "SELECT * FROM words WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WordEntity>() {
      @Override
      @Nullable
      public WordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGerman = CursorUtil.getColumnIndexOrThrow(_cursor, "german");
          final int _cursorIndexOfItalian = CursorUtil.getColumnIndexOrThrow(_cursor, "italian");
          final int _cursorIndexOfExample = CursorUtil.getColumnIndexOrThrow(_cursor, "example");
          final int _cursorIndexOfArticle = CursorUtil.getColumnIndexOrThrow(_cursor, "article");
          final int _cursorIndexOfPronunciation = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciation");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfLesson = CursorUtil.getColumnIndexOrThrow(_cursor, "lesson");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final WordEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpGerman;
            _tmpGerman = _cursor.getString(_cursorIndexOfGerman);
            final String _tmpItalian;
            _tmpItalian = _cursor.getString(_cursorIndexOfItalian);
            final String _tmpExample;
            _tmpExample = _cursor.getString(_cursorIndexOfExample);
            final String _tmpArticle;
            if (_cursor.isNull(_cursorIndexOfArticle)) {
              _tmpArticle = null;
            } else {
              _tmpArticle = _cursor.getString(_cursorIndexOfArticle);
            }
            final String _tmpPronunciation;
            if (_cursor.isNull(_cursorIndexOfPronunciation)) {
              _tmpPronunciation = null;
            } else {
              _tmpPronunciation = _cursor.getString(_cursorIndexOfPronunciation);
            }
            final String _tmpLevel;
            _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
            final int _tmpLesson;
            _tmpLesson = _cursor.getInt(_cursorIndexOfLesson);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfArchived);
            _tmpArchived = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new WordEntity(_tmpId,_tmpGerman,_tmpItalian,_tmpExample,_tmpArticle,_tmpPronunciation,_tmpLevel,_tmpLesson,_tmpTags,_tmpArchived,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WordEntity>> observeAll() {
    final String _sql = "SELECT * FROM words WHERE archived = 0 ORDER BY lesson, german";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"words"}, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGerman = CursorUtil.getColumnIndexOrThrow(_cursor, "german");
          final int _cursorIndexOfItalian = CursorUtil.getColumnIndexOrThrow(_cursor, "italian");
          final int _cursorIndexOfExample = CursorUtil.getColumnIndexOrThrow(_cursor, "example");
          final int _cursorIndexOfArticle = CursorUtil.getColumnIndexOrThrow(_cursor, "article");
          final int _cursorIndexOfPronunciation = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciation");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfLesson = CursorUtil.getColumnIndexOrThrow(_cursor, "lesson");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpGerman;
            _tmpGerman = _cursor.getString(_cursorIndexOfGerman);
            final String _tmpItalian;
            _tmpItalian = _cursor.getString(_cursorIndexOfItalian);
            final String _tmpExample;
            _tmpExample = _cursor.getString(_cursorIndexOfExample);
            final String _tmpArticle;
            if (_cursor.isNull(_cursorIndexOfArticle)) {
              _tmpArticle = null;
            } else {
              _tmpArticle = _cursor.getString(_cursorIndexOfArticle);
            }
            final String _tmpPronunciation;
            if (_cursor.isNull(_cursorIndexOfPronunciation)) {
              _tmpPronunciation = null;
            } else {
              _tmpPronunciation = _cursor.getString(_cursorIndexOfPronunciation);
            }
            final String _tmpLevel;
            _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
            final int _tmpLesson;
            _tmpLesson = _cursor.getInt(_cursorIndexOfLesson);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfArchived);
            _tmpArchived = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WordEntity(_tmpId,_tmpGerman,_tmpItalian,_tmpExample,_tmpArticle,_tmpPronunciation,_tmpLevel,_tmpLesson,_tmpTags,_tmpArchived,_tmpCreatedAt);
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

  @Override
  public Flow<List<WordEntity>> observeByLesson(final int lesson) {
    final String _sql = "SELECT * FROM words WHERE archived = 0 AND lesson = ? ORDER BY german";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, lesson);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"words"}, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGerman = CursorUtil.getColumnIndexOrThrow(_cursor, "german");
          final int _cursorIndexOfItalian = CursorUtil.getColumnIndexOrThrow(_cursor, "italian");
          final int _cursorIndexOfExample = CursorUtil.getColumnIndexOrThrow(_cursor, "example");
          final int _cursorIndexOfArticle = CursorUtil.getColumnIndexOrThrow(_cursor, "article");
          final int _cursorIndexOfPronunciation = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciation");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfLesson = CursorUtil.getColumnIndexOrThrow(_cursor, "lesson");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpGerman;
            _tmpGerman = _cursor.getString(_cursorIndexOfGerman);
            final String _tmpItalian;
            _tmpItalian = _cursor.getString(_cursorIndexOfItalian);
            final String _tmpExample;
            _tmpExample = _cursor.getString(_cursorIndexOfExample);
            final String _tmpArticle;
            if (_cursor.isNull(_cursorIndexOfArticle)) {
              _tmpArticle = null;
            } else {
              _tmpArticle = _cursor.getString(_cursorIndexOfArticle);
            }
            final String _tmpPronunciation;
            if (_cursor.isNull(_cursorIndexOfPronunciation)) {
              _tmpPronunciation = null;
            } else {
              _tmpPronunciation = _cursor.getString(_cursorIndexOfPronunciation);
            }
            final String _tmpLevel;
            _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
            final int _tmpLesson;
            _tmpLesson = _cursor.getInt(_cursorIndexOfLesson);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfArchived);
            _tmpArchived = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WordEntity(_tmpId,_tmpGerman,_tmpItalian,_tmpExample,_tmpArticle,_tmpPronunciation,_tmpLevel,_tmpLesson,_tmpTags,_tmpArchived,_tmpCreatedAt);
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

  @Override
  public Flow<List<Integer>> observeLessons() {
    final String _sql = "SELECT DISTINCT lesson FROM words WHERE archived = 0 ORDER BY lesson";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"words"}, new Callable<List<Integer>>() {
      @Override
      @NonNull
      public List<Integer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Integer> _result = new ArrayList<Integer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Integer _item;
            _item = _cursor.getInt(0);
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

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM words WHERE archived = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

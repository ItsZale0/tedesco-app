package com.alessandro.tedesco.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
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
import java.lang.Long;
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
public final class ReviewDao_Impl implements ReviewDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReviewEntity> __insertionAdapterOfReviewEntity;

  private final EntityDeletionOrUpdateAdapter<ReviewEntity> __updateAdapterOfReviewEntity;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public ReviewDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReviewEntity = new EntityInsertionAdapter<ReviewEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reviews` (`wordId`,`easeFactor`,`intervalDays`,`repetitions`,`lapses`,`dueAt`,`lastReviewedAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReviewEntity entity) {
        statement.bindString(1, entity.getWordId());
        statement.bindDouble(2, entity.getEaseFactor());
        statement.bindLong(3, entity.getIntervalDays());
        statement.bindLong(4, entity.getRepetitions());
        statement.bindLong(5, entity.getLapses());
        statement.bindLong(6, entity.getDueAt());
        if (entity.getLastReviewedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastReviewedAt());
        }
      }
    };
    this.__updateAdapterOfReviewEntity = new EntityDeletionOrUpdateAdapter<ReviewEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reviews` SET `wordId` = ?,`easeFactor` = ?,`intervalDays` = ?,`repetitions` = ?,`lapses` = ?,`dueAt` = ?,`lastReviewedAt` = ? WHERE `wordId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReviewEntity entity) {
        statement.bindString(1, entity.getWordId());
        statement.bindDouble(2, entity.getEaseFactor());
        statement.bindLong(3, entity.getIntervalDays());
        statement.bindLong(4, entity.getRepetitions());
        statement.bindLong(5, entity.getLapses());
        statement.bindLong(6, entity.getDueAt());
        if (entity.getLastReviewedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastReviewedAt());
        }
        statement.bindString(8, entity.getWordId());
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reviews";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final ReviewEntity review, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReviewEntity.insert(review);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReviewEntity review, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReviewEntity.handle(review);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
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
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object dueNow(final long now, final Continuation<? super List<ReviewEntity>> $completion) {
    final String _sql = "SELECT * FROM reviews WHERE dueAt <= ? ORDER BY dueAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewEntity>>() {
      @Override
      @NonNull
      public List<ReviewEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfEaseFactor = CursorUtil.getColumnIndexOrThrow(_cursor, "easeFactor");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfRepetitions = CursorUtil.getColumnIndexOrThrow(_cursor, "repetitions");
          final int _cursorIndexOfLapses = CursorUtil.getColumnIndexOrThrow(_cursor, "lapses");
          final int _cursorIndexOfDueAt = CursorUtil.getColumnIndexOrThrow(_cursor, "dueAt");
          final int _cursorIndexOfLastReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewedAt");
          final List<ReviewEntity> _result = new ArrayList<ReviewEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewEntity _item;
            final String _tmpWordId;
            _tmpWordId = _cursor.getString(_cursorIndexOfWordId);
            final float _tmpEaseFactor;
            _tmpEaseFactor = _cursor.getFloat(_cursorIndexOfEaseFactor);
            final int _tmpIntervalDays;
            _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            final int _tmpRepetitions;
            _tmpRepetitions = _cursor.getInt(_cursorIndexOfRepetitions);
            final int _tmpLapses;
            _tmpLapses = _cursor.getInt(_cursorIndexOfLapses);
            final long _tmpDueAt;
            _tmpDueAt = _cursor.getLong(_cursorIndexOfDueAt);
            final Long _tmpLastReviewedAt;
            if (_cursor.isNull(_cursorIndexOfLastReviewedAt)) {
              _tmpLastReviewedAt = null;
            } else {
              _tmpLastReviewedAt = _cursor.getLong(_cursorIndexOfLastReviewedAt);
            }
            _item = new ReviewEntity(_tmpWordId,_tmpEaseFactor,_tmpIntervalDays,_tmpRepetitions,_tmpLapses,_tmpDueAt,_tmpLastReviewedAt);
            _result.add(_item);
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
  public Flow<Integer> observeDueCount(final long now) {
    final String _sql = "SELECT COUNT(*) FROM reviews WHERE dueAt <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reviews"}, new Callable<Integer>() {
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
    final String _sql = "SELECT COUNT(*) FROM reviews";
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

  @Override
  public Object all(final Continuation<? super List<ReviewEntity>> $completion) {
    final String _sql = "SELECT * FROM reviews";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewEntity>>() {
      @Override
      @NonNull
      public List<ReviewEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfEaseFactor = CursorUtil.getColumnIndexOrThrow(_cursor, "easeFactor");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfRepetitions = CursorUtil.getColumnIndexOrThrow(_cursor, "repetitions");
          final int _cursorIndexOfLapses = CursorUtil.getColumnIndexOrThrow(_cursor, "lapses");
          final int _cursorIndexOfDueAt = CursorUtil.getColumnIndexOrThrow(_cursor, "dueAt");
          final int _cursorIndexOfLastReviewedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewedAt");
          final List<ReviewEntity> _result = new ArrayList<ReviewEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewEntity _item;
            final String _tmpWordId;
            _tmpWordId = _cursor.getString(_cursorIndexOfWordId);
            final float _tmpEaseFactor;
            _tmpEaseFactor = _cursor.getFloat(_cursorIndexOfEaseFactor);
            final int _tmpIntervalDays;
            _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            final int _tmpRepetitions;
            _tmpRepetitions = _cursor.getInt(_cursorIndexOfRepetitions);
            final int _tmpLapses;
            _tmpLapses = _cursor.getInt(_cursorIndexOfLapses);
            final long _tmpDueAt;
            _tmpDueAt = _cursor.getLong(_cursorIndexOfDueAt);
            final Long _tmpLastReviewedAt;
            if (_cursor.isNull(_cursorIndexOfLastReviewedAt)) {
              _tmpLastReviewedAt = null;
            } else {
              _tmpLastReviewedAt = _cursor.getLong(_cursorIndexOfLastReviewedAt);
            }
            _item = new ReviewEntity(_tmpWordId,_tmpEaseFactor,_tmpIntervalDays,_tmpRepetitions,_tmpLapses,_tmpDueAt,_tmpLastReviewedAt);
            _result.add(_item);
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

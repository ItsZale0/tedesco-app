package com.alessandro.tedesco.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class FeedLogDao_Impl implements FeedLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FeedLogEntity> __insertionAdapterOfFeedLogEntity;

  public FeedLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFeedLogEntity = new EntityInsertionAdapter<FeedLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `feed_log` (`id`,`syncedAt`,`newWords`,`updatedWords`,`status`,`message`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FeedLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSyncedAt());
        statement.bindLong(3, entity.getNewWords());
        statement.bindLong(4, entity.getUpdatedWords());
        statement.bindString(5, entity.getStatus());
        if (entity.getMessage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getMessage());
        }
      }
    };
  }

  @Override
  public Object insert(final FeedLogEntity entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFeedLogEntity.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<FeedLogEntity> observeLast() {
    final String _sql = "SELECT * FROM feed_log ORDER BY syncedAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"feed_log"}, new Callable<FeedLogEntity>() {
      @Override
      @Nullable
      public FeedLogEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAt");
          final int _cursorIndexOfNewWords = CursorUtil.getColumnIndexOrThrow(_cursor, "newWords");
          final int _cursorIndexOfUpdatedWords = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedWords");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final FeedLogEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSyncedAt;
            _tmpSyncedAt = _cursor.getLong(_cursorIndexOfSyncedAt);
            final int _tmpNewWords;
            _tmpNewWords = _cursor.getInt(_cursorIndexOfNewWords);
            final int _tmpUpdatedWords;
            _tmpUpdatedWords = _cursor.getInt(_cursorIndexOfUpdatedWords);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            _result = new FeedLogEntity(_tmpId,_tmpSyncedAt,_tmpNewWords,_tmpUpdatedWords,_tmpStatus,_tmpMessage);
          } else {
            _result = null;
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
  public Object recent(final Continuation<? super List<FeedLogEntity>> $completion) {
    final String _sql = "SELECT * FROM feed_log ORDER BY syncedAt DESC LIMIT 30";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FeedLogEntity>>() {
      @Override
      @NonNull
      public List<FeedLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAt");
          final int _cursorIndexOfNewWords = CursorUtil.getColumnIndexOrThrow(_cursor, "newWords");
          final int _cursorIndexOfUpdatedWords = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedWords");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final List<FeedLogEntity> _result = new ArrayList<FeedLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FeedLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSyncedAt;
            _tmpSyncedAt = _cursor.getLong(_cursorIndexOfSyncedAt);
            final int _tmpNewWords;
            _tmpNewWords = _cursor.getInt(_cursorIndexOfNewWords);
            final int _tmpUpdatedWords;
            _tmpUpdatedWords = _cursor.getInt(_cursorIndexOfUpdatedWords);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            _item = new FeedLogEntity(_tmpId,_tmpSyncedAt,_tmpNewWords,_tmpUpdatedWords,_tmpStatus,_tmpMessage);
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

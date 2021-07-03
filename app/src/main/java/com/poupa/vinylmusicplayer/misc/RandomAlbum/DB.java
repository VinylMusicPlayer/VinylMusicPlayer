package com.poupa.vinylmusicplayer.misc.RandomAlbum;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.App;


public class DB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "next_random_album.db";
    private static final int VERSION = 5;

    DB() {
        super(App.getInstance().getApplicationContext(), DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ListenHistoryColumns.NAME + " ("
                        + ListenHistoryColumns._ID + " INTEGER PRIMARY KEY,"
                        + ListenHistoryColumns.ALBUM_ID + " LONG NOT NULL"
                        + ");"
        );
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NextRandomAlbumIdColumns.NAME + " ("
                + ListenHistoryColumns._ID + " INTEGER PRIMARY KEY,"
                + NextRandomAlbumIdColumns.ALBUM_ID + " LONG NOT NULL"
                + ");"
        );
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ListenHistoryColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NextRandomAlbumIdColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ListenHistoryColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NextRandomAlbumIdColumns.NAME);
        onCreate(db);
    }

    synchronized void clear() {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            db.delete(ListenHistoryColumns.NAME, null, null);
            db.delete(NextRandomAlbumIdColumns.NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void removeFirstAlbumOfHistory() {
        try (final SQLiteDatabase db = getWritableDatabase()) {

            //String selection = ListenHistoryColumns._ID + " LIKE ?";
            //String[] selectionArgs = { "1" };
            //db.delete(ListenHistoryColumns.NAME, selection, selectionArgs);
            String DELETE_FIRST_ELEMENT = "DELETE FROM " + ListenHistoryColumns.NAME + " WHERE " + ListenHistoryColumns._ID + " IN " +
                    "(SELECT " + ListenHistoryColumns._ID + " FROM " + ListenHistoryColumns.NAME + " ORDER BY " + ListenHistoryColumns._ID + " LIMIT 1)";
            db.execSQL(DELETE_FIRST_ELEMENT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void addIdToHistory(@NonNull Long albumId) {
        try (final SQLiteDatabase db = getWritableDatabase()) {

            final ContentValues values = new ContentValues();
            values.put(ListenHistoryColumns.ALBUM_ID, albumId);

            db.insert(ListenHistoryColumns.NAME, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    synchronized List<Long> fetchAllListenHistory() {
        ArrayList<Long> listenHistory = new ArrayList<>();
        final SQLiteDatabase database = getReadableDatabase();

        try (final Cursor cursor = database.query(ListenHistoryColumns.NAME,
                new String[]{
                        ListenHistoryColumns.ALBUM_ID
                },
                null,
                null,
                null,
                null,
                null))
        {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int columnIndex = -1;
                    final long albumId = cursor.getLong(++columnIndex);

                    listenHistory.add(albumId);
                } while (cursor.moveToNext());
            }

            if (cursor != null)
                cursor.close();

            return listenHistory;
        }
    }

    synchronized void setNextRandomAlbumId(@NonNull Long albumId) {
        try (final SQLiteDatabase db = getWritableDatabase()) {

            //replace current album id
            final ContentValues values = new ContentValues();
            values.put(NextRandomAlbumIdColumns._ID, 0);
            values.put(NextRandomAlbumIdColumns.ALBUM_ID, albumId);
            db.replace(NextRandomAlbumIdColumns.NAME, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    synchronized Long fetchNextRandomAlbumId() {
        Long nextRandomAlbums = (long)0;
        final SQLiteDatabase database = getReadableDatabase();

        try (final Cursor cursor = database.query(NextRandomAlbumIdColumns.NAME,
                new String[]{
                        NextRandomAlbumIdColumns.ALBUM_ID
                },
                null,
                null,
                null,
                null,
                null))
        {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int columnIndex = -1;
                    final long albumId = cursor.getLong(++columnIndex);

                    nextRandomAlbums = albumId;
                } while (cursor.moveToNext());
            }

            if (cursor != null)
                cursor.close();

            return nextRandomAlbums;
        }
    }


    public static class ListenHistoryColumns implements BaseColumns {
        public static final String NAME = "listenHistory";

        public static final String ALBUM_ID = "album_id";
    }

    public static class NextRandomAlbumIdColumns implements BaseColumns {
        public static final String NAME = "nextRandomAlbumId";

        public static final String ALBUM_ID = "album_id";
    }
}

/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.poupa.vinylmusicplayer.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.IndexedSong;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Andrew Neal, modified by Karim Abou Zeid
 *
 * This keeps track of the music playback and history state of the playback service
 */
public class MusicPlaybackQueueStore extends SQLiteOpenHelper {
    @Nullable
    private static MusicPlaybackQueueStore sInstance = null;
    private static final String DATABASE_NAME = "music_playback_state.db";
    private static final String PLAYING_QUEUE_TABLE_NAME = "playing_queue";
    private static final String ORIGINAL_PLAYING_QUEUE_TABLE_NAME = "original_playing_queue";
    private static final int VERSION = 7;

    /**
     * Constructor of <code>MusicPlaybackState</code>
     *
     * @param context The {@link Context} to use
     */
    private MusicPlaybackQueueStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        createTable(db, PLAYING_QUEUE_TABLE_NAME);
        createTable(db, ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
    }

    private static void createTable(@NonNull final SQLiteDatabase db, @NonNull final String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                tableName +
                " (" +
                BaseColumns._ID + " LONG NOT NULL, " +
                MusicPlaybackColumns.INDEX_IN_QUEUE + " INT NOT NULL " +
                ");");
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        migrateDB(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        migrateDB(db, oldVersion, newVersion);
    }

    private void migrateDB(@NonNull final SQLiteDatabase dbase, final int oldVersion, final int newVersion) {
        final List<String> tableNames = List.of(PLAYING_QUEUE_TABLE_NAME, ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
        final Consumer<SQLiteDatabase> migrateResetAll = (db) -> {
            for (final String tableName : tableNames) {
                db.execSQL("DROP TABLE IF EXISTS " + tableName);
            }
            onCreate(db);
        };
        final Consumer<SQLiteDatabase> migrateFromV6ToV7 = (db) -> {
            for (final String tableName : tableNames) {
                // From v6 to v7, only unused columns are dropped
                // Create a temp table, copy the data over and rename it
                // Cannot use 'ALTER TABLE ... DROP COLUMN ...' due to SQLite limitation
                final String tempName = tableName + "_V7";
                createTable(db, tempName);
                dbase.execSQL(String.format("INSERT INTO %s SELECT %s, %s FROM %s;",
                        tempName, BaseColumns._ID, MusicPlaybackColumns.INDEX_IN_QUEUE, tableName));
                dbase.execSQL(String.format("DROP TABLE %s;", tableName));
                dbase.execSQL(String.format("ALTER TABLE %s RENAME TO %s", tempName, tableName));
            }
        };
        final Consumer<SQLiteDatabase> migrateUnsupported = (db) -> {
            final String message = String.format("Unsupported migration version %s -> %s of database %s", oldVersion, newVersion, DATABASE_NAME);
            throw new IllegalStateException(message);
        };

        if (oldVersion < newVersion)
        {
            // Upgrade path
            switch (oldVersion) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    migrateResetAll.accept(dbase);
                    break;

                case 6:
                case VERSION: // At target. This case is here for consistency check
                    migrateFromV6ToV7.accept(dbase);
                    break;

                default:
                    migrateUnsupported.accept(dbase);
                    break;
            }
        } else {
            // Downgrade path - downgrading is often impossible
            migrateResetAll.accept(dbase);
        }
    }

    /**
     * @param context The {@link Context} to use
     * @return A new instance of this class.
     */
    @NonNull
    public static synchronized MusicPlaybackQueueStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlaybackQueueStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public synchronized void saveQueues(@NonNull final ArrayList<IndexedSong> playingQueue, @NonNull final ArrayList<IndexedSong> originalPlayingQueue) {
        saveQueue(PLAYING_QUEUE_TABLE_NAME, playingQueue);
        saveQueue(ORIGINAL_PLAYING_QUEUE_TABLE_NAME, originalPlayingQueue);
    }

    /**
     * Clears the existing database and saves the queue into the db so that when the
     * app is restarted, the tracks you were listening to is restored
     *
     * @param queue the queue to save
     */
    private synchronized void saveQueue(final String tableName, @NonNull final ArrayList<IndexedSong> queue) {
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            database.delete(tableName, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) {
                    Song song = queue.get(i);
                    int indexInQueue = queue.get(i).index;
                    ContentValues values = new ContentValues();

                    values.put(BaseColumns._ID, song.id);
                    values.put(MusicPlaybackColumns.INDEX_IN_QUEUE, indexInQueue);

                    database.insert(tableName, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }
    }

    @NonNull
    public ArrayList<IndexedSong> getSavedPlayingQueue() {
        return getQueue(PLAYING_QUEUE_TABLE_NAME);
    }

    @NonNull
    public ArrayList<IndexedSong> getSavedOriginalPlayingQueue() {
        return getQueue(ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
    }


    @NonNull
    private ArrayList<IndexedSong> getSongPosition(@Nullable Cursor cursor, @NonNull final ArrayList<Song> songs, @NonNull final ArrayList<Long> removedSongIds) {
        ArrayList<IndexedSong> queue = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int i = 0;
            int idColumn = cursor.getColumnIndex(BaseColumns._ID);
            int indexColumn = cursor.getColumnIndex(MusicPlaybackColumns.INDEX_IN_QUEUE);

            do {
                long songId = cursor.getLong(idColumn);
                int songIndex = cursor.getInt(indexColumn);

                if (removedSongIds.contains(songId) || i >= songs.size()) {
                    // Add a place holder song here, to be removed after by the caller
                    // This is done to maintain consistent queue and playing position
                    queue.add(new IndexedSong(Song.EMPTY_SONG, songIndex, IndexedSong.INVALID_INDEX));
                } else {
                    queue.add(new IndexedSong(songs.get(i), songIndex, IndexedSong.INVALID_INDEX));
                    i++;
                }
            } while (cursor.moveToNext());
        }

        return queue;
    }

    @NonNull
    private ArrayList<IndexedSong> getQueue(@NonNull final String tableName) {
        try (Cursor cursor = getReadableDatabase().query(tableName, new String[]{BaseColumns._ID, MusicPlaybackColumns.INDEX_IN_QUEUE},
                null, null, null, null, null)) {
            ArrayList<Long> songIds = StoreLoader.getIdsFromCursor(cursor, BaseColumns._ID);

            ArrayList<Long> removedSongIds = new ArrayList<>();
            Discography discography = Discography.getInstance();
            ArrayList<Song> songs = discography.getSongsFromIdsAndCleanupOrphans(songIds, removedSongIds::addAll);

            return getSongPosition(cursor, songs, removedSongIds);
        }
    }

    public interface MusicPlaybackColumns extends AudioColumns {
        String INDEX_IN_QUEUE = "index_in_queue";
    }
}

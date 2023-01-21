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
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.misc.queue.IndexedSong;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Andrew Neal, modified by Karim Abou Zeid
 *         <p/>
 *         This keeps track of the music playback and history state of the playback service
 */
public class MusicPlaybackQueueStore extends SQLiteOpenHelper {
    @Nullable
    private static MusicPlaybackQueueStore sInstance = null;
    public static final String DATABASE_NAME = "music_playback_state.db";
    public static final String PLAYING_QUEUE_TABLE_NAME = "playing_queue";
    public static final String ORIGINAL_PLAYING_QUEUE_TABLE_NAME = "original_playing_queue";
    private static final int VERSION = 6;

    /**
     * Constructor of <code>MusicPlaybackState</code>
     *
     * @param context The {@link Context} to use
     */
    public MusicPlaybackQueueStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        createTable(db, PLAYING_QUEUE_TABLE_NAME);
        createTable(db, ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
    }

    private void createTable(@NonNull final SQLiteDatabase db, final String tableName) {
        //noinspection StringBufferReplaceableByString
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(tableName);
        builder.append("(");

        builder.append(BaseColumns._ID);
        builder.append(" LONG NOT NULL,");

        builder.append(MusicPlaybackColumns.INDEX_IN_QUEUE);
        builder.append(" INT NOT NULL,");

        builder.append(AudioColumns.TITLE);
        builder.append(" STRING NOT NULL,");

        builder.append(AudioColumns.TRACK);
        builder.append(" INT NOT NULL,");

        builder.append(AudioColumns.YEAR);
        builder.append(" INT NOT NULL,");

        builder.append(AudioColumns.DURATION);
        builder.append(" LONG NOT NULL,");

        builder.append(AudioColumns.DATA);
        builder.append(" STRING NOT NULL,");

        builder.append(AudioColumns.DATE_ADDED);
        builder.append(" LONG NOT NULL,");

        builder.append(AudioColumns.DATE_MODIFIED);
        builder.append(" LONG NOT NULL,");

        builder.append(AudioColumns.ALBUM_ID);
        builder.append(" LONG NOT NULL,");

        builder.append(AudioColumns.ALBUM);
        builder.append(" STRING NOT NULL,");

        builder.append(AudioColumns.ARTIST_ID);
        builder.append(" LONG NOT NULL,");

        builder.append(AudioColumns.ARTIST);
        builder.append(" STRING NOT NULL);");

        db.execSQL(builder.toString());
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // not necessary yet
        db.execSQL("DROP TABLE IF EXISTS " + PLAYING_QUEUE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        // If we ever have downgrade, drop the table to be safe
        db.execSQL("DROP TABLE IF EXISTS " + PLAYING_QUEUE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ORIGINAL_PLAYING_QUEUE_TABLE_NAME);
        onCreate(db);
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
                    ContentValues values = new ContentValues(4);

                    values.put(BaseColumns._ID, song.id);
                    values.put(MusicPlaybackColumns.INDEX_IN_QUEUE, indexInQueue);
                    // TODO No need to save the song metadata here-under
                    //      Keep it for now for backward compat (in the case of rollback)
                    values.put(AudioColumns.TITLE, song.title);
                    values.put(AudioColumns.TRACK, song.trackNumber);
                    values.put(AudioColumns.YEAR, song.year);
                    values.put(AudioColumns.DURATION, song.duration);
                    values.put(AudioColumns.DATA, song.data);
                    values.put(AudioColumns.DATE_ADDED, song.dateAdded);
                    values.put(AudioColumns.DATE_MODIFIED, song.dateModified);
                    values.put(AudioColumns.ALBUM_ID, song.albumId);
                    values.put(AudioColumns.ALBUM, song.albumName);
                    values.put(AudioColumns.ARTIST_ID, song.artistId);
                    values.put(AudioColumns.ARTIST, MultiValuesTagUtil.merge(song.artistNames));
                    // Skip genre, discNumber, albumArtist fields since not supported in this DB.

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

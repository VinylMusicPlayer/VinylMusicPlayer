package com.poupa.vinylmusicplayer.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.loader.ReplayGainTagExtractor;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.DelayedTaskThread;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author SC (soncaokim)
 */

public class Discography extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "discography.db";
    private static final int VERSION = 1;

    @Nullable
    private static Discography sInstance = null;

    private HashMap<Long, Song> songsById = new HashMap<>();

    public Discography() {
        super(App.getInstance().getApplicationContext(), DATABASE_NAME, null, VERSION);

        fetchAllSongs();

        // House keeping - clean orphan songs
        DelayedTaskThread.getInstance().addTask(
                DelayedTaskThread.ONE_MINUTE,
                true,
                () -> cleanOrphanSongsImpl()
        );
    }

    @NonNull
    public synchronized String getStatsString() {
        return "Discography " + songsById.size() + " songs";
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SongColumns.NAME + " ("
                        + SongColumns.ID + " LONG NOT NULL, "
                        + SongColumns.ALBUM_ID + " LONG, "
                        + SongColumns.ALBUM_NAME +  " TEXT, "
                        + SongColumns.ARTIST_ID + " LONG, "
                        + SongColumns.ARTIST_NAME + " TEXT, "
                        + SongColumns.DATA_PATH + " TEXT, "
                        + SongColumns.DATE_ADDED + " LONG, "
                        + SongColumns.DATE_MODIFIED + " LONG, "
                        + SongColumns.REPLAYGAIN_ALBUM + " REAL, "
                        + SongColumns.REPLAYGAIN_TRACK + " REAL, "
                        + SongColumns.TRACK_DURATION + " LONG, "
                        + SongColumns.TRACK_NUMBER + " LONG, "
                        + SongColumns.TRACK_TITLE + " TEXT, "
                        + SongColumns.YEAR + " LONG"
                        + ");"
        );
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SongColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SongColumns.NAME);
        onCreate(db);
    }

    @NonNull
    public static synchronized Discography getInstance() {
        if (sInstance == null) {
            sInstance = new Discography();
        }
        return sInstance;
    }

    public void clear() {
        try (SQLiteDatabase database = getWritableDatabase()) {
            database.delete(SongColumns.NAME, null, null);
        }
        synchronized (this) {
            songsById.clear();
        }
    }

    @Nullable
    public synchronized Song getSong(long songId) {
        return songsById.get(songId);
    }

    public void addSong(@NonNull Song song) {
        DelayedTaskThread.getInstance().addTask(
                DelayedTaskThread.ONE_MILLIS,
                false,
                () -> addSongImpl(song)
        );
    }

    public synchronized void addSongImpl(@NonNull Song song) {
        // Race condition check: If the song has been added in between time --> skip
        if (songsById.containsKey(song.id)) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            removeSongById(database, song.id);

            extractTags(song);

            // add the entry
            final ContentValues values = new ContentValues();
            values.put(SongColumns.ID, song.id);
            values.put(SongColumns.ALBUM_ID, song.albumId);
            values.put(SongColumns.ALBUM_NAME, song.albumName);
            values.put(SongColumns.ARTIST_ID, song.artistId);
            values.put(SongColumns.ARTIST_NAME, song.artistName);
            values.put(SongColumns.DATA_PATH, song.data);
            values.put(SongColumns.DATE_ADDED, song.dateAdded);
            values.put(SongColumns.DATE_MODIFIED, song.dateModified);
            values.put(SongColumns.REPLAYGAIN_ALBUM, song.getReplayGainAlbum());
            values.put(SongColumns.REPLAYGAIN_TRACK, song.getReplayGainTrack());
            values.put(SongColumns.TRACK_DURATION, song.duration);
            values.put(SongColumns.TRACK_NUMBER, song.trackNumber);
            values.put(SongColumns.TRACK_TITLE, song.title);
            values.put(SongColumns.YEAR, song.year);

            database.insert(SongColumns.NAME, null, values);
            songsById.put(song.id, song);
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private void cleanOrphanSongsImpl() {
        final Context context = App.getInstance().getApplicationContext();

        HashSet<Long> allSongIds = new HashSet<>();
        try (final Cursor cursor = SongLoader.makeSongCursor(context, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    final long id = cursor.getInt(0);
                    allSongIds.add(id);
                } while (cursor.moveToNext());
            }
        }

        synchronized (this) {
            Set<Long> orphanSongsId = new HashSet<>(songsById.keySet()); // make a copy
            if (orphanSongsId.removeAll(allSongIds)) {
                removeSongsById(orphanSongsId);
            }
        }
    }

    private void extractTags(@NonNull Song song) {
        try {
            // Override with metadata extracted from the file ourselves
            AudioFile file = AudioFileIO.read(new File(song.data));
            Tag tags = file.getTagOrCreateAndSetDefault();

            song.albumName = tags.getFirst(FieldKey.ALBUM);
            song.artistName = tags.getFirst(FieldKey.ARTIST);
            song.title = tags.getFirst(FieldKey.TITLE);
            try {song.trackNumber = Integer.parseInt(tags.getFirst(FieldKey.TRACK));} catch (NumberFormatException ignored) {}
            try {song.year = Integer.parseInt(tags.getFirst(FieldKey.YEAR));} catch (NumberFormatException ignored) {}

            ReplayGainTagExtractor.setReplayGainValues(song);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeSongById(@NonNull final SQLiteDatabase database, final long songId) {
        synchronized (this) {
            songsById.remove(songId);
        }
        database.delete(
                SongColumns.NAME,
                SongColumns.ID + " = ?",
                new String[]{
                    String.valueOf(songId)
                });
    }

    private void removeSongsById(@NonNull final Set<Long> songsId) {
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            for (long id : songsId) {
                removeSongById(database, id);
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private void fetchAllSongs() {
        synchronized (this) {
            songsById.clear();
        }

        final SQLiteDatabase database = getReadableDatabase();

        try (final Cursor cursor = database.query(SongColumns.NAME,
                new String[]{
                        SongColumns.ID,
                        SongColumns.ALBUM_ID,
                        SongColumns.ALBUM_NAME,
                        SongColumns.ARTIST_ID,
                        SongColumns.ARTIST_NAME,
                        SongColumns.DATA_PATH,
                        SongColumns.DATE_ADDED,
                        SongColumns.DATE_MODIFIED,
                        SongColumns.REPLAYGAIN_ALBUM,
                        SongColumns.REPLAYGAIN_TRACK,
                        SongColumns.TRACK_DURATION,
                        SongColumns.TRACK_NUMBER,
                        SongColumns.TRACK_TITLE,
                        SongColumns.YEAR
                },
                null,
                null,
                null,
                null,
                null))
        {
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }

            do {
                int columnIndex = -1;
                final long id = cursor.getLong(++columnIndex);
                final long albumId = cursor.getLong(++columnIndex);
                final String albumName = cursor.getString(++columnIndex);
                final long artistId = cursor.getLong(++columnIndex);
                final String artistName = cursor.getString(++columnIndex);
                final String dataPath = cursor.getString(++columnIndex);
                final long dateAdded = cursor.getLong(++columnIndex);
                final long dateModified = cursor.getLong(++columnIndex);
                final float replaygainAlbum = cursor.getFloat(++columnIndex);
                final float replaygainTrack = cursor.getFloat(++columnIndex);
                final long trackDuration = cursor.getLong(++columnIndex);
                final int trackNumber = cursor.getInt(++columnIndex);
                final String trackTitle = cursor.getString(++columnIndex);
                final int year = cursor.getInt(++columnIndex);

                Song song = new Song(
                        id,
                        trackTitle,
                        trackNumber,
                        year,
                        trackDuration,
                        dataPath,
                        dateAdded,
                        dateModified,
                        albumId,
                        albumName,
                        artistId,
                        artistName);
                song.setReplayGainValues(replaygainTrack, replaygainAlbum);

                synchronized (this) {
                    songsById.put(id, song);
                }
            } while (cursor.moveToNext());
        }
    }

    private interface SongColumns {
        String NAME = "songs";

        String ID = "id";
        String ALBUM_ID = "album_id";
        String ALBUM_NAME = "album_name";
        String ARTIST_ID = "artist_id";
        String ARTIST_NAME = "artist_name";
        String DATA_PATH = "data_path";
        String DATE_ADDED = "date_added";
        String DATE_MODIFIED = "date_modified";
        String REPLAYGAIN_ALBUM = "replaygain_album";
        String REPLAYGAIN_TRACK = "replaygain_track";
        String TRACK_DURATION = "track_duration";
        String TRACK_TITLE = "track_title";
        String TRACK_NUMBER = "track_number";
        String YEAR = "year";
    }
}

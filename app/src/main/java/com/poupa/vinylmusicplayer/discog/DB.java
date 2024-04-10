package com.poupa.vinylmusicplayer.discog;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.OopsHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author SC (soncaokim)
 */

class DB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "discography.db";
    private static final int VERSION = 7;

    DB() {
        super(App.getInstance().getApplicationContext(), DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SongColumns.NAME + " ("
                        + SongColumns.ID + " LONG NOT NULL, "
                        + SongColumns.ALBUM_ID + " LONG, "
                        + SongColumns.ALBUM_ARTIST_NAME +  " TEXT, "
                        + SongColumns.ALBUM_NAME +  " TEXT, "
                        + SongColumns.ARTIST_ID + " LONG, "
                        + SongColumns.ARTIST_NAME + " TEXT, "
                        + SongColumns.DATA_PATH + " TEXT, "
                        + SongColumns.DATE_ADDED + " LONG, "
                        + SongColumns.DATE_MODIFIED + " LONG, "
                        + SongColumns.DISC_NUMBER + " LONG, "
                        + SongColumns.GENRE +  " TEXT, "
                        + SongColumns.REPLAYGAIN_ALBUM + " REAL, "
                        + SongColumns.REPLAYGAIN_TRACK + " REAL, "
                        + SongColumns.REPLAYGAINPEAK_ALBUM + " REAL, "
                        + SongColumns.REPLAYGAINPEAK_TRACK + " REAL, "
                        + SongColumns.TRACK_DURATION + " LONG, "
                        + SongColumns.TRACK_NUMBER + " LONG, "
                        + SongColumns.TRACK_TITLE + " TEXT, "
                        + SongColumns.YEAR + " LONG"
                        + ");"
        );
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        migrateDB(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        migrateDB(db, oldVersion, newVersion);
    }

    private void migrateDB(@NonNull SQLiteDatabase dbase, int oldVersion, int newVersion) {
        final Consumer<SQLiteDatabase> migrateResetAll = (db) -> {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", SongColumns.NAME));
            onCreate(db);
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
                case 6:
                case VERSION: // At target. This case is here for consistency check
                    migrateResetAll.accept(dbase);
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

    synchronized void addSong(@NonNull Song song) {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            final ContentValues values = new ContentValues();
            values.put(SongColumns.ID, song.id);
            values.put(SongColumns.ALBUM_ID, song.albumId);
            values.put(SongColumns.ALBUM_ARTIST_NAME, MultiValuesTagUtil.merge(song.albumArtistNames));
            values.put(SongColumns.ALBUM_NAME, song.albumName);
            values.put(SongColumns.ARTIST_NAME, MultiValuesTagUtil.merge(song.artistNames));
            values.put(SongColumns.DATA_PATH, song.data);
            values.put(SongColumns.DATE_ADDED, song.dateAdded);
            values.put(SongColumns.DATE_MODIFIED, song.dateModified);
            values.put(SongColumns.DISC_NUMBER, song.discNumber);
            values.put(SongColumns.GENRE, MultiValuesTagUtil.merge(song.genres));
            values.put(SongColumns.REPLAYGAIN_ALBUM, song.replayGainAlbum);
            values.put(SongColumns.REPLAYGAIN_TRACK, song.replayGainTrack);
            values.put(SongColumns.REPLAYGAINPEAK_ALBUM, song.replayGainPeakAlbum);
            values.put(SongColumns.REPLAYGAINPEAK_TRACK, song.replayGainPeakTrack);
            values.put(SongColumns.TRACK_DURATION, song.duration);
            values.put(SongColumns.TRACK_NUMBER, song.trackNumber);
            values.put(SongColumns.TRACK_TITLE, song.title);
            values.put(SongColumns.YEAR, song.year);

            db.insert(SongColumns.NAME, null, values);
        } catch (Exception e) {
            OopsHandler.collectStackTrace(e);
        }
    }

    synchronized void clear() {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            db.delete(SongColumns.NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized void removeSongById(long songId) {
        try (final SQLiteDatabase db = getWritableDatabase()) {
            db.delete(
                    SongColumns.NAME,
                    SongColumns.ID + " = " + songId,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    synchronized Collection<Song> fetchAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        final SQLiteDatabase database = getReadableDatabase();

        try (final Cursor cursor = database.query(SongColumns.NAME,
                new String[]{
                        SongColumns.ID,
                        SongColumns.ALBUM_ID,
                        SongColumns.ALBUM_ARTIST_NAME,
                        SongColumns.ALBUM_NAME,
                        SongColumns.ARTIST_NAME,
                        SongColumns.DATA_PATH,
                        SongColumns.DATE_ADDED,
                        SongColumns.DATE_MODIFIED,
                        SongColumns.DISC_NUMBER,
                        SongColumns.GENRE,
                        SongColumns.REPLAYGAIN_ALBUM,
                        SongColumns.REPLAYGAIN_TRACK,
                        SongColumns.REPLAYGAINPEAK_ALBUM,
                        SongColumns.REPLAYGAINPEAK_TRACK,
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
                return songs;
            }

            do {
                int columnIndex = -1;
                final long id = cursor.getLong(++columnIndex);
                final long albumId = cursor.getLong(++columnIndex);
                final String albumArtistNames = cursor.getString(++columnIndex);
                final String albumName = cursor.getString(++columnIndex);
                final String artistNames = cursor.getString(++columnIndex);
                final String dataPath = cursor.getString(++columnIndex);
                final long dateAdded = cursor.getLong(++columnIndex);
                final long dateModified = cursor.getLong(++columnIndex);
                final int discNumber = cursor.getInt(++columnIndex);
                final String genres = cursor.getString(++columnIndex);
                final float replayGainAlbum = cursor.getFloat(++columnIndex);
                final float replayGainTrack = cursor.getFloat(++columnIndex);
                final float replayGainPeakAlbum = cursor.getFloat(++columnIndex);
                final float replayGainPeakTrack = cursor.getFloat(++columnIndex);
                final long trackDuration = cursor.getLong(++columnIndex);
                final int trackNumber = cursor.getInt(++columnIndex);
                final String trackTitle = cursor.getString(++columnIndex);
                final int year = cursor.getInt(++columnIndex);

                final Song song = new Song(
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
                        MultiValuesTagUtil.split(artistNames));
                song.discNumber = discNumber;
                song.albumArtistNames = MultiValuesTagUtil.split(albumArtistNames);
                song.genres = MultiValuesTagUtil.split(genres);
                song.replayGainTrack = replayGainTrack;
                song.replayGainAlbum = replayGainAlbum;
                song.replayGainPeakTrack = replayGainPeakTrack;
                song.replayGainPeakAlbum = replayGainPeakAlbum;

                songs.add(song);
            } while (cursor.moveToNext());
            return songs;
        }
    }

    interface SongColumns {
        String NAME = "songs";

        String ID = "id";
        String ALBUM_ID = "album_id";
        String ALBUM_ARTIST_NAME = "album_artist_name";
        String ALBUM_NAME = "album_name";
        String ARTIST_ID = "artist_id"; // TODO Drop this column, unused
        String ARTIST_NAME = "artist_name";
        String DATA_PATH = "data_path";
        String DATE_ADDED = "date_added";
        String DATE_MODIFIED = "date_modified";
        String DISC_NUMBER = "disc_number";
        String GENRE = "genre";
        String REPLAYGAIN_ALBUM = "replaygain_album";
        String REPLAYGAIN_TRACK = "replaygain_track";
        String REPLAYGAINPEAK_ALBUM = "replaygainpeak_album";
        String REPLAYGAINPEAK_TRACK = "replaygainpeak_track";
        String TRACK_DURATION = "track_duration";
        String TRACK_TITLE = "track_title";
        String TRACK_NUMBER = "track_number";
        String YEAR = "year";
    }
}

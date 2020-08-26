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
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.DelayedTaskThread;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author SC (soncaokim)
 */

public class Discography {
    // TODO Genre editor.
    // For some select song (ex Evanescence / Fallen album), changing genre to 'Heavy Metal' always turn it into 137 (instead of the text inserted).
    // As if the change is redacted by JAudioTagger or the OS.

    // TODO The by-year ordering in the Songs tab is messed up since it relies on the year info provided by MediaStore (which is buggy)
    // TODO Extract album artist from ID3 tags and use that for album grouping/sorting
    // TODO Provide observer for add/remove/update entries in the in-memory cache
    // TODO Adopt play history database, in order to provide dynamic playlist
    // TODO Support multiple artists
    // TODO Refact the SortOrder to rely on enum/enum class, i.e. avoid doing string comparison
    // TODO Replace DelayedTaskThread by the standard AsyncTask (or any more modern alternative)

    @Nullable
    private static Discography sInstance = null;

    private DB database;

    private HashMap<Long, Song> songsById = new HashMap<>();

    HashMap<String, Artist> artistsByName = new HashMap<>();
    HashMap<Long, Artist> artistsById = new HashMap<>();

    HashMap<Long, Album> albumsById = new HashMap<>();

    HashMap<String, Genre> genresByName = new HashMap<>();
    HashMap<Long, ArrayList<Song>> songsByGenreId = new HashMap<>();

    public Discography() {
        database = new DB();
        fetchAllSongs();

        // TODO Instead of polling, register a MusicServiceEventListener/ContentObserver to be aware of MediaStore changes
        // Note that periodical polling is still necessary for deleted songs, and those changed while the app is not active
        DelayedTaskThread.getInstance().addTask(
                DelayedTaskThread.ONE_MINUTE,
                DelayedTaskThread.ONE_MINUTE,
                this::syncWithMediaStore
        );
    }

    @NonNull
    public static synchronized Discography getInstance() {
        if (sInstance == null) {
            sInstance = new Discography();
        }
        return sInstance;
    }

    @NonNull
    public synchronized Song getSong(int songId) {
        Song song = songsById.get(songId);
        return song == null ? Song.EMPTY_SONG : song;
    }

    @NonNull
    public synchronized Collection<Song> getAllSongs() {
        return songsById.values();
    }

    @Nullable
    public synchronized Artist getArtist(int artistId) {
        return artistsById.get(artistId);
    }

    @NonNull
    public synchronized Collection<Artist> getAllArtists() {
        return artistsById.values();
    }

    @Nullable
    public synchronized Album getAlbum(int albumId) {
        return albumsById.get(albumId);
    }

    @NonNull
    public synchronized Collection<Album> getAllAlbums() {
        return albumsById.values();
    }

    @NonNull
    public synchronized Collection<Genre> getAllGenres() {
        return genresByName.values();
    }

    @Nullable
    public synchronized Collection<Song> getSongsForGenre(long genreId) {
        return songsByGenreId.get(genreId);
    }

    public void addSong(@NonNull Song song) {
        DelayedTaskThread.getInstance().addTask(
                DelayedTaskThread.ONE_MILLIS,
                0,
                () -> addSongImpl(song, false)
        );
    }

    private void addSongImpl(@NonNull Song song, boolean cacheOnly) {
        if (!cacheOnly) {
            extractTags(song);
        }

        synchronized (this) {
            // Race condition check: If the song has been added in between time --> skip
            if (songsById.containsKey(song.id)) {
                return;
            }

            // Merge artist by name
            Artist artist = getOrCreateArtistByName(song);
            if (!artist.albums.isEmpty() && (artist.getId() != song.artistId)) {
                song.artistId = artist.getId();
            }

            // Merge album by name
            Album album = getOrCreateAlbumByName(song);
            if (!album.songs.isEmpty() && (album.getId() != song.albumId)) {
                song.albumId = album.getId();
            }
            album.songs.add(song);

            // Update genre cache
            // TODO Reduce songCount on old genre
            Genre genre = getOrCreateGenreByName(song);
            ArrayList<Song> songs = songsByGenreId.get(genre.id);
            if (songs != null) {
                songs.add(song);
                genre.songCount = songs.size();
            }

            // Only sort albums after the song has been added
            Collections.sort(artist.albums, (a1, a2) -> a1.getYear() - a2.getYear());
            Collections.sort(album.songs, (s1, s2) -> s1.trackNumber - s2.trackNumber);

            songsById.put(song.id, song);
        }

        if (!cacheOnly) {
            database.addSong(song);
        }
    }

    private void syncWithMediaStore() {
        final Context context = App.getInstance().getApplicationContext();

        // By querying via SongLoader, any newly added ones will be added to the cache
        ArrayList<Song> allSongs = SongLoader.getAllSongs(context);
        final HashSet<Long> allSongIds = new HashSet<>();
        for (Song song : allSongs) {
            allSongIds.add(song.id);
        }

        synchronized (this) {
            // Clean orphan songs (removed from MediaStore)
            Set<Long> cacheSongsId = new HashSet<>(songsById.keySet()); // make a copy
            if (cacheSongsId.removeAll(allSongIds)) {
                for (long songId : cacheSongsId) {
                    removeSongById(songId);
                }
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
            song.genre = tags.getFirst(FieldKey.GENRE);
            try {song.trackNumber = Integer.parseInt(tags.getFirst(FieldKey.TRACK));} catch (NumberFormatException ignored) {}
            try {song.year = Integer.parseInt(tags.getFirst(FieldKey.YEAR));} catch (NumberFormatException ignored) {}

            ReplayGainTagExtractor.setReplayGainValues(song);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeSongByPath(@NonNull final String path) {
        // TODO Avoid sequential search
        Song matchingSong = null;
        for (Song song : songsById.values()) {
            if (song.data.equals(path))
            {
                matchingSong = song;
                break;
            }
        }
        if (matchingSong != null) {
            removeSongById(matchingSong.id);
        }
    }

    public void removeSongById(long songId) {
        synchronized (this) {
            Song song = songsById.get(songId);
            if (song != null) {
                // Remove the song from linked Artist/Album cache
                Artist artist = artistsById.get(song.artistId);
                if (artist != null) {
                    for (Album album : artist.albums) {
                        if (album.getId() == song.albumId) {
                            album.songs.remove(song);
                            if (album.songs.isEmpty()) {
                                artist.albums.remove(album);
                                albumsById.remove(song.albumId);
                            }
                            break;
                        }
                    }
                    if (artist.albums.isEmpty()) {
                        artistsById.remove(song.artistId);
                        artistsByName.remove(song.artistName);
                    }
                }

                // Remove song from Genre cache
                Genre genre = genresByName.get(song.genre);
                if (genre != null) {
                    ArrayList<Song> songs = songsByGenreId.get(genre.id);
                    if (songs != null) {
                        songs.remove(song);
                        if (songs.isEmpty()) {
                            genresByName.remove(genre.name);
                            songsByGenreId.remove(genre.id);
                        } else {
                            genre.songCount = songs.size();
                        }
                    }
                }

                // Remove the song from the memory cache
                songsById.remove(songId);
            }
        }

        database.removeSongById(songId);
    }

    private void fetchAllSongs() {
        Collection<Song> songs = database.fetchAllSongs();
        for (Song song : songs) {
            addSongImpl(song, true);
        }
    }

    @NonNull
    private synchronized Artist getOrCreateArtistByName(@NonNull final Song song) {
        Artist artist = artistsByName.get(song.artistName);
        if (artist == null) {
            artist = new Artist();

            artistsByName.put(song.artistName, artist);
            artistsById.put(song.artistId, artist);
        }
        return artist;
    }

    @NonNull
    private synchronized Album getOrCreateAlbumByName(@NonNull final Song song) {
        Artist artist = getOrCreateArtistByName(song);
        for (Album album : artist.albums) {
            if (album.getTitle().equals(song.albumName)) {
                return album;
            }
        }
        Album album = new Album();
        artist.albums.add(album);
        albumsById.put(song.albumId, album);

        return album;
    }

    @NonNull
    private synchronized Genre getOrCreateGenreByName(@NonNull final Song song) {
        Genre genre = genresByName.get(song.genre);
        if (genre == null) {
            genre = new Genre(genresByName.size(), song.genre, 0);

            genresByName.put(song.genre, genre);
            songsByGenreId.put(genre.id, new ArrayList<>());
        }
        return genre;
    }

    private static class DB extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "discography.db";
        private static final int VERSION = 2;

        public DB() {
            super(App.getInstance().getApplicationContext(), DATABASE_NAME, null, VERSION);
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
                            + SongColumns.GENRE +  " TEXT, "
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

        public void addSong(@NonNull Song song) {
            final ContentValues values = new ContentValues();
            values.put(SongColumns.ID, song.id);
            values.put(SongColumns.ALBUM_ID, song.albumId);
            values.put(SongColumns.ALBUM_NAME, song.albumName);
            values.put(SongColumns.ARTIST_ID, song.artistId);
            values.put(SongColumns.ARTIST_NAME, song.artistName);
            values.put(SongColumns.DATA_PATH, song.data);
            values.put(SongColumns.DATE_ADDED, song.dateAdded);
            values.put(SongColumns.DATE_MODIFIED, song.dateModified);
            values.put(SongColumns.GENRE, song.genre);
            values.put(SongColumns.REPLAYGAIN_ALBUM, song.getReplayGainAlbum());
            values.put(SongColumns.REPLAYGAIN_TRACK, song.getReplayGainTrack());
            values.put(SongColumns.TRACK_DURATION, song.duration);
            values.put(SongColumns.TRACK_NUMBER, song.trackNumber);
            values.put(SongColumns.TRACK_TITLE, song.title);
            values.put(SongColumns.YEAR, song.year);

            final SQLiteDatabase db = getWritableDatabase();
            db.insert(SongColumns.NAME, null, values);
        }

        public void removeSongById(long songId) {
            final SQLiteDatabase db = getWritableDatabase();
            db.delete(
                    SongColumns.NAME,
                    SongColumns.ID + " = ?",
                    new String[]{
                            String.valueOf(songId)
                    });
        }

        @NonNull
        public Collection<Song> fetchAllSongs() {
            ArrayList<Song> songs = new ArrayList<>();
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
                            SongColumns.GENRE,
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
                    return songs;
                }

                do {
                    int columnIndex = -1;
                    final int id = cursor.getInt(++columnIndex);
                    final int albumId = cursor.getInt(++columnIndex);
                    final String albumName = cursor.getString(++columnIndex);
                    final int artistId = cursor.getInt(++columnIndex);
                    final String artistName = cursor.getString(++columnIndex);
                    final String dataPath = cursor.getString(++columnIndex);
                    final long dateAdded = cursor.getLong(++columnIndex);
                    final long dateModified = cursor.getLong(++columnIndex);
                    final String genre = cursor.getString(++columnIndex);
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
                    song.genre = genre;

                    songs.add(song);
                } while (cursor.moveToNext());
                return songs;
            }
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
        String GENRE = "genre";
        String REPLAYGAIN_ALBUM = "replaygain_album";
        String REPLAYGAIN_TRACK = "replaygain_track";
        String TRACK_DURATION = "track_duration";
        String TRACK_TITLE = "track_title";
        String TRACK_NUMBER = "track_number";
        String YEAR = "year";
    }
}

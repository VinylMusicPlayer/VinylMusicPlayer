package com.poupa.vinylmusicplayer.discog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.loader.ReplayGainTagExtractor;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.reference.GenreTypes;

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
    @Nullable
    private static Discography sInstance = null;

    private DB database;
    private MemCache cache;

    public Discography() {
        database = new DB();
        cache = new MemCache();

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

    public void startService() {
        DelayedTaskThread.getInstance().start();
    }

    public void stopService() {
        DelayedTaskThread.getInstance().stop();
    }

    @NonNull
    public Song getSong(int songId) {
        synchronized (cache) {
            Song song = cache.songsById.get(songId);
            return song == null ? Song.EMPTY_SONG : song;
        }
    }

    @NonNull
    public Collection<Song> getAllSongs() {
        synchronized (cache) {
            return cache.songsById.values();
        }
    }

    @Nullable
    public Artist getArtist(int artistId) {
        synchronized (cache) {
            return cache.artistsById.get(artistId);
        }
    }

    @NonNull
    public Collection<Artist> getAllArtists() {
        synchronized (cache) {
            return cache.artistsById.values();
        }
    }

    @Nullable
    public Album getAlbum(int albumId) {
        synchronized (cache) {
            return cache.albumsById.get(albumId);
        }
    }

    @NonNull
    public Collection<Album> getAllAlbums() {
        synchronized (cache) {
            return cache.albumsById.values();
        }
    }

    @NonNull
    public Collection<Genre> getAllGenres() {
        synchronized (cache) {
            return cache.genresByName.values();
        }
    }

    @Nullable
    public Collection<Song> getSongsForGenre(long genreId) {
        synchronized (cache) {
            return cache.songsByGenreId.get(genreId);
        }
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

        // Unicode normalization
        song.artistName = StringUtil.unicodeNormalize(song.artistName);
        song.albumName = StringUtil.unicodeNormalize(song.albumName);
        song.title = StringUtil.unicodeNormalize(song.title);
        song.genre = StringUtil.unicodeNormalize(song.genre);

        // ID3 genre mapping, replacing numerical ID3v1 values by textual ones
        try {
            Integer genreId = Integer.parseInt(song.genre);
            if (genreId != null) {
                String genre = GenreTypes.getInstanceOf().getValueForId(genreId);
                if (genre != null) {
                    song.genre = genre;
                }
            }
        } catch (NumberFormatException ignored) {}

        synchronized (cache) {
            // Race condition check: If the song has been added -> skip
            if (cache.songsById.containsKey(song.id)) {
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
            Genre genre = getOrCreateGenreByName(song);
            ArrayList<Song> songs = cache.songsByGenreId.get(genre.id);
            if (songs != null) {
                songs.add(song);
                genre.songCount = songs.size();
            }

            // Only sort albums after the song has been added
            Collections.sort(artist.albums, (a1, a2) -> a1.getYear() - a2.getYear());
            Collections.sort(album.songs, (s1, s2) -> s1.trackNumber - s2.trackNumber);

            cache.songsById.put(song.id, song);
        }

        if (!cacheOnly) {
            database.addSong(song);
        }
    }

    private void syncWithMediaStore() {
        final Context context = App.getInstance().getApplicationContext();

        // By querying via SongLoader, any newly added ones will be added to the cache
        ArrayList<Song> allSongs = SongLoader.getAllSongs(context);
        final HashSet<Integer> allSongIds = new HashSet<>();
        for (Song song : allSongs) {
            allSongIds.add(song.id);
        }

        synchronized (cache) {
            // Clean orphan songs (removed from MediaStore)
            Set<Integer> cacheSongsId = new HashSet<>(cache.songsById.keySet()); // make a copy
            if (cacheSongsId.removeAll(allSongIds)) {
                for (int songId : cacheSongsId) {
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

    public void removeSongByPath(@NonNull final String path) {
        synchronized (cache) {
            Song matchingSong = null;

            // TODO Avoid sequential search
            for (Song song : cache.songsById.values()) {
                if (song.data.equals(path)) {
                    matchingSong = song;
                    break;
                }
            }
            if (matchingSong != null) {
                removeSongById(matchingSong.id);
            }
        }
    }

    public void removeSongById(int songId) {
        synchronized (cache) {
            Song song = cache.songsById.get(songId);
            if (song != null) {
                // Remove the song from linked Artist/Album cache
                Artist artist = cache.artistsById.get(song.artistId);
                if (artist != null) {
                    for (Album album : artist.albums) {
                        if (album.getId() == song.albumId) {
                            album.songs.remove(song);
                            if (album.songs.isEmpty()) {
                                artist.albums.remove(album);
                                cache.albumsById.remove(song.albumId);
                            }
                            break;
                        }
                    }
                    if (artist.albums.isEmpty()) {
                        cache.artistsById.remove(song.artistId);
                        cache.artistsByName.remove(song.artistName);
                    }
                }

                // Remove song from Genre cache
                Genre genre = cache.genresByName.get(song.genre);
                if (genre != null) {
                    ArrayList<Song> songs = cache.songsByGenreId.get(genre.id);
                    if (songs != null) {
                        songs.remove(song);
                        if (songs.isEmpty()) {
                            cache.genresByName.remove(genre.name);
                            cache.songsByGenreId.remove(genre.id);
                        } else {
                            genre.songCount = songs.size();
                        }
                    }
                }

                // Remove the song from the memory cache
                cache.songsById.remove(songId);
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
    private Artist getOrCreateArtistByName(@NonNull final Song song) {
        synchronized (cache) {
            Artist artist = cache.artistsByName.get(song.artistName);
            if (artist == null) {
                artist = new Artist();

                cache.artistsByName.put(song.artistName, artist);
                cache.artistsById.put(song.artistId, artist);
            }
            return artist;
        }
    }

    @NonNull
    private Album getOrCreateAlbumByName(@NonNull final Song song) {
        synchronized (cache) {
            Artist artist = getOrCreateArtistByName(song);
            for (Album album : artist.albums) {
                if (album.getTitle().equals(song.albumName)) {
                    return album;
                }
            }
            Album album = new Album();
            artist.albums.add(album);
            cache.albumsById.put(song.albumId, album);

            return album;
        }
    }

    @NonNull
    private Genre getOrCreateGenreByName(@NonNull final Song song) {
        synchronized (cache) {
            Genre genre = cache.genresByName.get(song.genre);
            if (genre == null) {
                genre = new Genre(cache.genresByName.size(), song.genre, 0);

                cache.genresByName.put(song.genre, genre);
                cache.songsByGenreId.put(genre.id, new ArrayList<>());
            }
            return genre;
        }
    }

    private static class MemCache {
        public HashMap<Integer, Song> songsById = new HashMap<>();

        public HashMap<String, Artist> artistsByName = new HashMap<>();
        public HashMap<Integer, Artist> artistsById = new HashMap<>();

        public HashMap<Integer, Album> albumsById = new HashMap<>();

        public HashMap<String, Genre> genresByName = new HashMap<>();
        public HashMap<Long, ArrayList<Song>> songsByGenreId = new HashMap<>();
    }
}

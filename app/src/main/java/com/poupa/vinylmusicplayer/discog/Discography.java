package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.loader.ReplayGainTagExtractor;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;

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
    private final MemCache cache;

    private int addSongQueueSize;
    private Snackbar addSongProgressBar;
    private View addSongProgressBarView;

    public Discography() {
        database = new DB();
        cache = new MemCache();

        fetchAllSongs();
    }

    @NonNull
    public static synchronized Discography getInstance() {
        if (sInstance == null) {
            sInstance = new Discography();
        }
        return sInstance;
    }

    public void startService(@NonNull final View progressBarView) {
        addSongProgressBarView = progressBarView;

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Discography.this.syncWithMediaStore();
                return true;
            }
        }.execute();
    }

    @NonNull
    public Song getSong(long songId) {
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
    public Artist getArtist(long artistId) {
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
    public Album getAlbum(long albumId) {
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
        new AsyncTask<Song, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                ++addSongQueueSize;
            }

            @Override
            protected Boolean doInBackground(Song... songs) {
                for (Song song : songs) {
                    Discography.this.addSongImpl(song, false);
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                --addSongQueueSize;
                try {
                    if (addSongQueueSize > 0) {
                        final int discogSize = Discography.this.cache.songsById.size();
                        final String message = String.format(
                                App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_so_far),
                                discogSize);
                        if (addSongProgressBar == null) {
                            addSongProgressBar = Snackbar.make(
                                    addSongProgressBarView,
                                    message,
                                    Snackbar.LENGTH_INDEFINITE);
                            addSongProgressBar.show();
                        } else {
                            addSongProgressBar.setText(message);
                            if (!addSongProgressBar.isShownOrQueued()) {
                                addSongProgressBar.show();
                            }
                        }
                    } else {
                        if (addSongProgressBar.isShownOrQueued()) {
                            addSongProgressBar.dismiss();
                        }

                        // Force reload the UI
                        addSongProgressBarView.getRootView().invalidate();
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }.execute(song);
    }

    private void addSongImpl(@NonNull Song song, boolean cacheOnly) {
        synchronized (cache) {
            // Race condition check: If the song has been added -> skip
            if (cache.songsById.containsKey(song.id)) {
                return;
            }

            if (!cacheOnly) {
                extractTags(song);
            }

            // Unicode normalization
            song.artistName = StringUtil.unicodeNormalize(song.artistName);
            song.albumName = StringUtil.unicodeNormalize(song.albumName);
            song.title = StringUtil.unicodeNormalize(song.title);
            song.genre = StringUtil.unicodeNormalize(song.genre);

            // Replace genre numerical ID3v1 values by textual ones
            try {
                int genreId = Integer.parseInt(song.genre);
                String genre = GenreTypes.getInstanceOf().getValueForId(genreId);
                if (genre != null) {
                    song.genre = genre;
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            cache.addSong(song);

            if (!cacheOnly) {
                database.addSong(song);
            }
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

        synchronized (cache) {
            // Clean orphan songs (removed from MediaStore)
            Set<Long> cacheSongsId = new HashSet<>(cache.songsById.keySet()); // make a copy
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
            if (song.title.trim().isEmpty()) {
                // fallback to use the file name
                song.title = file.getFile().getName();
            }

            song.genre = tags.getFirst(FieldKey.GENRE);
            try {song.trackNumber = Integer.parseInt(tags.getFirst(FieldKey.TRACK));} catch (NumberFormatException ignored) {}
            try {song.year = Integer.parseInt(tags.getFirst(FieldKey.YEAR));} catch (NumberFormatException ignored) {}

            ReplayGainTagExtractor.setReplayGainValues(song);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void removeSongByPath(@NonNull final String path) {
        synchronized (cache) {
            Song matchingSong = null;

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

    public void removeSongById(long songId) {
        cache.removeSongById(songId);
        database.removeSongById(songId);
    }

    private void fetchAllSongs() {
        Collection<Song> songs = database.fetchAllSongs();
        for (Song song : songs) {
            addSongImpl(song, true);
        }
    }

    private static class MemCache {
        public HashMap<Long, Song> songsById = new HashMap<>();

        public HashMap<String, Artist> artistsByName = new HashMap<>();
        public HashMap<Long, Artist> artistsById = new HashMap<>();

        public HashMap<Long, Album> albumsById = new HashMap<>();

        public HashMap<String, Genre> genresByName = new HashMap<>();
        public HashMap<Long, ArrayList<Song>> songsByGenreId = new HashMap<>();

        public synchronized void addSong(@NonNull final Song song) {
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

        public synchronized void removeSongById(long songId) {
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
    }
}

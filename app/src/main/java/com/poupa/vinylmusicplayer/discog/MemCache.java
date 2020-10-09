package com.poupa.vinylmusicplayer.discog;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author SC (soncaokim)
 */

class MemCache {
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
                assert(artist.albums != null);
                for (Album album : artist.albums) {
                    if (album.getId() == song.albumId) {
                        assert(album.songs != null);
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

    public synchronized void clear() {
        songsById.clear();

        artistsByName.clear();
        artistsById.clear();

        albumsById.clear();

        genresByName.clear();
        songsByGenreId.clear();
    }

    @NonNull
    private synchronized Artist getOrCreateArtistByName(@NonNull final Song song) {
        Artist artist = artistsByName.get(song.artistName);
        if (artist == null) {
            artist = new Artist(song.artistId, song.artistName);

            artistsByName.put(song.artistName, artist);
            artistsById.put(song.artistId, artist);
        }
        return artist;
    }

    @NonNull
    private synchronized Album getOrCreateAlbumByName(@NonNull final Song song) {
        Artist artist = getOrCreateArtistByName(song);
        for (Album album : artist.albums) {
            // dont rely on the Album.getTitle since it goes through the 'unknown album' filter
            final String albumTitle = album.safeGetFirstSong().albumName;

            if (albumTitle.equals(song.albumName)) {
                return album;
            }
        }

        // For multi-artist album, there might be already an album created
        // Reuse the album if it has the same albumArtist
        Album album = albumsById.get(song.albumId);
        if (album != null) {
            final String albumArtist = album.safeGetFirstSong().albumArtistName;
            if (TextUtils.equals(albumArtist, song.albumArtistName)) {
                artist.albums.add(album);
                return album;
            }
        }

        //long albumId = song.albumId;
        //while (albumsById.containsKey(albumId)) {albumId += 10000;}
        //song.albumId = albumId;

        album = new Album();
        albumsById.put(song.albumId, album);
        artist.albums.add(album);

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

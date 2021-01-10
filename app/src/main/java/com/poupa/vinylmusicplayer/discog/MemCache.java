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
import java.util.List;
import java.util.function.Function;

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
        List<Artist> artists = getOrCreateArtistByName(song);
        for (Artist artist : artists) {
            Collections.sort(artist.albums, (a1, a2) -> a1.getYear() - a2.getYear());
        }

        Collections.sort(album.songs,
                (s1, s2) -> (s1.discNumber != s2.discNumber)
                        ? (s1.discNumber - s2.discNumber)
                        : (s1.trackNumber - s2.trackNumber)
        );

        songsById.put(song.id, song);
    }

    public synchronized void removeSongById(long songId) {
        Song song = songsById.get(songId);
        if (song != null) {
            // ---- Remove the song from linked Artist/Album cache
            final List<String> artistNames = song.artistNames;
            Album impactedAlbum = null;
            for (final String artistName : artistNames) {
                Artist artist = artistsByName.get(artistName);
                for (Album album : artist.albums) {
                    if (album.getId() == song.albumId) {
                        impactedAlbum = album;
                        break;
                    }
                }
                if (impactedAlbum != null) {
                    // there is only one impacted album
                    break;
                }
            }
            impactedAlbum.songs.remove(song);
            if (impactedAlbum.songs.isEmpty()) {
                albumsById.remove(song.albumId);
            }

            // Due to the approach to handle multi-artist per song
            // different artists can be linked to the same album.
            // As soon as a multi-artist song is removed from an album,
            // the artist-album link may become obsolete
            for (final String artistName : artistNames) {
                boolean isArtistAlbumLinkNeeded = false;
                for (Song albumSong : impactedAlbum.songs) {
                    if (albumSong.artistNames.contains(artistName)) {
                        isArtistAlbumLinkNeeded = true;
                        break;
                    }
                }
                if (!isArtistAlbumLinkNeeded) {
                    Artist artist = artistsByName.get(artistName);
                    artist.albums.remove(impactedAlbum);
                    if (artist.albums.isEmpty()) {
                        artistsById.remove(artist.id);
                        artistsByName.remove(artistName);
                    }
                }
            }

            // ---- Remove song from Genre cache
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

            // ---- Remove the song from the memory cache
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
    private synchronized List<Artist> getOrCreateArtistByName(@NonNull final Song song) {
        Function<String, Artist> getOrCreateArtist = (@NonNull final String artistName) -> {
            Artist artist = artistsByName.get(artistName);
            if (artist == null) {
                long artistId = artistName.hashCode(); // TODO: It is not safe to consider this as an unique ID
                artist = new Artist(artistId, artistName);

                artistsByName.put(artistName, artist);
                artistsById.put(artistId, artist);
            }
            return artist;
        };

        ArrayList<Artist> artists = new ArrayList<>();
        for (final String artistName : song.artistNames) {
            artists.add(getOrCreateArtist.apply(artistName));
        }

        // Since the MediaStore artistId is disregarded, correct the link on the Song object
        Artist mainArtist = artists.get(Song.TRACK_ARTIST_MAIN);
        if (!mainArtist.albums.isEmpty() && (mainArtist.getId() != song.artistId)) {
            song.artistId = mainArtist.getId();
        }
        return artists;
    }

    @NonNull
    private synchronized Album getOrCreateAlbumByName(@NonNull final Song song) {
        List<Artist> artists = getOrCreateArtistByName(song);

        // ---- For multi-artist album (i.e compilation ones), there might be already an album created
        Album albumById = albumsById.get(song.albumId);
        if (albumById != null) {
            final String albumArtist = albumById.safeGetFirstSong().albumArtistName;
            final String albumName = albumById.safeGetFirstSong().albumName;
            if (TextUtils.equals(albumArtist, song.albumArtistName) && TextUtils.equals(albumName, song.albumName)) {
                for (Artist artist : artists) {
                    if (artist.albums.contains(albumById)) continue;

                    artist.albums.add(albumById);
                }
                return albumById;
            }
        }

        // ---- Find by album name
        for (Artist artist : artists) {
            for (Album artistAlbum : artist.albums) {
                final String albumTitle = artistAlbum.safeGetFirstSong().albumName;
                if (albumTitle.equals(song.albumName)) {
                    // attach to the other artists
                    for (Artist otherArtist : artists) {
                        if (otherArtist == artist) continue;
                        if (otherArtist.albums.contains(artistAlbum)) continue;

                        otherArtist.albums.add(artistAlbum);
                    }
                    return artistAlbum;
                }
            }
        }

        // ---- None found
        Album album = new Album();

        albumsById.put(song.albumId, album);
        for (Artist artist : artists) {artist.albums.add(album);}

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

package com.poupa.vinylmusicplayer.discog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.sort.AlbumSortOrder;
import com.poupa.vinylmusicplayer.sort.SongSortOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author SC (soncaokim)
 */

class MemCache {
    enum ConsistencyState {
        UNINITIALIZED,
        RESETTING,
        REFRESHING,
        OK
    };
    ConsistencyState consistencyState = ConsistencyState.UNINITIALIZED;

    final Map<Long, Song> songsById = new HashMap<>();

    final Map<String, Artist> artistsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    final Map<Long, Artist> artistsById = new HashMap<>();

    // internal implementation class, to make explicit that we are dealing with slices of album, not the full one
    static class AlbumSlice extends Album {
    }

    final Map<Long, Map<Long, AlbumSlice>> albumsByAlbumIdAndArtistId = new HashMap<>();
    final Map<String, Set<Long>> albumsByName = new HashMap<>();

    final Map<String, Genre> genresByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    final Map<Long, ArrayList<Song>> songsByGenreId = new HashMap<>();
    private float maxReplayGain = Float.NaN; // Computed lazily when it's needed, since it's used only on some android versions

    synchronized void addSong(@NonNull final Song song) {
        Map<Long, AlbumSlice> albums = getOrCreateAlbum(song);
        for (Album album : albums.values()) {
            album.songs.add(song);
        }

        // Update genre cache
        addSongToGenres(song);

        // Update the overall max replay gain value, if it's been computed already
        if (!Float.isNaN(maxReplayGain)) {
            float songMaxReplayGain = computeSongMaxReplayGain(song);
            if (maxReplayGain < songMaxReplayGain){
                maxReplayGain = songMaxReplayGain;
            }
        }

        songsById.put(song.id, song);

        // Only sort albums after the song has been added
        for (Long artistId : albums.keySet()) {
            Artist artist = artistsById.get(artistId);
            if (artist == null) continue;
            Collections.sort(artist.albums, AlbumSortOrder.BY_YEAR_DESC);
        }
        for (Album album : albums.values()) {
            Collections.sort(album.songs, SongSortOrder.BY_DISC_TRACK);
        }
    }

    synchronized void removeSongById(long songId) {
        final Song song = songsById.get(songId);
        if (song != null) {
            // ---- Remove the song from linked Album cache
            final Collection<Long> orphanArtists = new HashSet<>();
            final Map<Long, AlbumSlice> impactedAlbumsByArtist = albumsByAlbumIdAndArtistId.get(song.albumId);
            if (impactedAlbumsByArtist != null) {
                for (final Map.Entry<Long, AlbumSlice> pair : impactedAlbumsByArtist.entrySet()) {
                    final Album album = pair.getValue();
                    if (album.songs.remove(song)) {
                        if (album.songs.isEmpty()) {
                            orphanArtists.add(pair.getKey());
                        }
                    }
                }
            }

            // ---- Check the Artist/Album link
            for (final Long artistId : orphanArtists) {
                final Artist artist = artistsById.get(artistId);

                final Album album = impactedAlbumsByArtist.get(artistId);
                impactedAlbumsByArtist.remove(artistId);

                if (artist == null) {continue;}

                artist.albums.remove(album);
                if (artist.albums.isEmpty()) {
                    artistsById.remove(artist.id);
                    artistsByName.remove(artist.name);
                }
            }
            if (impactedAlbumsByArtist != null && impactedAlbumsByArtist.isEmpty()) {
                albumsByAlbumIdAndArtistId.remove(song.albumId);

                @Nullable final Set<Long> albumsId = albumsByName.get(song.albumName);
                if (albumsId != null) {
                    albumsId.remove(song.albumId);
                    if (albumsId.isEmpty()) {
                        albumsByName.remove(song.albumName);
                    }
                }
            }

            // ---- Remove song from Genre cache
            removeSongFromGenreCache(song);

            // Update the overall max replay gain value, if it's been computed already
            if (!Float.isNaN(maxReplayGain)) {
                float songMaxReplayGain = computeSongMaxReplayGain(song);
                if (maxReplayGain == songMaxReplayGain){
                    maxReplayGain = Float.NaN; // Will be recomputed next time it's needed
                }
            }

            // ---- Remove the song from the memory cache
            songsById.remove(songId);
        }
    }

    synchronized void clear() {
        songsById.clear();

        artistsByName.clear();
        artistsById.clear();

        albumsByAlbumIdAndArtistId.clear();
        albumsByName.clear();

        genresByName.clear();
        songsByGenreId.clear();
    }

    private float computeSongMaxReplayGain(Song song) {
        if (Float.isNaN(song.replayGainAlbum) && Float.isNaN(song.replayGainTrack)) {
            return 0.0f;
        }
        if (Float.isNaN(song.replayGainAlbum)) {
            return song.replayGainTrack;
        }
        if (Float.isNaN(song.replayGainTrack)) {
            return song.replayGainAlbum;
        }
        return Math.max(song.replayGainAlbum, song.replayGainTrack);
    }

    synchronized float getMaxReplayGain() {
        if (Float.isNaN(maxReplayGain)) {
            maxReplayGain = songsById.values().stream()
                    .map(this::computeSongMaxReplayGain)
                    .max(Float::compareTo)
                    .orElse(0.0f);
        }

        return maxReplayGain;
    }

    @NonNull
    private synchronized List<Artist> getOrCreateArtistByName(@NonNull final Song song) {
        Function<String, Artist> getOrCreateArtist = (@NonNull final String artistName) -> {
            Artist artist = artistsByName.get(artistName);
            if (artist == null) {
                long artistId = artistsByName.size();
                artist = new Artist(artistId, artistName);

                artistsByName.put(artistName, artist);
                artistsById.put(artistId, artist);
            }
            return artist;
        };

        final LinkedHashSet<String> names = new LinkedHashSet<>(); // ordered and unique list of names
        names.addAll(song.artistNames);
        names.addAll(song.albumArtistNames);
        final List<Artist> artists = new ArrayList<>(names.size());
        if(names.isEmpty()){
            // Albums must have an artist to be inserted into albumsByAlbumIdAndArtistId.
            artists.add(Artist.EMPTY);
        } else {
            for (final String name : names) {
                final Artist artist = getOrCreateArtist.apply(name);
                artists.add(artist);
            }
        }

        return artists;
    }

    @NonNull
    private synchronized Map<Long, AlbumSlice> getOrCreateAlbum(@NonNull final Song song) {
        final List<Artist> artists = getOrCreateArtistByName(song);

        // Try reusing an existing album with same name
        final Set<Long> albumIdsSameName = albumsByName.get(song.albumName);
        if ((albumIdsSameName != null) && !artists.isEmpty()) {
            final Artist mainArtist = artists.get(0);

            for (final long id : albumIdsSameName) {
                final AlbumSlice byMainArtist = albumsByAlbumIdAndArtistId.get(id).get(mainArtist.id);
                if (byMainArtist != null) {
                    song.albumId = byMainArtist.getId();
                    break;
                }
            }
        }

        // Now search by ID
        Map<Long, AlbumSlice> albumsByArtist = albumsByAlbumIdAndArtistId.get(song.albumId);
        if (albumsByArtist == null) {
            albumsByArtist = new HashMap<>();
            albumsByAlbumIdAndArtistId.put(song.albumId, albumsByArtist);
        }

        final Map<Long, AlbumSlice> result = new HashMap<>();
        for (final Artist artist : artists) {
            // Attach to the artists if needed
            if (!albumsByArtist.containsKey(artist.id)) {
                final AlbumSlice album = new AlbumSlice();
                albumsByArtist.put(artist.id, album);

                Set<Long> albumsId = albumsByName.get(song.albumName);
                if (albumsId == null) {
                    albumsByName.put(song.albumName, new HashSet<>());
                    albumsId = albumsByName.get(song.albumName);
                }
                Objects.requireNonNull(albumsId);
                albumsId.add(song.albumId);

                artist.albums.add(album);
            }

            // Filter by concerned artists
            result.put(artist.id, albumsByArtist.get(artist.id));
        }

        return result;
    }

    private synchronized void removeSongFromGenreCache(@NonNull final Song song) {
        song.genres.stream().forEach(genreName -> {
            final Genre genre = genresByName.get(genreName);
            if (genre != null) {
                final ArrayList<Song> songs = songsByGenreId.get(genre.id);
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
        });
    }

    private synchronized void addSongToGenres(@NonNull final Song song) {
        List<Genre> genres = getOrCreateGenresBySong(song);
        genres.stream().forEach(genre->{
            this.addSongToGenreAndUpdateCount(song, genre);
        });
    }

    private synchronized void addSongToGenreAndUpdateCount(@NonNull final Song song, @NonNull final Genre genre) {
        ArrayList<Song> songs = songsByGenreId.get(genre.id);
        if (songs != null) {
            songs.add(song);
            genre.songCount = songs.size();
        }
    }

    @NonNull
    private synchronized List<Genre> getOrCreateGenresBySong(@NonNull final Song song) {
        // If a song has no genres, empty string is the "unknown" genre
        final List<String> genres = song.genres.isEmpty() ? List.of("") : song.genres;

        return genres.stream().map(this::getOrCreateGenreByName).collect(Collectors.toList());
    }

    @NonNull
    private synchronized Genre getOrCreateGenreByName(@NonNull final String genreName) {
        Genre genre = genresByName.get(genreName);
        if (genre == null) {
            genre = new Genre(genresByName.size(), genreName, 0);

            genresByName.put(genreName, genre);
            songsByGenreId.put(genre.id, new ArrayList<>());
        }
        return genre;
    }
}

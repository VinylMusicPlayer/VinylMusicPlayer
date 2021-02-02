package com.poupa.vinylmusicplayer.discog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.TagExtractor;
import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.util.StringUtil;

import org.jaudiotagger.tag.reference.GenreTypes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author SC (soncaokim)
 */

public class Discography implements MusicServiceEventListener {
    public static int ICON = R.drawable.ic_bookmark_music_white_24dp;

    // TODO wrap this inside the MemCache class
    private final DB database;
    private final MemCache cache;

    public SnackbarUtil snackbar = null;
    private Handler mainActivityTaskQueue = null;
    private final Collection<Runnable> changedListeners = new LinkedList<>();

    public Discography() {
        database = new DB();
        cache = new MemCache();

        fetchAllSongs();
    }

    // TODO This is not a singleton and should not be declared as such
    @NonNull
    public static Discography getInstance() {
        return App.getDiscography();
    }

    public void startService(@NonNull final MainActivity mainActivity) {
        mainActivityTaskQueue = new Handler(mainActivity.getMainLooper());
        snackbar = new SnackbarUtil(mainActivity.getSnackBarContainer());

        triggerSyncWithMediaStore(false);
    }

    public void stopService() {
        snackbar = null;
        mainActivityTaskQueue = null;
    }

    @NonNull
    public Song getOrAddSong(@NonNull final Song song) {
        // TODO synchronized(cache) to avoid race condition along get/remove/add
        Song discogSong = getSong(song.id);
        if (discogSong != Song.EMPTY_SONG) {
            BiPredicate<Song, Song> isMetadataObsolete = (final @NonNull Song incomingSong, final @NonNull Song cachedSong) -> {
                if (incomingSong.dateAdded != cachedSong.dateAdded) return true;
                if (incomingSong.dateModified != cachedSong.dateModified) return true;
                return (!incomingSong.data.equals(cachedSong.data));
            };

            if (!isMetadataObsolete.test(song, discogSong)) {
                return discogSong;
            } else {
                removeSongById(song.id);
            }
        }

        addSong(song);

        return song;
    }

    @NonNull
    public Song getSong(long songId) {
        synchronized (cache) {
            Song song = cache.songsById.get(songId);
            return song == null ? Song.EMPTY_SONG : song;
        }
    }

    @NonNull
    public Song getSongByPath(@NonNull final String path) {
        synchronized (cache) {
            Song matchingSong = Song.EMPTY_SONG;

            for (Song song : cache.songsById.values()) {
                if (song.data.equals(path)) {
                    matchingSong = song;
                    break;
                }
            }
            return matchingSong;
        }
    }

    @NonNull
    public ArrayList<Song> getAllSongs() {
        synchronized (cache) {
            // Make a copy here, to avoid error while the caller is iterating on the result
            return new ArrayList<>(cache.songsById.values());
        }
    }

    @Nullable
    public Artist getArtist(long artistId) {
        synchronized (cache) {
            return cache.artistsById.get(artistId);
        }
    }

    @Nullable
    public Artist getArtistByName(String artistName) {
        synchronized (cache) {
            return cache.artistsByName.get(artistName);
        }
    }

    @NonNull
    public ArrayList<Artist> getAllArtists() {
        synchronized (cache) {
            // Make a copy here, to avoid error while the caller is iterating on the result
            return new ArrayList<>(cache.artistsById.values());
        }
    }

    @Nullable
    public Album getAlbum(long albumId) {
        synchronized (cache) {
            Map<Long, MemCache.AlbumSlice> albumsByArtist = cache.albumsByAlbumIdAndArtistId.get(albumId);
            if (albumsByArtist == null) return null;
            return mergeFullAlbum(albumsByArtist.values());
        }
    }

    @NonNull
    public ArrayList<Album> getAllAlbums() {
        synchronized (cache) {
            ArrayList<Album> fullAlbums = new ArrayList<>();
            for (Map<Long, MemCache.AlbumSlice> albumsByArtist : cache.albumsByAlbumIdAndArtistId.values()) {
                fullAlbums.add(mergeFullAlbum(albumsByArtist.values()));
            }
            return fullAlbums;
        }
    }

    @NonNull
    private static Album mergeFullAlbum(@NonNull Collection<MemCache.AlbumSlice> albumParts) {
        Album fullAlbum = new Album();
        for (Album fragment : albumParts) {
            for (Song song : fragment.songs) {
                if (fullAlbum.songs.contains(song)) continue;
                fullAlbum.songs.add(song);
            }
        }
        // Maintain sorted album after merge
        Collections.sort(fullAlbum.songs, SongLoader.BY_DISC_TRACK);
        return fullAlbum;
    }

    @NonNull
    public ArrayList<Genre> getAllGenres() {
        synchronized (cache) {
            // Make a copy here, to avoid error while the caller is iterating on the result
            return new ArrayList<>(cache.genresByName.values());
        }
    }

    @Nullable
    public Collection<Song> getSongsForGenre(long genreId) {
        synchronized (cache) {
            return cache.songsByGenreId.get(genreId);
        }
    }

    private void addSong(@NonNull Song song) {
        new AddSongAsyncTask().execute(song);
    }

    boolean addSongImpl(@NonNull Song song, boolean cacheOnly) {
        synchronized (cache) {
            // Race condition check: If the song has been added -> skip
            if (cache.songsById.containsKey(song.id)) {
                return false;
            }

            if (!cacheOnly) {
                TagExtractor.extractTags(song);
            }

            Consumer<List<String>> normNames = (@NonNull List<String> names) -> {
                List<String> normalized = new ArrayList<>();
                for (String name : names) {
                    normalized.add(StringUtil.unicodeNormalize(name));
                }
                names.clear(); names.addAll(normalized);
            };
            normNames.accept(song.albumArtistNames);
            normNames.accept(song.artistNames);
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
            } catch (NumberFormatException ignored) {}

            cache.addSong(song);

            if (!cacheOnly) {
                database.addSong(song);
            }

            notifyDiscographyChanged();

            return true;
        }
    }

    @SuppressLint("StaticFieldLeak") // This task last seconds, much shorter than the lifespan of outer class Discography
    public void triggerSyncWithMediaStore(boolean reset) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                if (snackbar != null) {
                    String message = App.getInstance().getApplicationContext().getString(R.string.scanning_songs_started);
                    snackbar.showProgress(message);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                if (reset) {
                    Discography.this.clear();
                }
                Discography.this.syncWithMediaStore();
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (snackbar != null) {
                    snackbar.dismiss();
                }
            }
        }.execute();
    }

    private void syncWithMediaStore() {
        Context context = App.getInstance().getApplicationContext();

        // zombies are tracks that are removed but still indexed by MediaStore
        Predicate<Song> isZombie = (s) -> !(new File(s.data)).exists();

        // Blacklist
        final ArrayList<String> blackListedPaths = BlacklistStore.getInstance(context).getPaths();
        Predicate<Song> isBlackListed = (s) -> {
            for (String path : blackListedPaths) {
                if (s.data.startsWith(path)) return true;
            }
            return false;
        };

        ArrayList<Song> alienSongs = MediaStoreBridge.getAllSongs(context);
        final HashSet<Long> importedSongIds = new HashSet<>();
        for (Song song : alienSongs) {
            if (isBlackListed.test(song)) continue;
            if (isZombie.test(song)) continue;

            Song matchedSong = getOrAddSong(song);
            importedSongIds.add(matchedSong.id);
        }

        synchronized (cache) {
            // Clean orphan songs (removed from MediaStore)
            Set<Long> cacheSongsId = new HashSet<>(cache.songsById.keySet()); // make a copy
            cacheSongsId.removeAll(importedSongIds);
            removeSongById(cacheSongsId.toArray(new Long[0]));
        }
    }

    @Override
    public void onServiceConnected() {}

    @Override
    public void onServiceDisconnected() {}

    @Override
    public void onQueueChanged() {}

    @Override
    public void onPlayingMetaChanged() {}

    @Override
    public void onPlayStateChanged() {}

    @Override
    public void onRepeatModeChanged() {}

    @Override
    public void onShuffleModeChanged() {}

    @Override
    public void onMediaStoreChanged() {
        triggerSyncWithMediaStore(false);
    }

    public void addChangedListener(Runnable listener) {
        changedListeners.add(listener);
    }

    public void removeChangedListener(Runnable listener) {
        if (mainActivityTaskQueue != null) {
            mainActivityTaskQueue.removeCallbacks(listener);
        }
        changedListeners.remove(listener);
    }

    private void notifyDiscographyChanged() {
        // Notify the main activity to reload the tabs content
        // Since this can be called from a background thread, make it safe by wrapping as an event to main thread
        if (mainActivityTaskQueue != null) {
            // Post as much 1 event per a coalescence period
            final long COALESCENCE_DELAY = 500;
            for (Runnable listener : changedListeners) {
                mainActivityTaskQueue.removeCallbacks(listener);
                mainActivityTaskQueue.postDelayed(listener, COALESCENCE_DELAY);
            }
        }
    }

    public void removeSongByPath(@NotNull String... paths) {
        synchronized (cache) {
            ArrayList<Long> matchingSongIds = new ArrayList<>();
            for (String path : paths) {
                for (Song song : cache.songsById.values()) {
                    if (song.data.equals(path)) {
                        matchingSongIds.add(song.id);
                        break;
                    }
                }
            }
            removeSongById(matchingSongIds.toArray(new Long[0]));
        }
    }

    private void removeSongById(@NotNull Long... songIds) {
        if (songIds.length == 0) return;

        for (long songId : songIds) {
            cache.removeSongById(songId);
            database.removeSongById(songId);
        }
        notifyDiscographyChanged();
    }

    private void clear() {
        database.clear();
        cache.clear();
    }

    private void fetchAllSongs() {
        Collection<Song> songs = database.fetchAllSongs();
        for (Song song : songs) {
            addSongImpl(song, true);
        }
    }
}

package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.discog.tagging.TagExtractor;
import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.sort.SongSortOrder;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.util.FileUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import org.jaudiotagger.tag.reference.GenreTypes;

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
    // TODO wrap this inside the MemCache class
    private final DB database;
    private final MemCache cache;

    private MainActivity mainActivity = null;
    private Handler mainActivityTaskQueue = null;
    private final String TASK_QUEUE_COALESCENCE_TOKEN = "Discography.triggerSyncWithMediaStore";
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
        this.mainActivity = mainActivity;
        mainActivityTaskQueue = new Handler(mainActivity.getMainLooper());

        triggerSyncWithMediaStore(false);
    }

    public void stopService() {
        // Flush the delayed task queue - dont do anything is we are stopping
        if (mainActivityTaskQueue != null && mainActivity != null) {
            mainActivityTaskQueue.removeCallbacksAndMessages(TASK_QUEUE_COALESCENCE_TOKEN);

            mainActivity = null;
            mainActivityTaskQueue = null;
        }
    }

    void setCacheState(MemCache.ConsistencyState value) {
        synchronized (cache) {
            cache.consistencyState = value;
        }
    }

    MemCache.ConsistencyState getCacheState() {
        synchronized (cache) {
            return cache.consistencyState;
        }
    }

    @NonNull
    Song getOrAddSong(@NonNull final Song song) {
        synchronized (cache) {
            Song discogSong = getSong(song.id);
            if (!discogSong.equals(Song.EMPTY_SONG)) {
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

            addSong(song, false);

            return song;
        }
    }

    @NonNull
    public Song getSong(long songId) {
        synchronized (cache) {
            Song song = cache.songsById.get(songId);
            return song == null ? Song.EMPTY_SONG : song;
        }
    }

    @NonNull
    public ArrayList<Song> getSongsFromIdsAndCleanupOrphans(@NonNull ArrayList<Long> songIds, @Nullable Consumer<ArrayList<Long>> orphanIdsCleaner) {
        ArrayList<Long> orphanSongIds = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();

        synchronized (cache) {
            for (Long id : songIds) {
                Song song = cache.songsById.get(id);
                if (song != null) {
                    songs.add(song);
                } else if (orphanIdsCleaner != null) {
                    orphanSongIds.add(id);
                }
            }
        }

        MemCache.ConsistencyState cacheState = getCacheState();
        // In the case where the Discog is being reset situation, the operation takes time,
        // and while the cache is being filled up,
        // correct and existing songs may be considered as orphan
        // --> incorrectly cleaned from the auxiliary DBs (history, queue, etc)
        if ((orphanIdsCleaner != null) && (cacheState != MemCache.ConsistencyState.RESETTING)) {
            orphanIdsCleaner.accept(orphanSongIds);
        }
        return songs;
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

    private int getSongCount() {
        synchronized (cache) {
            return cache.songsById.size();
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
        Collections.sort(fullAlbum.songs, SongSortOrder.BY_DISC_TRACK);
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

    private void addSong(@NonNull Song song, boolean cacheOnly) {
        synchronized (cache) {
            // Race condition check: If the song has been added -> skip
            if (cache.songsById.containsKey(song.id)) {
                return;
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
        }
    }

    public void triggerSyncWithMediaStore(boolean reset) {
        if (getCacheState() != MemCache.ConsistencyState.OK) {
            // Prevent reentrance - delay to later
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                final long DELAY = 500;

                mainActivityTaskQueue.removeCallbacksAndMessages(TASK_QUEUE_COALESCENCE_TOKEN);
                mainActivityTaskQueue.postDelayed(() -> triggerSyncWithMediaStore(reset), TASK_QUEUE_COALESCENCE_TOKEN, DELAY);
            } // else: too bad, just drop the operation. It is unlikely we get there anyway
        } else {
            (new SyncWithMediaStoreAsyncTask(mainActivity, this, reset)).execute();
        }
    }

    int syncWithMediaStore(Consumer<Integer> progressUpdater) {
        final Context context = App.getInstance().getApplicationContext();

        // Zombies are tracks that are removed but still indexed by MediaStore
        Predicate<Song> isZombie = (s) -> !(new File(s.data)).exists();

        // Whitelist
        final File startDirectory = PreferenceUtil.getInstance().getStartDirectory();
        final String startPath = FileUtil.safeGetCanonicalPath(startDirectory);
        Predicate<Song> isNotWhiteListed = (s) -> {
            if (PreferenceUtil.getInstance().getWhitelistEnabled()) {
                return !s.data.startsWith(startPath);
            }
            return false;
        };

        // Blacklist
        final ArrayList<String> blackListedPaths = BlacklistStore.getInstance(context).getPaths();
        Predicate<Song> isBlackListed = (s) -> {
            for (String path : blackListedPaths) {
                if (s.data.startsWith(path)) return true;
            }
            return false;
        };


        final int initialSongCount = getSongCount();
        ArrayList<Song> alienSongs = MediaStoreBridge.getAllSongs(context);
        final HashSet<Long> importedSongIds = new HashSet<>();
        for (Song song : alienSongs) {
            if (isNotWhiteListed.test(song)) continue;
            if (isBlackListed.test(song)) continue;
            if (isZombie.test(song)) continue;

            Song matchedSong = getOrAddSong(song);
            importedSongIds.add(matchedSong.id);

            progressUpdater.accept(getSongCount() - initialSongCount);
        }

        synchronized (cache) {
            // Clean orphan songs (removed from MediaStore)
            Set<Long> cacheSongsId = new HashSet<>(cache.songsById.keySet()); // make a copy
            cacheSongsId.removeAll(importedSongIds);
            removeSongById(cacheSongsId.toArray(new Long[0]));
        }

        return (getSongCount() - initialSongCount);
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
            final long COALESCENCE_DELAY = 50;
            for (Runnable listener : changedListeners) {
                mainActivityTaskQueue.removeCallbacks(listener);
                mainActivityTaskQueue.postDelayed(listener, COALESCENCE_DELAY);
            }
        }
    }

    public void removeSongByPath(@NonNull String... paths) {
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

    private void removeSongById(@NonNull Long... songIds) {
        if (songIds.length == 0) return;

        for (long songId : songIds) {
            cache.removeSongById(songId);
            database.removeSongById(songId);
        }
        notifyDiscographyChanged();
    }

    void clear() {
        database.clear();
        cache.clear();
    }

    private void fetchAllSongs() {
        setCacheState(MemCache.ConsistencyState.REFRESHING);

        Collection<Song> songs = database.fetchAllSongs();
        for (Song song : songs) {
            addSong(song, true);
        }

        setCacheState(MemCache.ConsistencyState.OK);
    }
}

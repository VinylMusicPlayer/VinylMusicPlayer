package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.loader.ReplayGainTagExtractor;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.HistoryStore;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.reference.GenreTypes;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author SC (soncaokim)
 */

public class Discography implements MusicServiceEventListener {
    public static int ICON = R.drawable.ic_bookmark_music_white_24dp;

    @Nullable
    private static Discography sInstance = null;

    private DB database;
    private final MemCache cache;
    private final PlayHistory history;

    public MainActivity mainActivity;
    private Handler mainActivityTaskQueue;
    private Collection<Runnable> changedListeners = new LinkedList<>();

    public Discography() {
        database = new DB();
        cache = new MemCache();
        fetchAllSongs();

        history = new PlayHistory(cache);
        fetchPlayHistory();
    }

    @NonNull
    public static synchronized Discography getInstance() {
        if (sInstance == null) {
            sInstance = new Discography();
        }
        return sInstance;
    }

    public void startService(@NonNull final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mainActivityTaskQueue = new Handler(mainActivity.getMainLooper());
        triggerSyncWithMediaStore(false);
    }

    public void stopService() {}

    @NonNull
    public Song getOrAddSong(@NonNull  final Song song) {
        Song discogSong = getSong(song.id);
        if (discogSong != Song.EMPTY_SONG) {
            BiPredicate<Song, Song> isMetadataObsolete = (final @NonNull Song incomingSong, final @NonNull Song cachedSong) -> {
                if (incomingSong.dateAdded != cachedSong.dateAdded) return true;
                if (incomingSong.dateModified != cachedSong.dateModified) return true;
                if (!incomingSong.data.equals(cachedSong.data)) return true;
                return false;
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

    @Nullable
    public Artist getArtistByName(String artistName) {
        synchronized (cache) {
            return cache.artistsByName.get(artistName);
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
            Map<Long, MemCache.AlbumSlice> albumsByArtist = cache.albumsByAlbumIdAndArtistId.get(albumId);
            if (albumsByArtist == null) return null;
            return mergeFullAlbum(albumsByArtist.values());
        }
    }

    @NonNull
    public Collection<Album> getAllAlbums() {
        synchronized (cache) {
            ArrayList<Album> fullAlbums = new ArrayList<>();
            for (Map<Long, MemCache.AlbumSlice> albumsByArtist : cache.albumsByAlbumIdAndArtistId.values()) {
                fullAlbums.add(mergeFullAlbum(albumsByArtist.values()));
            }
            return fullAlbums;
        }
    }

    @NonNull
    private Album mergeFullAlbum(@NonNull Collection<MemCache.AlbumSlice> albumParts) {
        Album fullAlbum = new Album();
        for (Album fragment : albumParts) {
            for (Song song : fragment.songs) {
                if (fullAlbum.songs.contains(song)) continue;
                fullAlbum.songs.add(song);
            }
        }
        // Maintain sorted album after merge
        Collections.sort(fullAlbum.songs,
                (s1, s2) -> (s1.discNumber != s2.discNumber)
                        ? (s1.discNumber - s2.discNumber)
                        : (s1.trackNumber - s2.trackNumber)
        );
        return fullAlbum;
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

    @NonNull
    public ArrayList<Song> getPlayedSongs(long cutoff) {
        final Collection<List<Long>> idLists =
                (cutoff > 0)
                        ? history.songsByTimePlayed.tailMap(cutoff).values()
                        : history.songsByTimePlayed.headMap(-1 * cutoff).values();
        ArrayList<Song> songs = new ArrayList<>();
        for (List<Long> idList : idLists) {
            for (long id : idList) {
                songs.add(getSong(id));
            }
        }
        return songs;
    }

    private void addSong(@NonNull Song song) {
        new AddSongAsyncTask().execute(song);
    }

    public void addPlayedSong(long songId, long playedTime) {
        history.add(songId, playedTime);
    }

    boolean addSongImpl(@NonNull Song song, boolean cacheOnly) {
        synchronized (cache) {
            // Race condition check: If the song has been added -> skip
            if (cache.songsById.containsKey(song.id)) {
                return false;
            }

            if (!cacheOnly) {
                extractTags(song);
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

    public void triggerSyncWithMediaStore(boolean reset) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (reset) {
                    Discography.this.clear();
                }
                Discography.this.syncWithMediaStore();
                return true;
            }
        }.execute();
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
        mainActivityTaskQueue.removeCallbacks(listener);
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

    private void extractTags(@NonNull Song song) {
        try {
            // Override with metadata extracted from the file ourselves
            AudioFile file = AudioFileIO.read(new File(song.data));
            Tag tags = file.getTagOrCreateAndSetDefault();

            Function<FieldKey, String> safeGetTag = (tag) -> {
                try {return tags.getFirst(tag).trim();}
                catch (KeyNotFoundException ignored) {return "";}
                catch (UnsupportedOperationException ignored){ return "";}
            };
            Function<FieldKey, Integer> safeGetTagAsInteger = (tag) -> {
                try {return Integer.parseInt(safeGetTag.apply(tag));}
                catch (NumberFormatException ignored) {return 0;}
            };
            Function<FieldKey, List<String>> safeGetTagAsList = (tag) -> {
                try {return tags.getAll(tag);}
                catch (KeyNotFoundException ignored) {return new ArrayList<>(Arrays.asList(""));}
            };

            song.albumName = safeGetTag.apply(FieldKey.ALBUM);
            song.artistNames  = MultiValuesTagUtil.splitIfNeeded(safeGetTagAsList.apply(FieldKey.ARTIST));
            song.albumArtistNames = MultiValuesTagUtil.splitIfNeeded(safeGetTagAsList.apply(FieldKey.ALBUM_ARTIST));
            song.title = safeGetTag.apply(FieldKey.TITLE);
            if (song.title.isEmpty()) {
                // fallback to use the file name
                song.title = file.getFile().getName();
            }

            song.genre = safeGetTag.apply(FieldKey.GENRE);
            song.discNumber = safeGetTagAsInteger.apply(FieldKey.DISC_NO);
            song.trackNumber = safeGetTagAsInteger.apply(FieldKey.TRACK);
            song.year = safeGetTagAsInteger.apply(FieldKey.YEAR);

            ReplayGainTagExtractor.setReplayGainValues(song);
        } catch (Exception e) {
            e.printStackTrace();
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

    private void removeSongById(long songId) {
        cache.removeSongById(songId);
        database.removeSongById(songId);

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

    private void fetchPlayHistory() {
        try (Cursor cursor = HistoryStore.getInstance(App.getInstance().getApplicationContext()).queryRecentIds(0)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int idColumn = cursor.getColumnIndex(HistoryStore.RecentStoreColumns.ID);
                final int idTimePlayed = cursor.getColumnIndex(HistoryStore.RecentStoreColumns.TIME_PLAYED);

                do {
                    long id = cursor.getLong(idColumn);
                    long time = cursor.getLong(idTimePlayed);
                    history.add(id, time);
                } while (cursor.moveToNext());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PlayHistory {
        private final long NEVER_PLAYED = 0;

        private final SortedMap<Long, List<Long>> songsByTimePlayed = new TreeMap<>();
        private final Map<Long, Long> timePlayedBySongId = new HashMap<>();

        private final MemCache songCache;

        public PlayHistory(@NonNull final MemCache cache) {
            songCache = cache;

            // For all songs, initialize them as never played
            // TODO Sort by date added
            ArrayList<Long> allSongIds = new ArrayList<>(songCache.songsById.keySet()); // make a copy
            songsByTimePlayed.put(NEVER_PLAYED, allSongIds);
            for (long id : allSongIds) {
                timePlayedBySongId.put(id, NEVER_PLAYED);
            }
        }

        public void remove(long songId) {
            final Long time = timePlayedBySongId.get(songId);
            if (time == null) {return;}

            List<Long> songIds = songsByTimePlayed.get(time);
            if (songIds == null) {return;}

            songIds.remove(songId);
            songsByTimePlayed.put(time, songIds);
            timePlayedBySongId.put(songId, NEVER_PLAYED);
        }

        public void add(long songId, long time) {
            remove(songId);

            List<Long> songs = songsByTimePlayed.get(time);
            if (songs == null) {
                songs = new ArrayList<>();
            }
            songs.add(songId);
            songsByTimePlayed.put(time, songs);
            timePlayedBySongId.put(songId, time);
        }
    }
}

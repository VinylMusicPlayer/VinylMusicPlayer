package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
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
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.reference.GenreTypes;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author SC (soncaokim)
 */

public class Discography implements MusicServiceEventListener {
    public static int ICON = R.drawable.ic_bookmark_music_white_24dp;

    @Nullable
    private static Discography sInstance = null;

    private DB database;
    private final MemCache cache;

    MainActivity mainActivity;
    private Runnable mainActivityRefreshTask = () -> mainActivity.onMediaStoreChanged();
    private Handler mainActivityTaskQueue;

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
            if (song.data.equals(discogSong.data) && song.dateAdded == discogSong.dateAdded && song.dateModified == discogSong.dateModified) {
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
                extractTags(song);
            }

            // As the song metadata can come from MediaStore (no multi-artist support)
            // or from our own ID3 extractor (which handles multi-artist support)
            // -> split if is not already done
            final boolean possiblyNotSplit = (song.artistNames.size() == 1); // size = 0 -> nothing to do; size > 1 -> already split
            if (possiblyNotSplit) {
                song.artistNames = MusicUtil.artistNamesSplit(song.artistNames.get(0));
            }

            // Unicode normalization
            ArrayList<String> artistNames = new ArrayList<>();
            for (String name : song.artistNames) {
                artistNames.add(StringUtil.unicodeNormalize(name));
            }
            song.artistNames = artistNames;
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

    void notifyDiscographyChanged() {
        // Notify the main activity to reload the tabs content
        // Since this can be called from a background thread, make it safe by wrapping as an event to main thread
        if (mainActivityTaskQueue != null) {
            // Post as much 1 event per a coalescence period
            final long COALESCENCE_DELAY = 500;
            mainActivityTaskQueue.removeCallbacks(mainActivityRefreshTask);
            mainActivityTaskQueue.postDelayed(mainActivityRefreshTask, COALESCENCE_DELAY);
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
            };
            Function<FieldKey, Integer> safeGetTagAsInteger = (tag) -> {
                try {return Integer.parseInt(safeGetTag.apply(tag));}
                catch (NumberFormatException ignored) {return 0;}
            };

            song.albumName = safeGetTag.apply(FieldKey.ALBUM);
            song.artistNames  = MusicUtil.artistNamesSplit(safeGetTag.apply(FieldKey.ARTIST));
            song.albumArtistNames = MusicUtil.artistNamesSplit(safeGetTag.apply(FieldKey.ALBUM_ARTIST));
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
}

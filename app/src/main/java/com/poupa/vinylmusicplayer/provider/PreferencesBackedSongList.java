package com.poupa.vinylmusicplayer.provider;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.IndexedSong;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PrefKey;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Ordered list of songs, backed by a persistent storage
 * Can be used for playlist, play queue
 *
 * @author SC (soncaokim)
 */
abstract class SongList {
    @NonNull public String name;
    final List<Long> songIds = new ArrayList<>();

    SongList(@NonNull String name) {
        this.name = name;
        load();
    }

    abstract void load();
    abstract void save(@Nullable String newName);

    @NonNull
    public String getName() {return name;}

    public boolean contains(long songId) {
        return songIds.contains(songId);
    }

    @NonNull
    public List<? extends Song> asSongs() {
        ArrayList<Song> result = new ArrayList<>();
        ArrayList<Long> orphanIds = new ArrayList<>();

        // Since the song list is decoupled from Discography, we need to check its content
        // against the valid songs in discog
        final Map<Long, Song> availableSongsById = Discography.getInstance().getAllSongsById();
        for (final Long id : songIds) {
            final Song matchingSong = availableSongsById.get(id);
            if (matchingSong != null) {
                result.add(matchingSong);
            } else {
                orphanIds.add(id);
            }
        }

        if (!orphanIds.isEmpty()) {
            songIds.removeAll(orphanIds);
            save(null);
        }
        return result;
    }
}

abstract class MutableSongList extends SongList {
    MutableSongList(@NonNull String name) {
        super(name);
    }

    public void addSongs(@NonNull List<Song> songs) {
        for (Song song : songs) {
            songIds.add(song.id);
        }
        save(null);
    }

    public void removeSong(long id) {
        songIds.remove(id);
        save(null);
    }

    public void removeSongsAtPosition(@NonNull final List<Integer> positions) {
        final List<Integer> reversedPositions = new ArrayList<>(positions);
        reversedPositions.sort(Comparator.reverseOrder());
        for (final int position : reversedPositions) {
            if (position >= 0 && position < songIds.size()) {
                songIds.remove(position);
            }
        }
        save(null);
    }

    public boolean moveSong(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {return true;}

        final int size = songIds.size();
        if (fromPosition < 0 || fromPosition >= size) {return false;}
        if (toPosition < 0 || toPosition >= size) {return false;}

        final long movedSongId = songIds.get(fromPosition);
        songIds.remove(fromPosition);

        final int toPositionShift = fromPosition < toPosition ? -1 : 0;
        songIds.add(toPosition + toPositionShift, movedSongId);

        save(null);

        return true;
    }

    public void rename(@NonNull final String newName) {
        save(newName);
    }
}

public class PreferencesBackedSongList extends MutableSongList {
    private static final String SEPARATOR = ",";
    private static final String PREF_NAME_PREFIX = PrefKey.nonExportablePrefixedKey("SONG_IDS_");

    private static SharedPreferences preferences = null;
    static SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(App.getStaticContext());
        }
        return preferences;
    }

    static List<PreferencesBackedSongList> loadAll() {
        ArrayList<PreferencesBackedSongList> result = new ArrayList<>();

        String favoritesPlaylistName = App.getStaticContext().getString(R.string.favorites);
        PreferencesBackedSongList favoritesPlaylist = null;

        final SharedPreferences preferences = getPreferences();
        for (String prefName : preferences.getAll().keySet()) {
            if (prefName.startsWith(PREF_NAME_PREFIX)) {
                final String name = prefName.substring(PREF_NAME_PREFIX.length());
                if (name.equals(favoritesPlaylistName)){
                    favoritesPlaylist = new PreferencesBackedSongList(name);
                    continue;
                }
                result.add(new PreferencesBackedSongList(name));
            }
        }

        Collections.sort(result, (l1, l2) -> StringUtil.compareIgnoreAccent(l1.name, l2.name));

        if (favoritesPlaylist != null)
            result.add(0, favoritesPlaylist);

        return result;
    }

    static void remove(@NonNull String name) {
        final SharedPreferences preferences = getPreferences();
        preferences.edit().remove(PREF_NAME_PREFIX + name).apply();
    }

    PreferencesBackedSongList(@NonNull String name) {
        super(name);
    }

    @Override
    void load() {
        final SharedPreferences preferences = getPreferences();
        String values = preferences.getString(PREF_NAME_PREFIX + name, "");

        songIds.clear();
        try {
            for (String id : values.split(SEPARATOR)) {songIds.add(Long.valueOf(id));}
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    void save(@Nullable String newName) {
        StringBuilder values = new StringBuilder();
        for (Long id : songIds) {
            if (values.length() > 0) {values.append(SEPARATOR);}
            values.append(id);
        }

        final SharedPreferences preferences = getPreferences();
        if (newName != null) {
            preferences.edit().remove(PREF_NAME_PREFIX + name).apply();
            name = newName;
        }
        preferences.edit().putString(PREF_NAME_PREFIX + name, values.toString()).apply();
    }
}

class PreferenceBackedReorderableSongList extends PreferencesBackedSongList {
    PreferenceBackedReorderableSongList(@NonNull final String name) {
        super(name);
    }

    // Assign a stable and unique ID to each song in the list. That ID can then be used as UI RecycleView's ID
    // - to be unique: the Song's id cannot be used since the list can contain duplicate of same song
    // - to be stable: the postition of the song in the list cannot be used as an ID since the song can be moved (hence the position changes)

    private long nextUniqueId = 0L;

    @Override
    @NonNull
    public List<? extends Song> asSongs() {
        final List<? extends Song> songs = super.asSongs();
        final int count = songs.size();

        final ArrayList<IndexedSong> indexedSongs = new ArrayList<>(count);
        for (int i=0; i<count; ++i) {
            ++nextUniqueId;
            indexedSongs.add(new IndexedSong(songs.get(i), i, nextUniqueId));
        }

        return indexedSongs;
    }
}
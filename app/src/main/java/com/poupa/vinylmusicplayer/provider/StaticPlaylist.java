package com.poupa.vinylmusicplayer.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistSongLoader;
import com.poupa.vinylmusicplayer.model.Playlist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author SC (soncaokim)
 */
public class StaticPlaylist extends PreferencesBackedSongList {
    public static final String PREF_MIGRATED_STATIC_PLAYLISTS = "migrated_static_playlists";

    @NonNull
    public static List<StaticPlaylist> getAllPlaylists() {
        List<StaticPlaylist> migratedPlaylists = new ArrayList<>();
        Set<String> migratedNames = new HashSet<>();
        for (PreferencesBackedSongList playlist : PreferencesBackedSongList.loadAll()) {
            migratedNames.add(playlist.name);
            migratedPlaylists.add(new StaticPlaylist(playlist.name));
        }

        final Context context = App.getStaticContext();
        List<Playlist> playlistsToMigrate = PlaylistLoader.getAllPlaylists(context);

        final SharedPreferences preferences = getPreferences();
        migratedNames.addAll(preferences.getStringSet(PREF_MIGRATED_STATIC_PLAYLISTS, new HashSet<>()));

        for (Playlist playlist : playlistsToMigrate) {
            if (migratedNames.contains(playlist.name)) {continue;}

            StaticPlaylist importedPlaylist = new StaticPlaylist(playlist.name);
            importedPlaylist.addSongs(PlaylistSongLoader.getPlaylistSongList(context, playlist.id));

            migratedPlaylists.add(importedPlaylist);
            migratedNames.add(playlist.name);

            // Note: Don't delete migrated playlists here, for two reasons:
            // - since playlist can be shared with other apps, this will be a destructive action
            // - this *would* require extra privilege (see https://github.com/AdrienPoupa/VinylMusicPlayer/pull/298)
        }

        // Set a persistent marker in prefs, to avoid doing this again
        if (!migratedNames.isEmpty()) {
            preferences.edit().putStringSet(PREF_MIGRATED_STATIC_PLAYLISTS, migratedNames).apply();
        }

        return migratedPlaylists;
    }

    @Nullable
    public static StaticPlaylist getPlaylist(long id) {
        List<StaticPlaylist> all = getAllPlaylists();
        for (StaticPlaylist item : all) {
            Playlist playlist = item.asPlaylist();
            if (playlist.id == id) {return item;}
        }
        return null;
    }

    @Nullable
    public static StaticPlaylist getPlaylist(@NonNull String playlistName) {
        List<StaticPlaylist> all = getAllPlaylists();
        for (StaticPlaylist item : all) {
            Playlist playlist = item.asPlaylist();
            if (TextUtils.equals(playlist.name, playlistName)) {return item;}
        }
        return null;
    }

    public static StaticPlaylist getOrCreatePlaylist(@NonNull String name) {
        StaticPlaylist result = getPlaylist(name);
        if (result == null) {
            result = new StaticPlaylist(name);
            result.save(null);
        }
        return result;
    }

    public static void removePlaylist(@NonNull String name) {
        remove(name);
    }

    public StaticPlaylist(@NonNull String name) {
        super(name);
    }

    public Playlist asPlaylist() {
        return new Playlist(
                // MD5-based algo, supposed to yield more unique ID than String.hash
                UUID.nameUUIDFromBytes(name.getBytes()).getMostSignificantBits(),
                name
        );
    }
}

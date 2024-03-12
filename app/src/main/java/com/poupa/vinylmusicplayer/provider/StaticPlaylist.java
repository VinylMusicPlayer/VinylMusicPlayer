package com.poupa.vinylmusicplayer.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistSongLoader;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author SC (soncaokim)
 */
public class StaticPlaylist extends PreferenceBackedReorderableSongList {
    private static final String PREF_MIGRATED_STATIC_PLAYLISTS = "migrated_static_playlists";

    @NonNull
    private static List<StaticPlaylist> importDevicePlaylists(@NonNull final Context context, @NonNull final Set<String> internalNames) {
        List<StaticPlaylist> importedPlaylists = new ArrayList<>();

        final SharedPreferences preferences = getPreferences();
        final boolean noMigrationMarker = !preferences.contains(PREF_MIGRATED_STATIC_PLAYLISTS);

        Set<String> previouslyMigratedNames = new HashSet<>(preferences.getStringSet(PREF_MIGRATED_STATIC_PLAYLISTS, new HashSet<>()));
        Set<String> skippedNames = new HashSet<>();
        Set<String> nowMigratedNames = new HashSet<>();

        final boolean keepMediaStorePlaylistsInSync = true;

        for (Playlist playlist : PlaylistLoader.getAllPlaylists(context)) {
            final String name = playlist.name;
            if (internalNames.contains(name)) {
                // don't overwrite internal ones
                skippedNames.add(name);
                continue;
            }

            if (previouslyMigratedNames.contains(name) && !keepMediaStorePlaylistsInSync) {
                // don't migrate again
                nowMigratedNames.add(name);
                continue;
            }

            StaticPlaylist importedPlaylist = new StaticPlaylist(name);
            importedPlaylist.addSongs(PlaylistSongLoader.getPlaylistSongList(context, playlist.id));

            importedPlaylists.add(importedPlaylist);
            nowMigratedNames.add(name);

            // Note: Don't delete migrated playlists here.
            // since playlist can be shared with other apps, this will be a destructive action
        }

        // Set a persistent marker in prefs, to avoid doing this again
        final boolean changed = !previouslyMigratedNames.containsAll(nowMigratedNames) || !nowMigratedNames.containsAll(previouslyMigratedNames);
        if (noMigrationMarker || changed) {
            preferences.edit().putStringSet(PREF_MIGRATED_STATIC_PLAYLISTS, nowMigratedNames).apply();
        }

        if (noMigrationMarker) {
            new Handler(context.getMainLooper()).post(() -> {
                final String message = !skippedNames.isEmpty()
                    ? context.getResources().getString(R.string.imported_x_skipped_x_playlists, nowMigratedNames.size(), skippedNames.size())
                    : context.getResources().getString(R.string.imported_x_playlists, nowMigratedNames.size());
                SafeToast.show(context, message);
            });
        }

        return importedPlaylists;
    }

    @NonNull
    public static List<StaticPlaylist> getAllPlaylists() {
        List<StaticPlaylist> internalPlaylists = new ArrayList<>();
        Set<String> internalNames = new HashSet<>();
        for (PreferencesBackedSongList playlist : PreferencesBackedSongList.loadAll()) {
            internalNames.add(playlist.name);
            internalPlaylists.add(new StaticPlaylist(playlist.name));
        }

        List<StaticPlaylist> importedPlaylists = importDevicePlaylists(App.getStaticContext(), internalNames);
        internalPlaylists.addAll(importedPlaylists);

        return internalPlaylists;
    }

    @Nullable
    public static StaticPlaylist getPlaylist(final long id) {
        List<StaticPlaylist> all = getAllPlaylists();
        for (StaticPlaylist item : all) {
            Playlist playlist = item.asPlaylist();
            if (playlist.id == id) {return item;}
        }
        return null;
    }

    @Nullable
    public static StaticPlaylist getPlaylist(@NonNull final String playlistName) {
        List<StaticPlaylist> all = getAllPlaylists();
        for (StaticPlaylist item : all) {
            Playlist playlist = item.asPlaylist();
            if (TextUtils.equals(playlist.name, playlistName)) {return item;}
        }
        return null;
    }

    @NonNull
    public static StaticPlaylist getOrCreatePlaylist(@NonNull final String name) {
        StaticPlaylist result = getPlaylist(name);
        if (result == null) {
            result = new StaticPlaylist(name);
            result.save(null);
        }
        return result;
    }

    public static void removePlaylist(@NonNull final String name) {
        remove(name);
    }

    public StaticPlaylist(@NonNull final String name) {
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

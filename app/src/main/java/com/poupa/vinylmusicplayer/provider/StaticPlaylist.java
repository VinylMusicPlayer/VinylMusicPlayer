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
import com.poupa.vinylmusicplayer.model.PlaylistSong;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class StaticPlaylist extends PreferencesBackedSongList {
    private static final String PREF_STATIC_PLAYLISTS_MIGRATED = "static_playlists_migrated";

    @NonNull
    public static List<StaticPlaylist> getAllPlaylists() {
        final SharedPreferences preferences = getPreferences();
        if (preferences.getBoolean(PREF_STATIC_PLAYLISTS_MIGRATED, false)) {
            List<StaticPlaylist> result = new ArrayList<>();
            for (PreferencesBackedSongList playlist : PreferencesBackedSongList.loadAll()) {
                result.add(new StaticPlaylist(playlist.name));
            }
            return result;
        } else {
            // Migrate from MediaStore
            final Context context = App.getStaticContext();
            List<Playlist> playlistsToMigrate = PlaylistLoader.getAllPlaylists(context);

            List<StaticPlaylist> result = new ArrayList<>();
            for (Playlist playlist : playlistsToMigrate) {
                StaticPlaylist importedPlaylist = new StaticPlaylist(playlist.name);
                importedPlaylist.addSongs(PlaylistSongLoader.getPlaylistSongList(context, playlist.id));

                result.add(importedPlaylist);
            }

            // Note: Dont delete migrated playlists here, for two reasons:
            // - since playlist can be shared with other apps, this will be a destructive action
            // - this *would* require extra priviledge (see https://github.com/AdrienPoupa/VinylMusicPlayer/pull/298)

            // Set a persistent marker in prefs, to avoid doing this again
            // Otherwise the MediaStore playlists will be reimported whence there is no static playlist in the prefs
            preferences.edit().putBoolean(PREF_STATIC_PLAYLISTS_MIGRATED, true).apply();

            return result;
        }
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
        // TODO Dont use hashcode as a substitude for id
        return new Playlist(name.hashCode(), name);
    }

    @Override
    @NonNull
    public ArrayList<Song> asSongs() {
        // TODO Review this suboptimal implementation, probably by removing the PlaylistSong class
        // Repackage the song lists as a list of PlaylistSong
        final ArrayList<Song> songs = super.asSongs();
        final long playlistId = asPlaylist().id;

        ArrayList<Song> result = new ArrayList<>();
        for (Song song : songs) {
            result.add(new PlaylistSong(song, playlistId, result.size()));
        }
        return result;
    }
}

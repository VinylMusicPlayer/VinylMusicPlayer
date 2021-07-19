package com.poupa.vinylmusicplayer.provider;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistSongLoader;
import com.poupa.vinylmusicplayer.model.Playlist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class StaticPlaylist extends PreferencesBackedSongList {
    @NonNull
    public static List<StaticPlaylist> getAllPlaylists() {
        List<StaticPlaylist> result = new ArrayList<>();

        List<PreferencesBackedSongList> songLists = PreferencesBackedSongList.loadAll();
        if (songLists.isEmpty()) {
            // Migrate from MediaStore
            final Context context = App.getStaticContext();
            List<Playlist> playlistsToMigrate = PlaylistLoader.getAllPlaylists(context);
            for (Playlist playlist : playlistsToMigrate) {
                StaticPlaylist list = new StaticPlaylist(playlist.name);
                list.addSongs(PlaylistSongLoader.getPlaylistSongList(context, playlist.id));

                songLists.add(list);
            }
            // TODO Delete migrated playlist?
        } else {
            for (PreferencesBackedSongList songList : songLists) {
                result.add(new StaticPlaylist(songList.name));
            }
        }

        return result;
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
}

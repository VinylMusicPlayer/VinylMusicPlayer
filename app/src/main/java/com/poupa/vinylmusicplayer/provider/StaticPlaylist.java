package com.poupa.vinylmusicplayer.provider;

import android.content.Context;

import androidx.annotation.NonNull;

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
    public static List<Playlist> getAllPlaylists() {
        List<String> names = PreferencesBackedSongList.loadAll();
        if (names.isEmpty()) {
            // Migrate from MediaStore
            final Context context = App.getStaticContext();
            List<Playlist> playlistsToMigrate = PlaylistLoader.getAllPlaylists(context);
            for (Playlist playlist : playlistsToMigrate) {
                StaticPlaylist list = new StaticPlaylist(playlist.name);
                list.addSongs(PlaylistSongLoader.getPlaylistSongList(context, playlist.id));

                names.add(playlist.name);
            }
            // TODO Delete migrated playlist?
        }

        List<Playlist> playlists = new ArrayList<>();
        for (String name : names) {
            StaticPlaylist list = new StaticPlaylist(name);
            // TODO Dont use hashcode as a substitude for id
            playlists.add(new Playlist(name.hashCode(), name));
        }
        return playlists;
    }

    public StaticPlaylist(@NonNull String name) {
        super(name);
    }
}

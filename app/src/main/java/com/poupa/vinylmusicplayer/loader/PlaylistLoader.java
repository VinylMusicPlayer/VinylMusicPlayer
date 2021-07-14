package com.poupa.vinylmusicplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.model.Playlist;

import java.util.ArrayList;

public class PlaylistLoader {

    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@NonNull final Context context) {
        ArrayList<Playlist> playlists = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID, // 0
                        PlaylistsColumns.NAME // 1
                },
                null,
                null,
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER))
        {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    final long id = cursor.getLong(0);
                    final String name = cursor.getString(1);

                    playlists.add(new Playlist(id, name));
                } while (cursor.moveToNext());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return playlists;
    }

    @NonNull
    public static Playlist getPlaylist(@NonNull final Context context, final long playlistId) {
        ArrayList<Playlist> all = getAllPlaylists(context);
        for (Playlist playlist : all) {
            if (playlist.id == playlistId) {return playlist;}
        }
        return new Playlist();
    }

    @NonNull
    public static Playlist getPlaylist(@NonNull final Context context, final String playlistName) {
        ArrayList<Playlist> all = getAllPlaylists(context);
        for (Playlist playlist : all) {
            if (TextUtils.equals(playlist.name, playlistName)) {return playlist;}
        }
        return new Playlist();
    }
}

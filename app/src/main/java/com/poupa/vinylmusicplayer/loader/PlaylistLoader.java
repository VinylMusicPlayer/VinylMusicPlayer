package com.poupa.vinylmusicplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;

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
}

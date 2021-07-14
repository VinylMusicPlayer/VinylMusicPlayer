package com.poupa.vinylmusicplayer.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.M3UWriter;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.PlaylistSong;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsUtil {
    private static void notifyChange(@NonNull final Context context, @NonNull Uri uri) {
        // TODO Use a more appropriate notification
        context.sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final long playlistId) {
        return (PlaylistLoader.getPlaylist(context, playlistId).id != -1);
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final String name) {
        return (PlaylistLoader.getPlaylist(context, name).id != -1);
    }

    public static long createPlaylist(@NonNull final Context context, @Nullable final String name) {
        // TODO Use StaticPlaylist

        long id = -1;
        if (!TextUtils.isEmpty(name)) {
            try (Cursor cursor = context.getContentResolver().query(EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Playlists._ID},
                    MediaStore.Audio.PlaylistsColumns.NAME + "='" + name + "'",
                    null, null))
            {
                if (cursor == null || cursor.getCount() < 1) {
                    final ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                    final Uri uri = context.getContentResolver().insert(
                            EXTERNAL_CONTENT_URI,
                            values);
                    if (uri != null) {
                        notifyChange(context, uri);
                        Toast.makeText(context, context.getResources().getString(
                                R.string.created_playlist_x, name), Toast.LENGTH_SHORT).show();
                        id = Long.parseLong(uri.getLastPathSegment());
                    }
                } else {
                    // Playlist exists
                    if (cursor.moveToFirst()) {
                        id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        if (id == -1) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.could_not_create_playlist), Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final ArrayList<Playlist> playlists) {
        // TODO Use StaticPlaylist

        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists._ID + " IN (");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).id);
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        try {
            context.getContentResolver().delete(EXTERNAL_CONTENT_URI, selection.toString(), null);
            notifyChange(context, EXTERNAL_CONTENT_URI);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void addToPlaylist(@NonNull final Context context, final Song song, final long playlistId, final boolean showToastOnFinish) {
        List<Song> helperList = List.of(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }

    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<Song> songs, final long playlistId, final boolean showToastOnFinish) {
        // TODO Use StaticPlaylist

        final int size = songs.size();
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);

        try {
            int base = 0;
            try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    base = cursor.getInt(0) + 1;
                }
            }

            int numInserted = 0;
            for (int offSet=0, batchSize=1000; offSet < size; offSet += batchSize) {
                numInserted += resolver.bulkInsert(uri, makeInsertItems(songs, offSet, batchSize, base));
            }
            notifyChange(context, uri);

            if (showToastOnFinish) {
                Toast.makeText(context, context.getResources().getString(
                        R.string.inserted_x_songs_into_playlist_x, numInserted, getNameForPlaylist(context, playlistId)), Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private static ContentValues[] makeInsertItems(@NonNull final List<Song> songs, final int offset, int len, final int base) {
        if (offset + len > songs.size()) {
            len = songs.size() - offset;
        }

        ContentValues[] contentValues = new ContentValues[len];

        for (int i = 0; i < len; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(offset + i).id);
        }
        return contentValues;
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, long playlistId) {
        // TODO Use StaticPlaylist

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selection = MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + song.id;

        try {
            context.getContentResolver().delete(uri, selection, null);
            notifyChange(context, uri);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
        // TODO Use StaticPlaylist

        final long playlistId = songs.get(0).playlistId;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(MediaStore.VOLUME_EXTERNAL, playlistId);
        String selection = MediaStore.Audio.Playlists.Members._ID + " in (";
        for (PlaylistSong song : songs) selection += song.idInPlayList + ", ";
        selection = selection.substring(0, selection.length() - 2) + ")";

        try {
            context.getContentResolver().delete(uri, selection, null);
            notifyChange(context, uri);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static boolean doesPlaylistContain(@NonNull final Context context, final long playlistId, final long songId) {
        // TODO Use StaticPlaylist

        if (playlistId != -1) {
            try {
                Cursor c = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri(MediaStore.VOLUME_EXTERNAL, playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID},
                        MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?",
                        new String[]{String.valueOf(songId)}, null
                );
                int count = 0;
                if (c != null) {
                    count = c.getCount();
                    c.close();
                }
                return count > 0;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean moveItem(@NonNull final Context context, long playlistId, int from, int to) {
        // TODO Use StaticPlaylist

        boolean res = MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(),
                playlistId, from, to);
        // NOTE: actually for now lets disable this because it messes with the animation (tested on Android 11)
        // notifyChange(context, ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, playlistId));
        return res;
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        // TODO Use StaticPlaylist

        Uri playlistUri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.PlaylistsColumns.NAME, newName);
        try {
            context.getContentResolver().update(
                    playlistUri,
                    contentValues,
                    null,
                    null
            );

            notifyChange(context, playlistUri);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        return PlaylistLoader.getPlaylist(context, id).name;
    }

    public static File savePlaylist(Context context, Playlist playlist) throws IOException {
        return M3UWriter.write(context, new File(Environment.getExternalStorageDirectory(), "Playlists"), playlist);
    }
}

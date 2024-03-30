package com.poupa.vinylmusicplayer.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.M3UConstants;
import com.poupa.vinylmusicplayer.helper.M3UWriter;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.service.MusicService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class PlaylistsUtil {
    public static void notifyChange(@NonNull final Context context) {
        context.sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
    }

    public static boolean doesPlaylistExist(final String name) {
        return (StaticPlaylist.getPlaylist(name) != null);
    }

    public static long createPlaylist(@NonNull final Context context, @NonNull final String name) {
        StaticPlaylist playlist = StaticPlaylist.getPlaylist(name);
        if (playlist != null) {
            return playlist.asPlaylist().id;
        }

        long id = StaticPlaylist.getOrCreatePlaylist(name).asPlaylist().id;
        if (id != -1) {
            notifyChange(context);

            SafeToast.show(context, context.getResources().getString(R.string.created_playlist_x, name));
        } else {
            SafeToast.show(context, context.getResources().getString(R.string.could_not_create_playlist));
        }
        return id;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final List<? extends Playlist> playlists) {
        for (int i = 0; i < playlists.size(); i++) {
            final String name = playlists.get(i).name;
            StaticPlaylist.removePlaylist(name);
            deletePlaylistFromMediaStore(context, name);
        }
        notifyChange(context);
    }

    private static void deletePlaylistFromMediaStore(@NonNull final Context context, @NonNull final String name) {
        @NonNull final ContentResolver resolver = context.getContentResolver();
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.PlaylistsColumns.NAME + "='" + name + "'",
                null);
    }

    public static void addToPlaylist(@NonNull final Context context, final Song song, final long playlistId, final boolean showToastOnFinish) {
        List<Song> helperList = List.of(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }

    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<Song> songs, final long playlistId, final boolean showToastOnFinish) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return;}

        list.addSongs(songs);
        notifyChange(context);

        if (showToastOnFinish) {
            SafeToast.show(
                    context,
                    context.getResources().getString(
                            R.string.inserted_x_songs_into_playlist_x,
                            songs.size(),
                            list.getName())
            );
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, long playlistId) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return;}

        list.removeSong(song.id);
        notifyChange(context);
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<Integer> songPositions, long playlistId) {
        if (songPositions.isEmpty()) {return;}

        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return;}

        list.removeSongsAtPosition(songPositions);
        notifyChange(context);
    }

    public static boolean doesPlaylistContain(final long playlistId, final long songId) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return false;}
        return list.contains(songId);
    }

    public static boolean moveItem(long playlistId, int from, int to) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return false;}

        return list.moveSong(from, to);
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(id);
        if (list == null) {return;}

        list.rename(newName);
        notifyChange(context);
    }

    @NonNull
    public static String getNameForPlaylist(final long id) {
        StaticPlaylist playlist = StaticPlaylist.getPlaylist(id);
        if (playlist == null) {return "";}
        return playlist.getName();
    }

    @Nullable
    public static String savePlaylist(@NonNull final Context context, @NonNull final Playlist playlist) throws IOException {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            return savePlaylist_Api28(context, playlist);
        } else {
            return savePlaylist_Api29(context, playlist);
        }
    }

    @NonNull
    private static String savePlaylist_Api28(@NonNull final Context context, @NonNull final Playlist playlist) throws IOException {
        final File path = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_MUSIC);
        if (!path.exists()) {path.mkdirs();}

        final File m3uFile = new File(path, playlist.name + "." + M3UConstants.EXTENSION);
        M3UWriter.write(context, new FileOutputStream(m3uFile), playlist);

        return Environment.DIRECTORY_MUSIC + File.pathSeparator + playlist.name;
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Nullable
    private static String savePlaylist_Api29(@NonNull final Context context, @NonNull final Playlist playlist) throws IOException {
        @NonNull ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/x-mpegurl");
        //Note: Cannot obtain the permission to "Playlists" folder - Android 13 simply disallows non-standard ones
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, playlist.name);

        // Delete existing, if any
        deletePlaylistFromMediaStore(context, playlist.name);
        // Now create a new one
        @NonNull final ContentResolver resolver = context.getContentResolver();
        final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            SafeToast.show(context, context.getResources().getString(R.string.failed_to_save_playlist, "Null URI"));
            return null;
        }

        try (final OutputStream stream = resolver.openOutputStream(uri, "wt")) {
            M3UWriter.write(context, stream, playlist);
        }

        return Environment.DIRECTORY_MUSIC + File.pathSeparator + playlist.name;
    }
}

package com.poupa.vinylmusicplayer.util;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.M3UWriter;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.PlaylistSong;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.service.MusicService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsUtil {
    private static void notifyChange(@NonNull final Context context) {
        // TODO Use a more appropriate notification
        context.sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final long playlistId) {
        return (StaticPlaylist.getPlaylist(playlistId) != null);
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final String name) {
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

            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.created_playlist_x, name),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.could_not_create_playlist),
                    Toast.LENGTH_SHORT
            ).show();
        }
        return id;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final ArrayList<Playlist> playlists) {
        for (int i = 0; i < playlists.size(); i++) {
            StaticPlaylist.removePlaylist(playlists.get(i).name);
        }
        notifyChange(context);
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
            Toast.makeText(
                    context,
                    context.getResources().getString(
                            R.string.inserted_x_songs_into_playlist_x,
                            songs.size(),
                            list.asPlaylist().name),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, long playlistId) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return;}

        list.removeSong(song.id);
        notifyChange(context);
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
        // TODO This is suboptimal and may create a notification spam
        for (PlaylistSong song : songs) {
            removeFromPlaylist(context, song, song.playlistId);
        }
    }

    public static boolean doesPlaylistContain(@NonNull final Context context, final long playlistId, final long songId) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return false;}

        // TODO Not optimal implementation
        for (Song song : list.asSongs()) {
            if (song.id == songId) {return true;}
        }
        return false;
    }

    public static boolean moveItem(@NonNull final Context context, long playlistId, int from, int to) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(playlistId);
        if (list == null) {return false;}

        if (!list.moveSong(from, to)) {return false;}

        // TODO NOTE: actually for now lets disable this because it messes with the animation (tested on Android 11)
        // notifyChange(context);
        return true;
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        StaticPlaylist list = StaticPlaylist.getPlaylist(id);
        if (list == null) {return;}

        list.rename(newName);
        notifyChange(context);
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        StaticPlaylist playlist = StaticPlaylist.getPlaylist(id);
        if (playlist == null) {return "";}
        return playlist.getName();
    }

    public static File savePlaylist(Context context, Playlist playlist) throws IOException {
        return M3UWriter.write(context, new File(Environment.getExternalStorageDirectory(), "Playlists"), playlist);
    }
}

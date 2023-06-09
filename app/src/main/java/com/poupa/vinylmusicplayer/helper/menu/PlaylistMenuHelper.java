package com.poupa.vinylmusicplayer.helper.menu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.DeletePlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.RenamePlaylistDialog;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.WeakContextAsyncTask;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistMenuHelper {
    public static boolean handleMenuClick(@NonNull AppCompatActivity activity, @NonNull final Playlist playlist, @NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_play) {
            MusicPlayerRemote.openQueue(playlist.getSongs(activity), 0, true);
            return true;
        } else if (itemId == R.id.action_play_next) {
            MusicPlayerRemote.playNext(playlist.getSongs(activity));
            return true;
        } else if (itemId == R.id.action_add_to_current_playing) {
            MusicPlayerRemote.enqueue(playlist.getSongs(activity));
            return true;
        } else if (itemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(playlist.getSongs(activity)).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_rename_playlist) {
            RenamePlaylistDialog.create(playlist.id).show(activity.getSupportFragmentManager(), "RENAME_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_delete_playlist) {
            DeletePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_save_playlist) {
            new SavePlaylistAsyncTask(activity).execute(playlist);
            return true;
        }
        return false;
    }

    private static class SavePlaylistAsyncTask extends WeakContextAsyncTask<Playlist, String, String> {
        public SavePlaylistAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackground(Playlist... params) {
            final Context context = getContext();
            final Playlist playlist = params[0];
            if (playlist.getSongs(context).isEmpty()) {
                return context.getString(R.string.playlist_is_empty);
            }
            try {
                final File file = PlaylistsUtil.savePlaylist(context, playlist);
                return context.getString(R.string.saved_playlist_to, file);
            } catch (IOException e) {
                // Copy the exception to clipboard
                final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                final ClipData clip = ClipData.newPlainText(context.getString(R.string.failed_to_save_playlist), OopsHandler.getStackTrace(e));
                clipboard.setPrimaryClip(clip);

                return context.getString(R.string.failed_to_save_playlist, e);
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        }
    }
}

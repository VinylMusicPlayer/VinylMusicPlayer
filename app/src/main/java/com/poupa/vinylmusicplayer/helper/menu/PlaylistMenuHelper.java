package com.poupa.vinylmusicplayer.helper.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.ClearSmartPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.DeletePlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.ImportFromPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.RenamePlaylistDialog;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.WeakContextAsyncTask;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.smartplaylist.AbsSmartPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.LastAddedPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.NotRecentlyPlayedPlaylist;
import com.poupa.vinylmusicplayer.preferences.SmartPlaylistPreferenceDialog;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.io.IOException;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistMenuHelper {
    public static void hideShowSmartPlaylistMenuItems(final @NonNull Menu menu, final @NonNull AbsSmartPlaylist smartPlaylist) {
        if (!smartPlaylist.isClearable()) {
            menu.findItem(R.id.action_clear_playlist).setVisible(false);
        }
        if (!smartPlaylist.canImport()) {
            menu.findItem(R.id.action_import_from_playlist).setVisible(false);
        }
        final String prefKey = smartPlaylist.getPlaylistPreference();
        if (prefKey == null) {
            menu.findItem(R.id.action_playlist_settings).setVisible(false);
        }

        // "Group by album" option
        if (smartPlaylist instanceof NotRecentlyPlayedPlaylist) {
            final MenuItem item = menu.add(Menu.NONE, R.id.action_song_sort_group_by_album, Menu.NONE, R.string.sort_order_group_by_album);
            item.setCheckable(true)
                    .setEnabled(true)
                    .setChecked(PreferenceUtil.getInstance().getNotRecentlyPlayedSortOrder().equals(PreferenceUtil.ALBUM_SORT_ORDER));
        } else if (smartPlaylist instanceof LastAddedPlaylist) {
            final MenuItem item = menu.add(Menu.NONE, R.id.action_song_sort_group_by_album, Menu.NONE, R.string.sort_order_group_by_album);
            item.setCheckable(true)
                    .setEnabled(true)
                    .setChecked(PreferenceUtil.getInstance().getLastAddedSortOrder().equals(PreferenceUtil.ALBUM_SORT_ORDER));
        }

    }

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
        } else if (itemId == R.id.action_clear_playlist) {
            final AbsSmartPlaylist smartPlaylist = (AbsSmartPlaylist) playlist;
            ClearSmartPlaylistDialog.create(smartPlaylist).show(activity.getSupportFragmentManager(), "CLEAR_SMART_PLAYLIST_" + smartPlaylist.name);
            return true;
        }
        else if (itemId == R.id.action_import_from_playlist) {
            final AbsSmartPlaylist smartPlaylist = (AbsSmartPlaylist) playlist;
            ImportFromPlaylistDialog.create(smartPlaylist).show(activity.getSupportFragmentManager(), "IMPORT_SMART_PLAYLIST_" + smartPlaylist.name);
            return true;
        }
        else if (itemId == R.id.action_playlist_settings) {
            final AbsSmartPlaylist smartPlaylist = (AbsSmartPlaylist) playlist;
            final String prefKey = smartPlaylist.getPlaylistPreference();
            if (prefKey != null) {
                SmartPlaylistPreferenceDialog
                        .newInstance(prefKey)
                        .show(activity.getSupportFragmentManager(), "SETTINGS_SMART_PLAYLIST_" + smartPlaylist.name);
            }
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
                final String file = PlaylistsUtil.savePlaylist(context, playlist);
                return context.getString(R.string.saved_playlist_to, file);
            } catch (IOException e) {
                OopsHandler.collectStackTrace(e);
                return context.getString(R.string.failed_to_save_playlist, e);
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Context context = getContext();
            if (context != null) {
                SafeToast.show(context, string);
            }
        }
    }
}

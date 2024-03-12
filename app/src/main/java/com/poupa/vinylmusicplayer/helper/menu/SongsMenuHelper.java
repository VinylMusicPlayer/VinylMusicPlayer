package com.poupa.vinylmusicplayer.helper.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.helper.DeleteSongsHelper;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsMenuHelper {
    public static void handleMenuClick(@NonNull FragmentActivity activity, @NonNull ArrayList<? extends Song> songs, int menuItemId) {
        if (menuItemId == R.id.action_play_next) {
            MusicPlayerRemote.playNext(songs);
        } else if (menuItemId == R.id.action_add_to_current_playing) {
            MusicPlayerRemote.enqueue(songs);
        } else if (menuItemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
        } else if (menuItemId == R.id.action_delete_from_device) {
            DeleteSongsHelper.delete(songs, activity.getSupportFragmentManager(), "DELETE_SONGS");
        }
    }
}

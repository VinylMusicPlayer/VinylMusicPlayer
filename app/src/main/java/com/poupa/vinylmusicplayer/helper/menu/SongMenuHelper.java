package com.poupa.vinylmusicplayer.helper.menu;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.SongDetailDialog;
import com.poupa.vinylmusicplayer.dialogs.helper.DeleteSongsHelper;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.AbsTagEditorActivity;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.SongTagEditorActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.RingtoneManager;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongMenuHelper {
    public static final int MENU_RES = R.menu.menu_item_song;

    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull Song song, int menuItemId) {
        if (menuItemId == R.id.action_set_as_ringtone) {
            if (RingtoneManager.requiresDialog(activity)) {
                RingtoneManager.showDialog(activity);
            } else {
                RingtoneManager ringtoneManager = new RingtoneManager();
                ringtoneManager.setRingtone(activity, song.id);
            }
            return true;
        } else if (menuItemId == R.id.action_share) {
            activity.startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, activity), null));
            return true;
        } else if (menuItemId == R.id.action_delete_from_device) {
            DeleteSongsHelper.delete(song, activity.getSupportFragmentManager(), "DELETE_SONGS");
            return true;
        } else if (menuItemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(song).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (menuItemId == R.id.action_play_next) {
            MusicPlayerRemote.playNext(song);
            return true;
        } else if (menuItemId == R.id.action_add_to_current_playing) {
            MusicPlayerRemote.enqueue(song);
            return true;
        } else if (menuItemId == R.id.action_tag_editor) {
            Intent tagEditorIntent = new Intent(activity, SongTagEditorActivity.class);
            tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
            if (activity instanceof PaletteColorHolder)
                tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_PALETTE, ((PaletteColorHolder) activity).getPaletteColor());
            activity.startActivity(tagEditorIntent);
            return true;
        } else if (menuItemId == R.id.action_details) {
            SongDetailDialog.create(song).show(activity.getSupportFragmentManager(), "SONG_DETAILS");
            return true;
        } else if (menuItemId == R.id.action_go_to_album) {
            NavigationUtil.goToAlbum(activity, song.albumId);
            return true;
        } else if (menuItemId == R.id.action_go_to_artist) {
            NavigationUtil.goToArtist(activity, song.getArtistNames());
            return true;
        }
        return false;
    }

    public static abstract class OnClickSongMenu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private final AppCompatActivity activity;

        public OnClickSongMenu(@NonNull AppCompatActivity activity) {
            this.activity = activity;
        }

        public int getMenuRes() {
            return MENU_RES;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(getMenuRes());
            popupMenu.setOnMenuItemClickListener(this);

            MenuHelper.decorateDestructiveItems(popupMenu.getMenu(), v.getContext());

            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return handleMenuClick(activity, getSong(), item.getItemId());
        }

        public abstract Song getSong();
    }
}

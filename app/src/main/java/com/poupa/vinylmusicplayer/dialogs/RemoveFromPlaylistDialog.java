package com.poupa.vinylmusicplayer.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RemoveFromPlaylistDialog extends DialogFragment {
    private static final String PLAYLIST_ID = "playlist_id";
    private static final String SONGS = "songs";

    @NonNull
    public static RemoveFromPlaylistDialog create(long playlistId, Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(playlistId, list);
    }

    @NonNull
    public static RemoveFromPlaylistDialog create(long playlistId, ArrayList<? extends Song> songs) {
        RemoveFromPlaylistDialog dialog = new RemoveFromPlaylistDialog();
        Bundle args = new Bundle();
        args.putLong(PLAYLIST_ID, playlistId);
        args.putParcelableArrayList(SONGS, songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @NonNull final Activity activity = requireActivity();
        @NonNull final Bundle arguments = requireArguments();

        final long playlistId = arguments.getLong(PLAYLIST_ID);
        final ArrayList<Song> songs = arguments.getParcelableArrayList(SONGS);
        int title;
        CharSequence content;
        if (songs.size() > 1) {
            title = R.string.remove_songs_from_playlist_title;
            content = Html.fromHtml(getString(R.string.remove_x_songs_from_playlist, songs.size()));
        } else {
            title = R.string.remove_song_from_playlist_title;
            content = Html.fromHtml(getString(R.string.remove_song_x_from_playlist, songs.get(0).title));
        }
        return new MaterialDialog.Builder(activity)
                .title(title)
                .content(content)
                .positiveText(R.string.remove_action)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (getActivity() == null)
                        return;
                    PlaylistsUtil.removeFromPlaylist(activity, songs, playlistId);
                })
                .build();
    }
}

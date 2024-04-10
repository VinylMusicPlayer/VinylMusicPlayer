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
import java.util.List;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RemoveFromPlaylistDialog extends DialogFragment {
    private static final String PLAYLIST_ID = "playlist_id";
    private static final String SONGS = "songs";
    public  static final String TAG = "REMOVE_FROM_PLAYLIST";

    @NonNull
    public static RemoveFromPlaylistDialog create(final long playlistId, final int position, @NonNull final Song song) {
        return create(playlistId, Map.of(position, song));
    }

    @NonNull
    public static RemoveFromPlaylistDialog create(final long playlistId, @NonNull final Map<Integer, ? extends Song> songs) {
        RemoveFromPlaylistDialog dialog = new RemoveFromPlaylistDialog();
        Bundle args = new Bundle();
        args.putLong(PLAYLIST_ID, playlistId);
        args.putIntegerArrayList(SONGS, new ArrayList<>(songs.keySet()));

        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @NonNull final Activity activity = requireActivity();
        @NonNull final Bundle arguments = requireArguments();

        final long playlistId = arguments.getLong(PLAYLIST_ID);
        final List<Integer> songPositions = arguments.getIntegerArrayList(SONGS);
        return new MaterialDialog.Builder(activity)
                .title(R.string.remove_songs_from_playlist_title)
                .content(Html.fromHtml(getString(R.string.remove_x_songs_from_playlist, songPositions.size())))
                .positiveText(R.string.remove_action)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (getActivity() == null)
                        return;
                    PlaylistsUtil.removeFromPlaylist(activity, songPositions, playlistId);
                })
                .build();
    }
}

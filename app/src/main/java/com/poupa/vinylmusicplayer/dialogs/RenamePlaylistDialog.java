package com.poupa.vinylmusicplayer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class RenamePlaylistDialog extends DialogFragment {

    private static final String PLAYLIST_ID = "playlist_id";

    @NonNull
    public static RenamePlaylistDialog create(long playlistId) {
        RenamePlaylistDialog dialog = new RenamePlaylistDialog();
        Bundle args = new Bundle();
        args.putLong(PLAYLIST_ID, playlistId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long playlistId = requireArguments().getLong(PLAYLIST_ID);

        final Activity activity = requireActivity();

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(PlaylistsUtil.getNameForPlaylist(playlistId));

        return new AlertDialog.Builder(activity)
                //.setTitle(R.string.rename_playlist_title)
                .setTitle(R.string.playlist_name_empty)
                .setView(input)
                .setPositiveButton(R.string.rename_action, (dialog, which) -> {
                    final String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        PlaylistsUtil.renamePlaylist(activity, playlistId, name);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .create();
    }
}

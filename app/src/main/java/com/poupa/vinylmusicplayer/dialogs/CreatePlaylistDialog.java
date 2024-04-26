package com.poupa.vinylmusicplayer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class CreatePlaylistDialog extends DialogFragment {
    private static final String SONGS = "songs";

    @NonNull
    public static CreatePlaylistDialog create() {
        return create((Song) null);
    }

    @NonNull
    public static CreatePlaylistDialog create(@Nullable Song song) {
        ArrayList<Song> list = new ArrayList<>();
        if (song != null)
            list.add(song);
        return create(list);
    }

    @NonNull
    public static CreatePlaylistDialog create(List<? extends Song> songs) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, new ArrayList<>(songs));
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = requireActivity();

        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        return new AlertDialog.Builder(activity)
                //.setTitle(R.string.new_playlist_title)
                .setTitle(R.string.playlist_name_empty)
                .setView(input)
                .setPositiveButton(R.string.create_action, (dialog, which) -> {
                    final String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        if (!PlaylistsUtil.doesPlaylistExist(name)) {
                            final long playlistId = PlaylistsUtil.createPlaylist(activity, name);
                            ArrayList<Song> songs = requireArguments().getParcelableArrayList(SONGS);
                            if (songs != null && !songs.isEmpty()) {
                                PlaylistsUtil.addToPlaylist(getActivity(), songs, playlistId, true);
                            }
                        } else {
                            SafeToast.show(
                                    activity,
                                    getActivity().getResources().getString(R.string.playlist_exists, name)
                            );
                        }
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .create();
    }
}

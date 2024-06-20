package com.poupa.vinylmusicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class DeletePlaylistDialog extends DialogFragment {

    @NonNull
    public static DeletePlaylistDialog create(Playlist playlist) {
        ArrayList<Playlist> list = new ArrayList<>();
        list.add(playlist);
        return create(list);
    }

    @NonNull
    public static DeletePlaylistDialog create(ArrayList<Playlist> playlists) {
        DeletePlaylistDialog dialog = new DeletePlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("playlists", playlists);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<Playlist> playlists = getArguments().getParcelableArrayList("playlists");
        int title;
        CharSequence content;
        if (playlists.size() > 1) {
            title = R.string.delete_playlists_title;
            content = Html.fromHtml(getString(R.string.delete_x_playlists, playlists.size()));
        } else {
            title = R.string.delete_playlist_title;
            content = Html.fromHtml(getString(R.string.delete_playlist_x, playlists.get(0).name));
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.delete_action, (dialog, which) -> {
                    dialog.dismiss();
                    if (getActivity() == null)
                        return;
                    PlaylistsUtil.deletePlaylists(getActivity(), playlists);
                })
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .create();
    }
}

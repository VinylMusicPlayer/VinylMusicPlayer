package com.poupa.vinylmusicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.smartplaylist.AbsSmartPlaylist;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ClearSmartPlaylistDialog extends DialogFragment {

    @NonNull
    public static ClearSmartPlaylistDialog create(AbsSmartPlaylist playlist) {
        ClearSmartPlaylistDialog dialog = new ClearSmartPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelable("playlist", playlist);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AbsSmartPlaylist playlist = getArguments().getParcelable("playlist");
        int title = R.string.clear_playlist_title;
        CharSequence content = Html.fromHtml(getString(R.string.clear_playlist_x, playlist.name));

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.clear_action, (dialog, which) -> {
                    if (getActivity() == null) {
                        return;
                    }
                    playlist.clear(getActivity());
                })
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .create();
    }
}

package com.poupa.vinylmusicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongShareDialog extends DialogFragment {
    @NonNull
    public static SongShareDialog create(final Song song) {
        final SongShareDialog dialog = new SongShareDialog();
        final Bundle args = new Bundle();
        args.putParcelable("song", song);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Song song = getArguments().getParcelable("song");
        final String currentlyListening = getString(
                R.string.currently_listening_to_x_by_x,
                song.getTitle(),
                MultiValuesTagUtil.infoStringAsArtists(song.artistNames));
        final String[] items = new String[]{
                getString(R.string.the_audio_file),
                "\u201C" + currentlyListening + "\u201D"
        };
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.what_do_you_want_to_share)
                .setItems(items, (materialDialog, which) -> {
                    switch (which) {
                        case 0:
                            startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, getContext()), null));
                            break;
                        case 1:
                            getActivity().startActivity(
                                    Intent.createChooser(
                                            new Intent()
                                                    .setAction(Intent.ACTION_SEND)
                                                    .putExtra(Intent.EXTRA_TEXT, currentlyListening)
                                                    .setType("text/plain"),
                                            null
                                    )
                            );
                            break;
                    }
                })
                .create();
    }
}

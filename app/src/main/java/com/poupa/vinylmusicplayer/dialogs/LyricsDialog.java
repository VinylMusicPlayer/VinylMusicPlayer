package com.poupa.vinylmusicplayer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.poupa.vinylmusicplayer.model.lyrics.Lyrics;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LyricsDialog extends DialogFragment {
    public static LyricsDialog create(@NonNull Lyrics lyrics) {
        LyricsDialog dialog = new LyricsDialog();
        Bundle args = new Bundle();
        args.putString("title", lyrics.song.getTitle());
        args.putString("lyrics", lyrics.getText());
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString("title"))
                .setMessage(getArguments().getString("lyrics"))
                .create();
    }
}

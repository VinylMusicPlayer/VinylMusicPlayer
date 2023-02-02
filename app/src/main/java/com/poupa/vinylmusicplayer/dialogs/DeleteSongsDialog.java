package com.poupa.vinylmusicplayer.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.saf.SAFGuideActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class DeleteSongsDialog extends DialogFragment {
    private static final String SONGS = "songs";

    private BaseDeleteSongsAsyncTask deleteSongsTask;
    private ArrayList<Song> songsToRemove;
    private Song currentSong;

    private ActivityResultLauncher<String> deleteSongsKitkat_SAFFilePicker;
    private ActivityResultLauncher<Intent> deleteSongs_SAFGuide;
    private ActivityResultLauncher<Uri> deleteSongs_SAFTreePicker;

    @NonNull
    public static DeleteSongsDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static DeleteSongsDialog create(ArrayList<Song> songs) {
        DeleteSongsDialog dialog = new DeleteSongsDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, songs);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteSongs_SAFGuide = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        deleteSongs_SAFTreePicker.launch(null);
                    }
                });

        deleteSongs_SAFTreePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, Uri input) {
                        return super.createIntent(context, input)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> {
                    if (deleteSongsTask != null) {
                        deleteSongsTask.cancel(true);
                    }
                    deleteSongsTask = new DeleteSongsLollipopAsyncTask(this);
                    ((DeleteSongsLollipopAsyncTask)deleteSongsTask).execute(resultUri);
                });

        deleteSongsKitkat_SAFFilePicker = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("audio/*") {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> {
                    if (deleteSongsTask != null) {
                        deleteSongsTask.cancel(true);
                    }
                    deleteSongsTask = new DeleteSongsKitkatAsyncTask(this);
                    ((DeleteSongsKitkatAsyncTask)deleteSongsTask).execute(resultUri);
                });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<Song> songs = requireArguments().getParcelableArrayList(SONGS);
        int title;
        CharSequence content;
        if (songs.size() > 1) {
            title = R.string.delete_songs_title;
            content = Html.fromHtml(getString(R.string.delete_x_songs, songs.size()));
        } else {
            title = R.string.delete_song_title;
            content = Html.fromHtml(getString(R.string.delete_song_x, songs.get(0).title));
        }
        return new MaterialDialog.Builder(requireActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.delete_action)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onPositive((dialog, which) -> {
                    // If song removed was the playing song, then play the next song
                    if ((songs.size() == 1) && MusicPlayerRemote.isPlaying(songs.get(0))) {
                        MusicPlayerRemote.playNextSong();
                    }

                    // Now remove the track in background
                    songsToRemove = songs;
                    deleteSongsTask = new DeleteSongsAsyncTask(DeleteSongsDialog.this);
                    ((DeleteSongsAsyncTask)deleteSongsTask).execute(songs);
                })
                .onNegative((materialDialog, dialogAction) -> dismiss())
                .build();
    }

    private void deleteSongs(List<Song> songs, List<Uri> safUris) {
        MusicUtil.deleteTracks(requireActivity(), songs, safUris, this::dismiss);
    }

    private void deleteSongsKitkat() {
        if (songsToRemove.size() < 1) {
            dismiss();
            return;
        }

        currentSong = songsToRemove.remove(0);

        if (!SAFUtil.isSAFRequired(currentSong)) {
            deleteSongs(Collections.singletonList(currentSong), null);
            deleteSongsKitkat();
        } else {
            final String message = getString(R.string.saf_pick_file, currentSong.data);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            deleteSongsKitkat_SAFFilePicker.launch(message);
        }
    }

    private static abstract class BaseDeleteSongsAsyncTask<Params>
            extends DialogAsyncTask<Params, Integer, Void>
    {
        protected final WeakReference<DeleteSongsDialog> dialog;
        protected final WeakReference<FragmentActivity> activity;

        public BaseDeleteSongsAsyncTask(DeleteSongsDialog dialog) {
            super(dialog.getActivity());
            this.dialog = new WeakReference<>(dialog);
            this.activity = new WeakReference<>(dialog.getActivity());
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return new MaterialDialog.Builder(context)
                    .title(R.string.deleting_songs)
                    .cancelable(false)
                    .progress(true, 0)
                    .build();
        }
    }

    private static class DeleteSongsAsyncTask
            extends BaseDeleteSongsAsyncTask<List<Song>> {
        public DeleteSongsAsyncTask(DeleteSongsDialog dialog) {
            super(dialog);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Song>... lists) {
            try {
                DeleteSongsDialog dialog = this.dialog.get();
                FragmentActivity activity = this.activity.get();

                if (dialog == null || activity == null) {return null;}

                final List<Song> songs = lists[0];
                if (!SAFUtil.isSAFRequiredForSongs(songs)) {
                    dialog.deleteSongs(songs, null);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (SAFUtil.isSDCardAccessGranted(activity)) {
                            dialog.deleteSongs(songs, null);
                        } else {
                            dialog.deleteSongs_SAFGuide.launch(new Intent(activity, SAFGuideActivity.class));
                        }
                    } else {
                        dialog.deleteSongsKitkat();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static class DeleteSongsKitkatAsyncTask
            extends BaseDeleteSongsAsyncTask<Uri> {

        public DeleteSongsKitkatAsyncTask(DeleteSongsDialog dialog) {
            super(dialog);
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                DeleteSongsDialog dialog = this.dialog.get();
                if (dialog != null) {
                    dialog.deleteSongs(List.of(dialog.currentSong), List.of(uris[0]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static class DeleteSongsLollipopAsyncTask
            extends BaseDeleteSongsAsyncTask<Uri> {

        public DeleteSongsLollipopAsyncTask(DeleteSongsDialog dialog) {
            super(dialog);
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                DeleteSongsDialog dialog = this.dialog.get();
                FragmentActivity activity = this.activity.get();

                if (dialog == null || activity == null) {return null;}

                SAFUtil.saveTreeUri(activity, uris[0]);
                dialog.deleteSongs(dialog.songsToRemove, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

package com.poupa.vinylmusicplayer.dialogs.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.saf.SAFGuideActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.SAFUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class DeleteSongsDialogApi19 extends DialogFragment {
    private static final String SONGS = "songs";

    private BaseDeleteSongsAsyncTask deleteSongsTask;
    ArrayList<Song> songsToRemove;
    Song currentSong;

    private ActivityResultLauncher<String> deleteSongsApi19_SAFFilePicker;
    ActivityResultLauncher<Intent> deleteSongs_SAFGuide;
    private ActivityResultLauncher<Uri> deleteSongs_SAFTreePicker;

    @NonNull
    public static DeleteSongsDialogApi19 create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static DeleteSongsDialogApi19 create(Collection<? extends Song> songs) {
        DeleteSongsDialogApi19 dialog = new DeleteSongsDialogApi19();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, new ArrayList<>(songs));
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
                    deleteSongsTask = new AsyncTaskApi21(this);
                    ((AsyncTaskApi21)deleteSongsTask).execute(resultUri);
                });

        deleteSongsApi19_SAFFilePicker = registerForActivityResult(
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
                    deleteSongsTask = new AsyncTaskApi19(this);
                    ((AsyncTaskApi19)deleteSongsTask).execute(resultUri);
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
            content = Html.fromHtml(getString(R.string.delete_song_x, songs.get(0).getTitle()));
        }
        return new MaterialDialog.Builder(requireActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.delete_action)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onPositive((dialog, which) -> {
                    dismiss();

                    DeleteSongsHelper.managePlayingSong(songs);

                    // Now remove the track in background
                    songsToRemove = songs;
                    deleteSongsTask = new DeleteSongsAsyncTask(DeleteSongsDialogApi19.this);
                    ((DeleteSongsAsyncTask)deleteSongsTask).execute(songs);
                })
                .onNegative((materialDialog, dialogAction) -> dismiss())
                .build();
    }

    void deleteSongs(List<Song> songs, List<Uri> safUris) {
        MusicUtil.deleteTracks(DeleteSongsDialogApi19.this, null, songs, safUris);
    }

    void deleteSongsApi19() {
        if (songsToRemove.size() < 1) {
            dismiss();
            return;
        }

        currentSong = songsToRemove.remove(0);

        if (!SAFUtil.isSAFRequired(currentSong)) {
            deleteSongs(Collections.singletonList(currentSong), null);
            deleteSongsApi19();
        } else {
            final String message = getString(R.string.saf_pick_file, currentSong.data);
            SafeToast.show(getActivity(), message);
            deleteSongsApi19_SAFFilePicker.launch(message);
        }
    }

    private static abstract class BaseDeleteSongsAsyncTask<Params>
            extends DialogAsyncTask<Params, Integer, Void>
    {
        protected final WeakReference<DeleteSongsDialogApi19> dialog;
        protected final WeakReference<FragmentActivity> activity;

        public BaseDeleteSongsAsyncTask(DeleteSongsDialogApi19 dialog) {
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
        public DeleteSongsAsyncTask(DeleteSongsDialogApi19 dialog) {
            super(dialog);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Song>... lists) {
            try {
                DeleteSongsDialogApi19 dialog = this.dialog.get();
                FragmentActivity activity = this.activity.get();

                if (dialog == null || activity == null) {return null;}

                final List<Song> songs = lists[0];
                if (!SAFUtil.isSAFRequired(songs)) {
                    dialog.deleteSongs(songs, null);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (SAFUtil.isSDCardAccessGranted(activity)) {
                            dialog.deleteSongs(songs, null);
                        } else {
                            dialog.deleteSongs_SAFGuide.launch(new Intent(activity, SAFGuideActivity.class));
                        }
                    } else {
                        dialog.deleteSongsApi19();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static class AsyncTaskApi19
            extends BaseDeleteSongsAsyncTask<Uri> {

        public AsyncTaskApi19(DeleteSongsDialogApi19 dialog) {
            super(dialog);
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                DeleteSongsDialogApi19 dialog = this.dialog.get();
                if (dialog != null) {
                    dialog.deleteSongs(List.of(dialog.currentSong), List.of(uris[0]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static class AsyncTaskApi21
            extends BaseDeleteSongsAsyncTask<Uri> {

        public AsyncTaskApi21(DeleteSongsDialogApi19 dialog) {
            super(dialog);
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                DeleteSongsDialogApi19 dialog = this.dialog.get();
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

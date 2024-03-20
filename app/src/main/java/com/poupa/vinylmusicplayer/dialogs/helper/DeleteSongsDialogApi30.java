package com.poupa.vinylmusicplayer.dialogs.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.misc.WeakContextAsyncTask;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.saf.SAFGuideActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SAFUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
@RequiresApi(api = VERSION_CODES.R)
public class DeleteSongsDialogApi30 extends Fragment {
    private static final String SONGS = "songs";

    private BaseDeleteSongsAsyncTask deleteSongsTask;
    ArrayList<Song> songsToRemove;
    ActivityResultLauncher<Intent> deleteSongs_SAFGuide;
    private ActivityResultLauncher<Uri> deleteSongs_SAFTreePicker;

    private ActivityResultLauncher<IntentSenderRequest> deleteRequestApi30;

    @NonNull
    public static DeleteSongsDialogApi30 create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static DeleteSongsDialogApi30 create(Collection<? extends Song> songs) {
        DeleteSongsDialogApi30 dialog = new DeleteSongsDialogApi30();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, new ArrayList<>(songs));
        dialog.setArguments(args);
        return dialog;
    }

    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteSongs_SAFGuide = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            deleteSongs_SAFTreePicker.launch(Uri.parse(PreferenceUtil.getInstance().getStartDirectory().getAbsolutePath()));
                        } catch (android.content.ActivityNotFoundException noActivity) {
                            SafeToast.show(getActivity(), R.string.android13_no_file_browser_error);
                        }
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
                    deleteSongsTask = new AsyncTaskApi30(this);
                    ((AsyncTaskApi30)deleteSongsTask).execute(resultUri);
                });

        deleteRequestApi30 = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        DeleteSongsHelper.managePlayingSong(songsToRemove);
                        SafeToast.show(getActivity(), getString(R.string.deleted_x_songs, songsToRemove.size()));
                    } else {
                        SafeToast.show(getActivity(), getString(R.string.saf_delete_failed, Integer.toString(songsToRemove.size())));
                    }
                });

        songsToRemove = requireArguments().getParcelableArrayList(SONGS);

        deleteSongsTask = new DeleteSongsAsyncTask(this);
        ((DeleteSongsAsyncTask)deleteSongsTask).execute(songsToRemove);
    }

    void deleteSongs(List<Song> songsToRemove) {
        MusicUtil.deleteTracks(DeleteSongsDialogApi30.this, this.deleteRequestApi30, songsToRemove, null);
    }

    public static abstract class BaseDeleteSongsAsyncTask<Params> extends
            WeakContextAsyncTask<Params, Integer, Void> {
        protected final WeakReference<DeleteSongsDialogApi30> fragment;
        protected final WeakReference<FragmentActivity> activity;

        public BaseDeleteSongsAsyncTask(DeleteSongsDialogApi30 fragment) {
            super(fragment.getActivity());
            this.fragment = new WeakReference<>(fragment);
            this.activity = new WeakReference<>(fragment.getActivity());
        }
    }

    private static class DeleteSongsAsyncTask
            extends BaseDeleteSongsAsyncTask<List<Song>> {
        public DeleteSongsAsyncTask(DeleteSongsDialogApi30 fragment) {
            super(fragment);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Song>... lists) {
            DeleteSongsDialogApi30 fragment = this.fragment.get();
            FragmentActivity activity = this.activity.get();

            try {
                if (fragment == null || activity == null) {return null;}

                final List<Song> songs = lists[0];
                if (!SAFUtil.isSAFRequired(songs)) {
                    fragment.deleteSongs(songs);
                } else {
                    if (SAFUtil.isSDCardAccessGranted(activity)) {
                        fragment.deleteSongs(songs);
                    } else {
                        fragment.deleteSongs_SAFGuide.launch(new Intent(activity, SAFGuideActivity.class));
                    }
                }
            } catch (Exception e) {
                OopsHandler.collectStackTrace(e);
            }

            return null;
        }
    }

    private static class AsyncTaskApi30
            extends BaseDeleteSongsAsyncTask<Uri> {

        public AsyncTaskApi30(DeleteSongsDialogApi30 dialog) {
            super(dialog);
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                DeleteSongsDialogApi30 fragment = this.fragment.get();
                FragmentActivity activity = this.activity.get();

                if (fragment == null || activity == null) {return null;}

                SAFUtil.saveTreeUri(activity, uris[0]);
                fragment.deleteSongs(fragment.songsToRemove);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}